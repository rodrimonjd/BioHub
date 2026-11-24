'use client';

import React, { useEffect, useRef, useState } from 'react';

export interface TerrenoTopograficoProps {
  className?: string;
  /** Lo que se ve mientras carga o si el navegador no tiene WebGL 2. */
  respaldo?: React.ReactNode;
}

const COLUMNAS = 200;
const FILAS = 140;
const ANCHO = 22;
const Z_CERCA = 3.6;
const Z_LEJOS = -11;

const OJO_Y = 3.1;
const OJO_Z = 4.4;
const INCLINACION = (29 * Math.PI) / 180;
const APERTURA = (46 * Math.PI) / 180;

const MAX_PULSOS = 3;
const VIDA_PULSO = 4;
const ENTRADA = 1.8;

/** Cuadros que se miden antes de decidir si el equipo va lento, y el promedio que se tolera. */
const CUADROS_MEDIDOS = 40;
const MILISEGUNDOS_LENTO = 42;

/** Altura y brillo del suelo: los usan los vértices (la forma) y cada píxel (para que la curva salga lisa). */
const RELIEVE = `
uniform float uTiempo, uEntrada, uAlza;
uniform vec2 uCursor;
uniform vec3 uPulsos[${MAX_PULSOS}];

vec2 azar(vec2 p) {
  p = vec2(dot(p, vec2(127.1, 311.7)), dot(p, vec2(269.5, 183.3)));
  return -1.0 + 2.0 * fract(sin(p) * 43758.5453);
}
float ruido(vec2 p) {
  vec2 i = floor(p), f = fract(p);
  vec2 u = f * f * f * (f * (f * 6.0 - 15.0) + 10.0);
  return mix(
    mix(dot(azar(i), f), dot(azar(i + vec2(1, 0)), f - vec2(1, 0)), u.x),
    mix(dot(azar(i + vec2(0, 1)), f - vec2(0, 1)), dot(azar(i + vec2(1, 1)), f - vec2(1, 1)), u.x),
    u.y);
}
vec2 suelo(vec2 p) {
  float a = ruido(p * 0.34 + vec2(3.7, uTiempo * -0.018));
  float b = ruido(p * 0.78 + vec2(uTiempo * 0.012, 8.1 + uTiempo * -0.03));
  float c = ruido(p * 1.7 + vec2(5.2, uTiempo * -0.04));
  // Más bajo detrás del texto, más alto hacia la derecha y hacia el fondo
  float lado = mix(0.45, 1.2, smoothstep(-4.5, 3.5, p.x)) * mix(1.25, 0.85, smoothstep(-5.0, 2.5, p.y));
  float h = (0.62 + a * 1.75 + b * 0.55 + c * 0.1) * lado * uEntrada;

  float cerca = length(p - uCursor);
  h += uAlza * 0.5 * exp(-cerca * cerca * 0.85);
  float brillo = uAlza * 0.35 * exp(-cerca * cerca * 1.1);
  for (int i = 0; i < ${MAX_PULSOS}; i++) {
    float edad = uPulsos[i].z;
    if (edad < 0.0) continue;
    float d = length(p - uPulsos[i].xy) - edad * 2.1;
    float anillo = exp(-d * d * 14.0) * exp(-edad * 0.95) * smoothstep(0.0, 0.15, edad);
    h += anillo * 0.12;
    brillo += anillo * 2.2;
  }
  return vec2(h, brillo);
}`;

const VERTICES = `#version 300 es
precision highp float;
in vec2 aPos;
uniform float uAspecto, uFoco;
uniform vec2 uOjo, uGiro;
out vec2 vPunto;
out float vFondo;
${RELIEVE}
void main() {
  vec3 v = vec3(aPos.x, suelo(aPos).x - uOjo.x, aPos.y - uOjo.y);
  float y = v.y * uGiro.x - v.z * uGiro.y;
  float fondo = -v.y * uGiro.y - v.z * uGiro.x;
  vPunto = aPos;
  vFondo = fondo;
  gl_Position = vec4(v.x * uFoco / uAspecto, y * uFoco, (fondo - 0.1) * 1.01 - 0.2, fondo);
}`;

const FRAGMENTOS = `#version 300 es
precision highp float;
in vec2 vPunto;
in float vFondo;
uniform float uGrosor;
out vec4 salida;
${RELIEVE}
const vec3 SUELO = vec3(0.0706, 0.2157, 0.1647);
const vec3 LADERA = vec3(0.1050, 0.3200, 0.2400);
const vec3 LINEA = vec3(0.5608, 0.7490, 0.4980);
const vec3 CUMBRE = vec3(0.9300, 0.7300, 0.3300);

void main() {
  vec2 s = suelo(vPunto);
  float altura = s.x, brillo = s.y;
  float c = altura * 13.0;
  float cambio = max(fwidth(c), 1e-4);
  float lejos = abs(fract(c - 0.5) - 0.5) / cambio;
  float mayor = step(abs(mod(floor(c + 0.5), 5.0)), 0.5);
  float grosor = uGrosor * mix(0.75, 1.3, mayor);
  float linea = 1.0 - smoothstep(grosor - 0.6, grosor + 0.6, lejos);
  // Donde las curvas quedan más juntas que un píxel se apagan, para que no titilen
  linea *= 1.0 - smoothstep(0.3, 0.75, cambio);

  float alto = smoothstep(0.85, 1.55, altura);
  float niebla = 1.0 - smoothstep(6.0, 13.5, vFondo);
  vec3 base = mix(SUELO, LADERA, smoothstep(0.2, 1.7, altura) * 0.85);
  vec3 tinta = mix(LINEA, CUMBRE, alto);
  float fuerza = linea * (mix(0.4, 0.75, mayor) + alto * 0.22 + brillo * 0.8) + min(brillo, 1.0) * 0.1;
  vec3 color = mix(base, tinta, clamp(fuerza, 0.0, 1.0));
  salida = vec4(mix(SUELO, color, niebla), 1.0);
}`;

function compilar(gl: WebGL2RenderingContext): WebGLProgram | null {
  const programa = gl.createProgram();
  const partes = [
    [gl.VERTEX_SHADER, VERTICES],
    [gl.FRAGMENT_SHADER, FRAGMENTOS],
  ] as const;
  for (const [tipo, codigo] of partes) {
    const parte = gl.createShader(tipo);
    if (!parte) return null;
    gl.shaderSource(parte, codigo);
    gl.compileShader(parte);
    gl.attachShader(programa, parte);
    gl.deleteShader(parte);
  }
  gl.linkProgram(programa);
  return gl.getProgramParameter(programa, gl.LINK_STATUS) ? programa : null;
}

function crearMalla(): { puntos: Float32Array; indices: Uint32Array } {
  const puntos = new Float32Array(COLUMNAS * FILAS * 2);
  const indices = new Uint32Array((COLUMNAS - 1) * (FILAS - 1) * 6);
  let n = 0;
  for (let f = 0; f < FILAS; f++) {
    for (let c = 0; c < COLUMNAS; c++) {
      const i = (f * COLUMNAS + c) * 2;
      puntos[i] = (c / (COLUMNAS - 1) - 0.5) * ANCHO;
      puntos[i + 1] = Z_CERCA + (f / (FILAS - 1)) * (Z_LEJOS - Z_CERCA);
      if (f === FILAS - 1 || c === COLUMNAS - 1) continue;
      const a = f * COLUMNAS + c;
      indices.set([a, a + 1, a + COLUMNAS, a + 1, a + COLUMNAS + 1, a + COLUMNAS], n);
      n += 6;
    }
  }
  return { puntos, indices };
}

/**
 * Terreno de curvas de nivel en 3D. Con mouse el relieve se levanta bajo el cursor y cada clic
 * manda un pulso. En pantallas táctiles o con "reducir movimiento" se dibuja una sola vez.
 * Escucha el puntero en su elemento contenedor para que el texto pueda ir encima.
 */
export function TerrenoTopografico({ className = '', respaldo }: TerrenoTopograficoProps) {
  const lienzo = useRef<HTMLCanvasElement>(null);
  const [listo, setListo] = useState(false);

  useEffect(() => {
    const canvas = lienzo.current;
    const zona = canvas?.parentElement?.parentElement;
    const gl = canvas?.getContext('webgl2', { antialias: true, alpha: false, powerPreference: 'low-power' });
    if (!canvas || !zona || !gl) return;
    const programa = compilar(gl);
    if (!programa) return;

    const vivo =
      !window.matchMedia('(prefers-reduced-motion: reduce)').matches &&
      window.matchMedia('(hover: hover) and (pointer: fine)').matches;

    const { puntos, indices } = crearMalla();
    const bufferPuntos = gl.createBuffer();
    const bufferIndices = gl.createBuffer();
    gl.useProgram(programa);
    gl.bindBuffer(gl.ARRAY_BUFFER, bufferPuntos);
    gl.bufferData(gl.ARRAY_BUFFER, puntos, gl.STATIC_DRAW);
    gl.bindBuffer(gl.ELEMENT_ARRAY_BUFFER, bufferIndices);
    gl.bufferData(gl.ELEMENT_ARRAY_BUFFER, indices, gl.STATIC_DRAW);
    const atributo = gl.getAttribLocation(programa, 'aPos');
    gl.enableVertexAttribArray(atributo);
    gl.vertexAttribPointer(atributo, 2, gl.FLOAT, false, 0, 0);
    gl.enable(gl.DEPTH_TEST);
    gl.clearColor(0.0706, 0.2157, 0.1647, 1);

    const u = (nombre: string) => gl.getUniformLocation(programa, nombre);
    const variables = {
      tiempo: u('uTiempo'),
      entrada: u('uEntrada'),
      aspecto: u('uAspecto'),
      alza: u('uAlza'),
      cursor: u('uCursor'),
      pulsos: u('uPulsos[0]'),
      grosor: u('uGrosor'),
    };
    const foco = 1 / Math.tan(APERTURA / 2);
    const coseno = Math.cos(INCLINACION);
    const seno = Math.sin(INCLINACION);
    gl.uniform1f(u('uFoco'), foco);
    gl.uniform2f(u('uOjo'), OJO_Y, OJO_Z);
    gl.uniform2f(u('uGiro'), coseno, seno);

    let cuadro = 0;
    let anterior = 0;
    let medidos = 0;
    let sumaMs = 0;
    let calidad = 1;
    let detenido = false;
    let tiempo = vivo ? 0 : 6;
    let entrada = vivo ? 0 : ENTRADA;
    let visible = true;
    let perdido = false;
    let alza = 0;
    let alzaDestino = 0;
    const cursor = { x: 3, z: 0 };
    const cursorDestino = { x: 3, z: 0 };
    const pulsos: { x: number; z: number; edad: number }[] = [];
    const datosPulsos = new Float32Array(MAX_PULSOS * 3);

    const dibujar = () => {
      if (perdido) return;
      const avance = Math.min(1, entrada / ENTRADA);
      gl.uniform1f(variables.tiempo, tiempo);
      gl.uniform1f(variables.entrada, 1 - Math.pow(1 - avance, 3));
      gl.uniform1f(variables.aspecto, canvas.width / Math.max(1, canvas.height));
      gl.uniform1f(variables.alza, alza);
      gl.uniform2f(variables.cursor, cursor.x, cursor.z);
      for (let i = 0; i < MAX_PULSOS; i++) {
        const p = pulsos[i];
        datosPulsos.set(p ? [p.x, p.z, p.edad] : [0, 0, -1], i * 3);
      }
      gl.uniform3fv(variables.pulsos, datosPulsos);
      gl.clear(gl.COLOR_BUFFER_BIT | gl.DEPTH_BUFFER_BIT);
      gl.drawElements(gl.TRIANGLES, indices.length, gl.UNSIGNED_INT, 0);
    };

    const ajustarTamano = () => {
      const escala = Math.min(window.devicePixelRatio || 1, 1.5) * calidad;
      const ancho = Math.round(canvas.clientWidth * escala);
      const alto = Math.round(canvas.clientHeight * escala);
      if (!ancho || !alto) return false;
      if (canvas.width !== ancho || canvas.height !== alto) {
        canvas.width = ancho;
        canvas.height = alto;
        gl.viewport(0, 0, ancho, alto);
        gl.uniform1f(variables.grosor, Math.max(0.65, 0.75 * escala));
      }
      return true;
    };

    /** En un equipo lento primero baja la resolución y, si no alcanza, deja el terreno quieto. */
    const medir = (ms: number) => {
      sumaMs += Math.min(ms, 250);
      if (++medidos < CUADROS_MEDIDOS) return;
      const lento = sumaMs / medidos > MILISEGUNDOS_LENTO;
      medidos = 0;
      sumaMs = 0;
      if (!lento) return;
      if (calidad === 1) {
        calidad = 0.65;
        ajustarTamano();
      } else if (entrada >= ENTRADA) {
        detenido = true;
        alza = 0;
        pulsos.length = 0;
      }
    };

    const paso = (ahora: number) => {
      const dt = Math.min(0.05, anterior ? (ahora - anterior) / 1000 : 0.016);
      if (anterior) medir(ahora - anterior);
      anterior = ahora;
      tiempo += dt;
      entrada += dt;
      const suave = 1 - Math.exp(-dt * 5);
      cursor.x += (cursorDestino.x - cursor.x) * suave;
      cursor.z += (cursorDestino.z - cursor.z) * suave;
      alza += (alzaDestino - alza) * suave;
      for (const p of pulsos) p.edad += dt;
      while (pulsos.length && pulsos[0].edad > VIDA_PULSO) pulsos.shift();
      dibujar();
      if (!detenido) cuadro = requestAnimationFrame(paso);
    };

    const reanudar = () => {
      cancelAnimationFrame(cuadro);
      anterior = 0;
      if (vivo && visible && !perdido && !detenido && !document.hidden) cuadro = requestAnimationFrame(paso);
    };

    /** Punto del suelo que queda bajo el puntero. */
    const suelo = (e: PointerEvent) => {
      const r = canvas.getBoundingClientRect();
      const nx = ((e.clientX - r.left) / r.width) * 2 - 1;
      const ny = 1 - ((e.clientY - r.top) / r.height) * 2;
      const vx = (nx * (r.width / r.height)) / foco;
      const vy = ny / foco;
      const dirY = vy * coseno - seno;
      const dirZ = -vy * seno - coseno;
      const alcance = dirY < -0.12 ? Math.min(14, (OJO_Y - 0.6) / -dirY) : 14;
      return { x: vx * alcance, z: OJO_Z + dirZ * alcance };
    };

    const mover = (e: PointerEvent) => {
      const p = suelo(e);
      if (alzaDestino === 0 && alza < 0.02) Object.assign(cursor, p);
      Object.assign(cursorDestino, p);
      alzaDestino = 1;
    };
    const salir = () => {
      alzaDestino = 0;
    };
    const pulsar = (e: PointerEvent) => {
      if (e.button !== 0 || (e.target as Element).closest('a, button')) return;
      if (pulsos.length === MAX_PULSOS) pulsos.shift();
      pulsos.push({ ...suelo(e), edad: 0 });
    };
    const perder = (e: Event) => {
      e.preventDefault();
      perdido = true;
      cancelAnimationFrame(cuadro);
      setListo(false);
    };

    const tamano = new ResizeObserver(() => {
      if (!ajustarTamano()) return;
      dibujar();
      setListo(!perdido);
    });
    tamano.observe(canvas);

    const vista = new IntersectionObserver(([entradaVista]) => {
      visible = entradaVista.isIntersecting;
      reanudar();
    });
    vista.observe(canvas);

    canvas.addEventListener('webglcontextlost', perder);
    document.addEventListener('visibilitychange', reanudar);
    if (vivo) {
      zona.addEventListener('pointermove', mover);
      zona.addEventListener('pointerleave', salir);
      zona.addEventListener('pointerdown', pulsar);
    }

    return () => {
      cancelAnimationFrame(cuadro);
      tamano.disconnect();
      vista.disconnect();
      canvas.removeEventListener('webglcontextlost', perder);
      document.removeEventListener('visibilitychange', reanudar);
      zona.removeEventListener('pointermove', mover);
      zona.removeEventListener('pointerleave', salir);
      zona.removeEventListener('pointerdown', pulsar);
      gl.deleteBuffer(bufferPuntos);
      gl.deleteBuffer(bufferIndices);
      gl.deleteProgram(programa);
    };
  }, []);

  return (
    <div aria-hidden="true" className={`pointer-events-none absolute inset-0 ${className}`}>
      <div className={`absolute inset-0 transition-opacity duration-700 ${listo ? 'opacity-0' : 'opacity-100'}`}>
        {respaldo}
      </div>
      <canvas
        ref={lienzo}
        className={`absolute inset-0 h-full w-full transition-opacity duration-700 ${
          listo ? 'opacity-100' : 'opacity-0'
        }`}
      />
    </div>
  );
}
