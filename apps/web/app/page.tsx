export default function Home() {
  return (
    <main
      style={{
        minHeight: "100vh",
        display: "flex",
        justifyContent: "center",
        alignItems: "center",
        background: "linear-gradient(135deg,#000,#0f1f0f)",
      }}
    >
      <div
        style={{
          background: "#111",
          padding: 40,
          borderRadius: 16,
          textAlign: "center",
          boxShadow: "0 0 40px rgba(0,255,0,0.25)",
          maxWidth: 600,
        }}
      >
        <h1 style={{ fontSize: 40 }}>Hacknuma AI 🚀</h1>
        <p style={{ color: "#aaa", marginBottom: 30 }}>
          Script → Scene → Motion → Voice → Video
        </p>

        <a href="/generate">
          <button
            style={{
              padding: "14px 36px",
              background: "#00c853",
              border: "none",
              borderRadius: 10,
              fontSize: 18,
              fontWeight: "bold",
              cursor: "pointer",
            }}
          >
            Generate Video
          </button>
        </a>
      </div>
    </main>
  );
}
