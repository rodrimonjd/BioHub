import { test, expect, type Page } from '@playwright/test';

test.describe('BioHub — Flujos principales e2e', () => {
  test('1. La página pública inicia sin sesión y redirige a /login para entrar al panel', async ({
    page,
  }) => {
    await page.goto('/');
    await expect(
      page.getByRole('heading', {
        name: 'Sistema Nacional de Registro y Vigilancia de Biodiversidad',
      })
    ).toBeVisible();
    await expect(
      page.getByRole('heading', {
        name: 'Por qué el inventario nacional exige una arquitectura especializada',
      })
    ).toBeVisible();

    // No debe haber jerga de desarrollo como HU-01 ni :8081
    await expect(page.getByText(/HU-\d+/i)).toHaveCount(0);
    await expect(page.getByText(/:8081/i)).toHaveCount(0);

    await page
      .getByRole('link', { name: /Iniciar sesión institucional/i })
      .click();
    await expect(page).toHaveURL(/\/login/);
  });

  test('2. Login con cuenta de investigador y registro de avistamiento (requiere servicio-autorizacion)', async ({
    page,
  }) => {
    await iniciarSesion(page, 'investigador.campo', 'BioHub-Invest-2026');

    await expect(page).toHaveURL(/\/panel/);
    await expect(
      page.getByRole('heading', { name: 'Operación científica de campo' })
    ).toBeVisible();

    // Registrar un avistamiento
    await page.getByLabel('Individuos').fill('5');
    await page.getByRole('button', { name: 'Guardar avistamiento' }).click();

    // Verifica que aparece en la bitácora (sincronizado o guardado solo en este equipo)
    await expect(page.getByText(/5 individuos/i).first()).toBeVisible();
  });

  test('3. Coordenadas según el permiso ubicacion:ver_exacta de la cuenta', async ({ page }) => {
    // La autoridad ambiental tiene ubicacion:ver_exacta
    await iniciarSesion(page, 'autoridad.ambiental', 'BioHub-Autoridad-2026');
    await expect(page).toHaveURL(/\/panel/);
    await expect(page.getByText(/Coordenadas exactas verificadas/i)).toBeVisible();
  });

  test('3b. Cerrar sesión vuelve a la página principal', async ({ page }) => {
    await iniciarSesion(page, 'investigador.campo', 'BioHub-Invest-2026');
    await expect(page).toHaveURL(/\/panel/);
    await page.getByRole('button', { name: /Cuenta de/ }).click();
    await page.getByRole('menuitem', { name: 'Cerrar sesión' }).click();
    await expect(page).toHaveURL('/');
  });

  test('4. El investigador no tiene ubicacion:ver_exacta y una contraseña incorrecta no entra', async ({
    page,
  }) => {
    await page.goto('/login');
    await page.getByLabel('Usuario o correo institucional').fill('investigador.campo');
    await page.getByLabel('Contraseña').fill('clave-incorrecta');
    await page.getByRole('button', { name: 'Iniciar sesión' }).click();
    // Next.js agrega su propio role="alert" vacío (__next-route-announcer__): se busca la alerta por su texto
    await expect(
      page.getByRole('alert').filter({ hasText: 'Usuario o contraseña incorrectos' })
    ).toBeVisible();
    await expect(page).toHaveURL(/\/login/);

    await iniciarSesion(page, 'investigador.campo', 'BioHub-Invest-2026');
    await expect(
      page.getByText(/Coordenadas exactas ocultas al público general/i)
    ).toBeVisible();
  });

  test('5. Login como invitado desde la página principal entra con permisos de ciudadano', async ({
    page,
  }) => {
    await page.goto('/');
    await page.getByRole('button', { name: 'Login como invitado' }).click();
    await expect(page).toHaveURL(/\/panel/);
    await expect(
      page.getByText(/Coordenadas exactas ocultas al público general/i)
    ).toBeVisible();

    // Sin hallazgo:crear no ve el formulario de la bitácora diaria
    await expect(page.getByText('Bitácora diaria de expedición')).toHaveCount(0);
    await expect(page.getByRole('button', { name: 'Guardar avistamiento' })).toHaveCount(0);
  });
});

async function iniciarSesion(page: Page, usuario: string, clave: string) {
  await page.goto('/login');
  await page.getByLabel('Usuario o correo institucional').fill(usuario);
  await page.getByLabel('Contraseña').fill(clave);
  await page.getByRole('button', { name: 'Iniciar sesión' }).click();
}
