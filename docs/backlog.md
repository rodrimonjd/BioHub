# Historias de usuario comprometidas (Entrega 2) — 93 puntos

Equipo de 3 personas: **Juan David Rodriguez** y **Raul Escobar** (backend) y **Sebastian Garcia** (aplicación web).

- **Responsable** es quien implementa el servicio (backend).
- **Interfaz web** indica que la historia tiene pantalla en `services/web-app`, hecha por Sebastian Garcia.

| ID | Historia | ASR | Pts | Servicio | Responsable | Interfaz web |
|---|---|---|---|---|---|---|
| HU-01 | Registrar hallazgos biológicos | ASR-06, 09 | 5 | registro | Juan David Rodriguez | Sebastian Garcia |
| HU-02.1 | Crear fichas taxonómicas | ASR-08 | 5 | taxonomía | Juan David Rodriguez | Sebastian Garcia |
| HU-03 | Registro de evidencias | ASR-08 | 5 | registro | Juan David Rodriguez | Sebastian Garcia |
| HU-05 | Consultar información de hallazgos | ASR-01, 02 | 5 | consulta pública | Raul Escobar | Sebastian Garcia |
| HU-06 | Consultar ubicación de hallazgos | ASR-04 | 3 | consulta pública | Raul Escobar | Sebastian Garcia |
| HU-07 | Consultar coordenadas exactas | ASR-04, 05 | 3 | consulta pública / autorización | Raul Escobar | Sebastian Garcia |
| HU-08.2 | Validar permisos de acceso | ASR-05 | 5 | autorización | Raul Escobar | Sebastian Garcia |
| HU-09 | Consultar historial de modificaciones | ASR-06, 07 | 5 | historial | Raul Escobar | Sebastian Garcia |
| HU-10 | Identificar responsable de modificaciones | ASR-06 | 3 | registro / historial | Juan David Rodriguez | Sebastian Garcia |
| HU-11.1 | Autenticar consumidores | ASR-03, 05 | 5 | gateway / autorización | Juan David Rodriguez | — |
| HU-11.2 | Consultar información mediante acceso externo | ASR-02, 03 | 5 | gateway | Juan David Rodriguez | — |
| HU-12.2 | Generar reportes científicos | ASR-09 | 8 | reportes | Juan David Rodriguez | Sebastian Garcia |
| HU-13 | Registrar avistamientos diarios | ASR-09 | 5 | registro | Juan David Rodriguez | Sebastian Garcia |
| HU-14.1 | Garantizar consultas públicas en menos de 1 s | ASR-01, 02 | 5 | consulta pública / gateway | Raul Escobar | — |
| HU-16 | Control de solicitudes por consumidor externo | ASR-03 | 5 | gateway | Juan David Rodriguez | Sebastian Garcia |
| HU-19.1 | Registrar modificaciones de los hallazgos | ASR-06 | 5 | registro / historial | Juan David Rodriguez | Sebastian Garcia |
| HU-20.1 | Conservar versiones anteriores | ASR-07 | 8 | historial | Raul Escobar | Sebastian Garcia |
| HU-21.1 | Agregar nuevos atributos taxonómicos | ASR-08 | 8 | taxonomía | Juan David Rodriguez | — |

Totales de backend: Juan David Rodriguez 59 · Raul Escobar 34 (total 93).

## Reparto por servicio
| Servicio | Responsable |
|---|---|
| `servicio-registro` | Juan David Rodriguez |
| `servicio-taxonomia` | Juan David Rodriguez |
| `api-gateway` | Juan David Rodriguez |
| `servicio-reportes` | Juan David Rodriguez |
| `servicio-autorizacion` | Raul Escobar |
| `servicio-consulta-publica` | Raul Escobar |
| `servicio-historial` | Raul Escobar |
| `web-app` | Sebastian Garcia |

## Etiquetas sugeridas en GitHub
`asr-01` … `asr-09`, `historia`, `tarea`, y una por servicio: `gateway`, `autorizacion`, `registro`, `taxonomia`, `consulta-publica`, `historial`, `reportes`, `web-app`, `infra`.
