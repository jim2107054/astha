import './globals.css';
import type { Metadata } from 'next';

export const metadata: Metadata = {
  title: 'Astha | Customer Dispute Portal',
  description: 'Bangladesh MFS Customer and Merchant Dispute Resolution Portal',
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="bn">
      <body className="min-h-screen bg-slate-950 text-slate-100 antialiased selection:bg-teal-500 selection:text-white">
        {children}
      </body>
    </html>
  );
}
