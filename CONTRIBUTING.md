# Guía de contribución

## Ramas
- `main`: estable; solo recibe PRs. Cada entrega se marca con un release/tag (`entrega-2`, `entrega-3`).
- `develop`: integración del trabajo en curso.
- Ramas de trabajo desde `develop`: `feat/<servicio>-<descripcion>`, `fix/...`, `docs/...`.

## Commits (Conventional Commits)
`feat(registro): crea ficha taxonómica con atributos dinámicos`
Tipos: `feat`, `fix`, `docs`, `refactor`, `test`, `chore`.

## Pull Requests
1. PR hacia `develop` con la plantilla; enlaza la historia (`Closes #n`).
2. Mínimo **1 aprobación** de otro integrante.
3. Sin conflictos; borra la rama al hacer merge.

## Issues
Una issue por historia de usuario (ver `docs/backlog.md`), con las etiquetas del ASR y del servicio.

## Decisiones
Cambios a la arquitectura: un ADR en `docs/entrega-2-arquitectura/adr/` (copia `0000-plantilla.md`).
