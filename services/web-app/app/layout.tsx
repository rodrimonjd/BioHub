import type { Metadata } from 'next';
import { Fraunces, Inter, JetBrains_Mono } from 'next/font/google';
import { NotificacionesProvider } from '@/components/ui/Notificaciones';
import './globals.css';

const fraunces = Fraunces({
  subsets: ['latin'],
  variable: '--font-fraunces',
  display: 'swap',
});

const inter = Inter({
  subsets: ['latin'],
  variable: '--font-inter',
  display: 'swap',
});

const jetbrainsMono = JetBrains_Mono({
  subsets: ['latin'],
  variable: '--font-jetbrains-mono',
  display: 'swap',
});

export const metadata: Metadata = {
  title: 'BioHub — Sistema Nacional de Registro y Vigilancia de Biodiversidad',
  description:
    'Plataforma científica del Instituto Nacional de Biodiversidad para centralizar, proteger y divulgar el inventario nacional de flora y fauna.',
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html
      lang="es"
      className={`${fraunces.variable} ${inter.variable} ${jetbrainsMono.variable}`}
    >
      <body className="min-h-screen bg-paper text-ink antialiased selection:bg-forest-700 selection:text-white">
        <NotificacionesProvider>{children}</NotificacionesProvider>
      </body>
    </html>
  );
}
