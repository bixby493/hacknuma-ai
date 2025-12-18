"use client";

import { useSearchParams } from "next/navigation";

export default function ResultPage() {
  const params = useSearchParams();
  const jobId = params.get("job");

  return (
    <main style={styles.main}>
      <div style={styles.card}>
        <h1>🎉 Video Ready</h1>

        <p style={{ color: "#aaa" }}>
          Job ID: <strong>{jobId}</strong>
        </p>

        <div style={styles.videoBox}>
          <p style={{ color: "#666" }}>
            Video preview will appear here
          </p>
        </div>

        <button style={styles.button}>
          ⬇ Download Video
        </button>

        <p style={{ marginTop: 16, fontSize: 13, color: "#777" }}>
          * Demo mode – real video rendering coming soon
        </p>
      </div>
    </main>
  );
}

const styles: any = {
  main: {
    minHeight: "100vh",
    background: "linear-gradient(135deg, #000, #0f1f0f)",
    display: "flex",
    justifyContent: "center",
    alignItems: "center",
    color: "#fff",
    fontFamily: "Arial",
  },
  card: {
    background: "#111",
    padding: 40,
    borderRadius: 16,
    width: "100%",
    maxWidth: 600,
    textAlign: "center",
    boxShadow: "0 0 40px rgba(0,255,0,0.2)",
  },
  videoBox: {
    marginTop: 20,
    height: 220,
    borderRadius: 12,
    background: "#000",
    display: "flex",
    alignItems: "center",
    justifyContent: "center",
    border: "1px dashed #333",
  },
  button: {
    marginTop: 24,
    padding: "14px 32px",
    background: "#00c853",
    border: "none",
    borderRadius: 10,
    fontWeight: "bold",
    cursor: "pointer",
  },
};
