import { test, expect } from '@playwright/test';

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

  test('2. Login por rol de Investigador y registro de avistamiento (con estado local si el backend está apagado)', async ({
    page,
  }) => {
    await page.goto('/login');
    await expect(page.getByText(/Modo demo/i)).toBeVisible();

    await page
      .getByRole('radio', { name: /Sebastián García/i })
      .click();
    await page
      .getByRole('button', { name: /Continuar al panel científico/i })
      .click();

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

  test('3. Coordenadas ocultas para visitante común y visibles para personal de alto rango', async ({
    page,
  }) => {
    await page.goto('/login');
    await page.getByRole('radio', { name: /Visitante público/i }).click();
    await page
      .getByRole('button', { name: /Continuar al panel científico/i })
      .click();

    await expect(page).toHaveURL(/\/panel/);

    // Como visitante común ve la región difuminada y las coordenadas ocultas
    await expect(
      page.getByText(/Región: Meta \(Polígono regional difuminado\)/i)
    ).toBeVisible();
    await expect(
      page.getByText(/Coordenadas exactas ocultas al público general/i)
    ).toBeVisible();

    // Al cambiar la vista local "Ver tarjeta como" a Alto rango, se revelan las coordenadas exactas sin cambiar la sesión global
    await page.getByRole('button', { name: 'Alto rango' }).click();
    await expect(
      page.getByText(/Coordenadas exactas verificadas/i)
    ).toBeVisible();
  });
});
