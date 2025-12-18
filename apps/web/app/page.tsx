export default function Home() {
  return (
    <main
      style={{
        minHeight: "100vh",
        background: "linear-gradient(135deg, #0f0f0f, #1a1a1a)",
        display: "flex",
        alignItems: "center",
        justifyContent: "center",
        color: "#fff",
        fontFamily: "Arial, sans-serif",
      }}
    >
      <div
        style={{
          textAlign: "center",
          maxWidth: "600px",
          padding: "40px",
          borderRadius: "12px",
          background: "#111",
          boxShadow: "0 0 40px rgba(0,255,0,0.15)",
        }}
      >
        <h1 style={{ fontSize: "36px", marginBottom: "10px" }}>
          Hacknuma AI 🚀
        </h1>

        <p style={{ fontSize: "16px", color: "#aaa", marginBottom: "30px" }}>
          Script → Scene → Motion → Voice → Video
        </p>

        <a
          href="/generate"
          style={{
            display: "inline-block",
            padding: "14px 28px",
            background: "#00c853",
            color: "#000",
            textDecoration: "none",
            fontWeight: "bold",
            borderRadius: "8px",
          }}
        >
          Generate Video
        </a>

        <p style={{ marginTop: "20px", fontSize: "14px", color: "#555" }}>
          Deployment successful ✅
        </p>
      </div>
    </main>
  );
}
