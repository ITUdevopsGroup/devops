import "./globals.css";

export const metadata = {
  title: "Minitwit",
  description: "Next gen twitter",
};

export default function RootLayout({ children }) {
  return (
    <html lang="en">
      <body>{children}</body>
    </html>
  );
}
