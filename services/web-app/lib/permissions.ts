export type UserRole = 'public' | 'researcher' | 'senior' | 'auditor' | 'director';

export interface BioHubUser {
  id: string;
  name: string;
  initials: string;
  email: string;
  role: UserRole;
  roleLabel: string;
  institution: string;
  station: string;
}

export const DEMO_USERS: BioHubUser[] = [
  {
    id: 'inv-garcia-01',
    name: 'Sebastián García',
    initials: 'SG',
    email: 'investigador@biohub.gov.co',
    role: 'researcher',
    roleLabel: 'Investigador de campo',
    institution: 'Instituto Nacional de Biodiversidad',
    station: 'Estación Base Río Tomo — Orinoquía',
  },
  {
    id: 'alt-escobar-02',
    name: 'Dra. Elena Escobar',
    initials: 'EE',
    email: 'altorango@biohub.gov.co',
    role: 'senior',
    roleLabel: 'Personal científico de alto rango',
    institution: 'Comité Científico Nacional BioHub',
    station: 'Dirección de Corredores Biológicos',
  },
  {
    id: 'aud-bonilla-03',
    name: 'Sebastián Bonilla',
    initials: 'SB',
    email: 'auditor@biohub.gov.co',
    role: 'auditor',
    roleLabel: 'Auditor científico',
    institution: 'Unidad de Trazabilidad Taxonómica',
    station: 'Archivo Central Bogotá',
  },
  {
    id: 'dir-rodriguez-04',
    name: 'Dr. Juan D. Rodríguez',
    initials: 'JR',
    email: 'director@biohub.gov.co',
    role: 'director',
    roleLabel: 'Director científico',
    institution: 'Dirección General BioHub',
    station: 'Sede Central',
  },
  {
    id: 'vis-publico-00',
    name: 'Visitante público',
    initials: 'VP',
    email: 'consulta@biohub.gov.co',
    role: 'public',
    roleLabel: 'Visitante común',
    institution: 'Consulta Abierta Ciudadana',
    station: 'Ventana Pública',
  },
];

const SESSION_STORAGE_KEY = 'biohub_active_session_v1';

export function getStoredSession(): BioHubUser | null {
  if (typeof window === 'undefined') return null;
  try {
    const raw = window.localStorage.getItem(SESSION_STORAGE_KEY);
    if (!raw) return null;
    const parsed = JSON.parse(raw) as BioHubUser;
    const found = DEMO_USERS.find((u) => u.id === parsed.id);
    return found || parsed;
  } catch {
    return null;
  }
}

export function saveStoredSession(user: BioHubUser): void {
  if (typeof window === 'undefined') return;
  try {
    window.localStorage.setItem(SESSION_STORAGE_KEY, JSON.stringify(user));
  } catch {
    // ignore storage errors
  }
}

export function clearStoredSession(): void {
  if (typeof window === 'undefined') return;
  try {
    window.localStorage.removeItem(SESSION_STORAGE_KEY);
  } catch {
    // ignore storage errors
  }
}

/**
 * Validación centralizada de permisos para acceso a coordenadas exactas
 * de especies sensibles.
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
