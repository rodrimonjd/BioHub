export type UserRole = 'public' | 'researcher' | 'senior' | 'auditor' | 'director';

export interface BioHubUser {
  id: string;
  name: string;
  email: string;
  passwordHint: string;
  role: UserRole;
  roleLabel: string;
  institution: string;
  station: string;
  permissions: string[];
}

export const DEMO_USERS: BioHubUser[] = [
  {
    id: 'inv-garcia-01',
    name: 'Inv. Sebastián García',
    email: 'sgarcia@biohub.gov.co',
    passwordHint: 'biohub2026',
    role: 'researcher',
    roleLabel: 'Investigador de Campo',
    institution: 'Instituto Nacional de Biodiversidad',
    station: 'Estación Base Río Tomo — Orinoquía',
    permissions: [
      'REGISTRAR_AVISTAMIENTO',
      'REGISTRAR_HALLAZGO',
      'ADJUNTAR_EVIDENCIA',
      'MODIFICAR_HALLAZGO',
      'GESTIONAR_FICHAS',
    ],
  },
  {
    id: 'alt-escobar-02',
    name: 'Dra. Elena Escobar',
    email: 'eescobar@biohub.gov.co',
    passwordHint: 'biohub2026',
    role: 'senior',
    roleLabel: 'Personal Científico de Alto Rango',
    institution: 'Comité Científico Nacional BioHub',
    station: 'Dirección de Corredores Biológicos',
    permissions: [
      'VER_COORDENADAS_EXACTAS',
      'AUTORIZAR_RECLASIFICACION',
      'REGISTRAR_HALLAZGO',
      'MODIFICAR_HALLAZGO',
      'CONSULTAR_HISTORIAL',
    ],
  },
  {
    id: 'aud-bonilla-03',
    name: 'Sebastián Bonilla',
    email: 'sbonilla@biohub.gov.co',
    passwordHint: 'biohub2026',
    role: 'auditor',
    roleLabel: 'Auditor Científico',
    institution: 'Unidad de Trazabilidad Taxonómica',
    station: 'Archivo Central Bogotá',
    permissions: ['CONSULTAR_HISTORIAL', 'VER_COORDENADAS_EXACTAS'],
  },
  {
    id: 'dir-rodriguez-04',
    name: 'Dr. Juan David Rodríguez',
    email: 'jdrodriguez@biohub.gov.co',
    passwordHint: 'biohub2026',
    role: 'director',
    roleLabel: 'Director Científico',
    institution: 'Dirección General BioHub',
    station: 'Sede Central',
    permissions: [
      'VER_COORDENADAS_EXACTAS',
      'AUTORIZAR_RECLASIFICACION',
      'CONSULTAR_HISTORIAL',
      'GENERAR_REPORTES',
    ],
  },
  {
    id: 'vis-publico-00',
    name: 'Visitante Público (Sin sesión científica)',
    email: 'visitante@publico.co',
    passwordHint: '',
    role: 'public',
    roleLabel: 'Visitante Común',
    institution: 'Consulta Abierta Ciudadana',
    station: 'Ventana Pública',
    permissions: ['CONSULTAR_INVENTARIO'],
  },
];

/**
 * Validación centralizada de permisos para acceso a coordenadas exactas
 * de especies en peligro (ASR-04 / ASR-05 — El corredor biológico).
 */
export function canViewExactCoordinates(role: UserRole): boolean {
  return role === 'senior' || role === 'director' || role === 'auditor';
}

export function canRegisterFieldData(role: UserRole): boolean {
  return role === 'researcher' || role === 'senior' || role === 'director';
}

export function canAuthorizeReclassification(role: UserRole): boolean {
  return role === 'senior' || role === 'director';
}
