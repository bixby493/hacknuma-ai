export default function Home() {
  return (
    <main style={{
      minHeight: "100vh",
      display: "flex",
      alignItems: "center",
      justifyContent: "center"
    }}>
      <div className="card" style={{maxWidth: 700, textAlign: "center"}}>
        <h1 style={{fontSize: 42}}>
          Hacknuma AI 🚀
        </h1>

        <p style={{opacity: 0.85, marginBottom: 24}}>
          Script → Scene → Motion → Voice → Video
        </p>

        <div style={{marginBottom: 30}}>
          <p>✔ No editing</p>
          <p>✔ No complex tools</p>
          <p>✔ 10× faster videos</p>
        </div>

        <a href="/generate">
          <button className="btn">
            Create Video
          </button>
        </a>
      </div>
    </main>
  );
}
