import "./globals.css";

export const metadata = {
  title: "Hacknuma AI",
  description: "AI Video Generator",
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="en">
      <body>{children}</body>
    </html>
  );
}
