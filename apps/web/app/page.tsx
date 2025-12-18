export default function Home() {
  return (
    <main style={{ minHeight: "100vh", display: "grid", placeItems: "center" }}>
      <div style={{ textAlign: "center" }}>
        <h1>Hacknuma AI 🚀</h1>
        <a href="/generate">
          <button>Generate Video</button>
        </a>
      </div>
    </main>
  );
}
