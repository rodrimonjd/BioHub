// Prueba de rendimiento de HU-14.1: "las consultas públicas responden en menos de 1 segundo".
//
// Lanza muchas consultas en paralelo contra servicio-consulta-publica (y unas pocas por el API Gateway,
// que limita a 60 por minuto), mide cada respuesta y escribe un informe en Markdown y en JSON.
// Solo usa Node 22 (fetch y performance nativos), sin dependencias.
//
//   node tests/rendimiento/consulta-publica.mjs
//
// Variables de entorno (todas opcionales):
//   CONSULTA_URL   servicio de consulta pública            (http://localhost:8083)
//   GATEWAY_URL    API Gateway                             (http://localhost:8080)
//   API_KEY        API key de un consumidor externo         (unal-demo-2026, la de demostración)
//   SOLICITUDES    solicitudes medidas por escenario        (500)
//   CONCURRENCIA   solicitudes en paralelo                  (25)
//   UMBRAL_MS      tiempo máximo aceptado por respuesta     (1000)
//   SALIDA         carpeta del informe                      (docs/entrega-3-implementacion/rendimiento)

import { mkdirSync, writeFileSync } from 'node:fs';
import { join } from 'node:path';
import { cpus, totalmem } from 'node:os';

const CONSULTA_URL = process.env.CONSULTA_URL ?? 'http://localhost:8083';
const GATEWAY_URL = process.env.GATEWAY_URL ?? 'http://localhost:8080';
const API_KEY = process.env.API_KEY ?? 'unal-demo-2026';
const SOLICITUDES = Number(process.env.SOLICITUDES ?? 500);
const CONCURRENCIA = Number(process.env.CONCURRENCIA ?? 25);
const UMBRAL_MS = Number(process.env.UMBRAL_MS ?? 1000);
const SALIDA = process.env.SALIDA ?? 'docs/entrega-3-implementacion/rendimiento';
// El gateway rechaza con 429 desde la solicitud 61 del minuto: se mide por debajo de ese límite.
const SOLICITUDES_GATEWAY = 50;
const CALENTAMIENTO = 20;

const BASE = `${CONSULTA_URL}/api/v1/consulta/hallazgos`;

async function medir(url, cabeceras = {}) {
  const inicio = performance.now();
  try {
    const res = await fetch(url, { headers: cabeceras });
    await res.arrayBuffer(); // el tiempo incluye recibir todo el cuerpo
    return { ms: performance.now() - inicio, estado: res.status };
  } catch (e) {
    return { ms: performance.now() - inicio, estado: 0, error: e.message };
  }
}

/** Ejecuta `total` solicitudes con `concurrencia` en vuelo a la vez. */
async function lote(urls, total, concurrencia, cabeceras) {
  const resultados = [];
  let siguiente = 0;
  const inicio = performance.now();
  async function trabajador() {
    while (siguiente < total) {
      const i = siguiente++;
      resultados.push(await medir(urls[i % urls.length], cabeceras));
    }
  }
  await Promise.all(Array.from({ length: Math.min(concurrencia, total) }, trabajador));
  return { resultados, segundos: (performance.now() - inicio) / 1000 };
}

function percentil(ordenados, p) {
  if (ordenados.length === 0) return 0;
  const i = Math.min(ordenados.length - 1, Math.ceil((p / 100) * ordenados.length) - 1);
  return ordenados[Math.max(0, i)];
}

function resumir(nombre, descripcion, { resultados, segundos }, estadoEsperado = 200) {
  const ok = resultados.filter((r) => r.estado === estadoEsperado);
  const tiempos = ok.map((r) => r.ms).sort((a, b) => a - b);
  const bajoUmbral = tiempos.filter((t) => t < UMBRAL_MS).length;
  const p95 = percentil(tiempos, 95);
  const errores = resultados.length - ok.length;
  return {
    nombre,
    descripcion,
    solicitudes: resultados.length,
    errores,
    estados: Object.fromEntries(
      Object.entries(
        resultados.reduce((acc, r) => ({ ...acc, [r.estado]: (acc[r.estado] ?? 0) + 1 }), {})
      )
    ),
    minMs: tiempos[0] ?? 0,
    promedioMs: tiempos.length ? tiempos.reduce((a, b) => a + b, 0) / tiempos.length : 0,
    p50Ms: percentil(tiempos, 50),
    p95Ms: p95,
    p99Ms: percentil(tiempos, 99),
    maxMs: tiempos[tiempos.length - 1] ?? 0,
    bajoUmbralPct: tiempos.length ? (bajoUmbral / tiempos.length) * 100 : 0,
    porSegundo: resultados.length / segundos,
    cumple: errores === 0 && tiempos.length > 0 && p95 < UMBRAL_MS,
  };
}

const ms = (v) => `${v.toFixed(1)} ms`;

function informeMarkdown(escenarios, fecha) {
  const cumpleTodo = escenarios.every((e) => e.cumple);
  const filas = escenarios
    .map(
      (e) =>
        `| ${e.nombre} | ${e.solicitudes} | ${e.errores} | ${ms(e.p50Ms)} | ${ms(e.p95Ms)} | ${ms(e.p99Ms)} | ${ms(e.maxMs)} | ${e.bajoUmbralPct.toFixed(1)} % | ${e.porSegundo.toFixed(0)} | ${e.cumple ? 'Sí' : 'No'} |`
    )
    .join('\n');
  const detalle = escenarios.map((e) => `- **${e.nombre}**: ${e.descripcion}`).join('\n');
  return `# Pruebas de rendimiento — HU-14.1

**Historia:** como visitante o consumidor del sistema, quiero que las consultas públicas respondan en menos de un segundo.

**Resultado: ${cumpleTodo ? 'CUMPLE' : 'NO CUMPLE'}** — criterio: sin errores y percentil 95 por debajo de ${UMBRAL_MS} ms en cada escenario.

- Fecha de ejecución: ${fecha}
- Solicitudes medidas por escenario: ${SOLICITUDES} (${CONCURRENCIA} en paralelo), después de ${CALENTAMIENTO} de calentamiento que no se cuentan
- Por el API Gateway: ${SOLICITUDES_GATEWAY} solicitudes, por debajo del límite de 60 por minuto (ASR-03)
- Equipo: ${cpus().length} núcleos (${cpus()[0]?.model.trim() ?? 'desconocido'}), ${(totalmem() / 1024 ** 3).toFixed(1)} GB de RAM, Node ${process.version}
- Servicio medido: \`${CONSULTA_URL}\` · Gateway: \`${GATEWAY_URL}\`

| Escenario | Solicitudes | Errores | p50 | p95 | p99 | Máximo | Bajo ${UMBRAL_MS} ms | Solicitudes/s | Cumple |
|---|---|---|---|---|---|---|---|---|---|
${filas}

## Escenarios
${detalle}

## Cómo repetir la prueba
Con el entorno levantado (\`docker compose -f infra/docker-compose.yml up -d\`), desde la raíz del repositorio:

\`\`\`bash
node tests/rendimiento/consulta-publica.mjs
\`\`\`

El script termina con código 1 si algún escenario no cumple, para poder usarlo en integración continua.
`;
}

async function main() {
  // El detalle de un hallazgo necesita un id real
  const primera = await fetch(`${BASE}?tamano=5`).then((r) => r.json()).catch(() => null);
  if (!primera) {
    console.error(`No responde ${BASE}. ¿Está levantado servicio-consulta-publica?`);
    process.exit(2);
  }
  const ids = (primera.contenido ?? []).map((h) => h.id);
  const especie = primera.contenido?.[0]?.especie ?? 'Panthera';
  const departamento = primera.contenido?.[0]?.departamento ?? 'Meta';
  const hoy = new Date().toISOString().slice(0, 10);

  const escenarios = [
    ['Listado público', 'Primera página del inventario público (20 hallazgos).', [`${BASE}?tamano=20`]],
    ['Filtro por departamento', `Hallazgos del departamento ${departamento}.`, [`${BASE}?departamento=${encodeURIComponent(departamento)}`]],
    ['Búsqueda por especie', `Búsqueda de texto por la especie "${especie}".`, [`${BASE}?especie=${encodeURIComponent(especie.split(' ')[0])}`]],
    ['Rango de fechas', `Hallazgos registrados entre 2026-01-01 y ${hoy}.`, [`${BASE}?fechaDesde=2026-01-01&fechaHasta=${hoy}`]],
  ];
  if (ids.length) {
    escenarios.push(['Detalle de un hallazgo', `Detalle por id, rotando entre ${ids.length} hallazgos.`, ids.map((id) => `${BASE}/${id}`)]);
  }

  const resultados = [];
  for (const [nombre, descripcion, urls] of escenarios) {
    await lote(urls, CALENTAMIENTO, CONCURRENCIA);
    const r = resumir(nombre, descripcion, await lote(urls, SOLICITUDES, CONCURRENCIA));
    resultados.push(r);
    console.log(`${r.cumple ? 'OK ' : 'MAL'} ${nombre}: p95 ${ms(r.p95Ms)}, máx ${ms(r.maxMs)}, errores ${r.errores}`);
  }

  // Por el API Gateway, como lo usaría una universidad u ONG
  const gateway = resumir(
    'API Gateway (consumidor externo)',
    'Inventario por la URL pública estable, con la API key de un consumidor y por debajo de su límite por minuto.',
    await lote([`${GATEWAY_URL}/api/v1/inventario?tamano=20`], SOLICITUDES_GATEWAY, 5, { 'X-API-Key': API_KEY })
  );
  resultados.push(gateway);
  console.log(`${gateway.cumple ? 'OK ' : 'MAL'} ${gateway.nombre}: p95 ${ms(gateway.p95Ms)}, estados ${JSON.stringify(gateway.estados)}`);

  const fecha = new Date().toISOString();
  mkdirSync(SALIDA, { recursive: true });
  writeFileSync(join(SALIDA, 'resultados-HU-14.1.md'), informeMarkdown(resultados, fecha));
  writeFileSync(
    join(SALIDA, 'resultados-HU-14.1.json'),
    JSON.stringify({ fecha, umbralMs: UMBRAL_MS, solicitudes: SOLICITUDES, concurrencia: CONCURRENCIA, escenarios: resultados }, null, 2)
  );
  console.log(`Informe: ${join(SALIDA, 'resultados-HU-14.1.md')}`);
  process.exit(resultados.every((e) => e.cumple) ? 0 : 1);
}

main();
