import type { Metadata } from 'next';
import './globals.css';

export const metadata: Metadata = {
  title: 'Astha Console - Fraud & Dispute Operations Platform',
  description: 'Real-time fraud operations and four-eyes dispute management platform for Bangladeshi MFS',
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="en" className="dark">
      <body className="min-h-screen bg-slate-950 text-slate-100">{children}</body>
    </html>
  );
}
