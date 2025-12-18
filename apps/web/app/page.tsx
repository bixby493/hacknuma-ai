export default function Home() {
  return (
    <main style={styles.main}>
      <div style={styles.card}>
        <h1>Hacknuma AI 🚀</h1>
        <p>Script → Scene → Voice → Video</p>

        <a href="/generate">
          <button style={styles.button}>Generate Video</button>
        </a>
      </div>
    </main>
  );
}

const styles = {
  main: {
    minHeight: "100vh",
    display: "flex",
    justifyContent: "center",
    alignItems: "center",
  },
  card: {
    background: "#111",
    padding: 40,
    borderRadius: 16,
    textAlign: "center",
    boxShadow: "0 0 40px rgba(0,255,0,0.2)",
  },
  button: {
    marginTop: 20,
    padding: "14px 32px",
    background: "#00c853",
    color: "#000",
    border: "none",
    borderRadius: 10,
    fontWeight: "bold",
    cursor: "pointer",
  },
};
