import "./globals.css";

export const metadata = {
  title: "Hacknuma AI",
  description: "Script to Video AI",
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
