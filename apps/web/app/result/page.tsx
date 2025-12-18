"use client";

import { useSearchParams } from "next/navigation";
import { useEffect, useState } from "react";

export default function ResultPage() {
  const searchParams = useSearchParams();
  const jobId = searchParams.get("job");

  const [progress, setProgress] = useState(10);
  const [status, setStatus] = useState("Processing...");

  useEffect(() => {
    let p = 10;

    const interval = setInterval(() => {
      p += 20;
      setProgress(p);

      if (p >= 100) {
        setStatus("✅ Video ready (demo)");
        clearInterval(interval);
      }
    }, 800);

    return () => clearInterval(interval);
  }, []);

  return (
    <main style={styles.main}>
      <div style={styles.card}>
        <h1>Video Result 🎥</h1>

        <p style={{ color: "#aaa" }}>
          Job ID: <b>{jobId}</b>
        </p>

        <div style={styles.progressWrap}>
          <div
            style={{
              ...styles.progressBar,
              width: `${progress}%`,
            }}
          />
        </div>

        <p style={{ marginTop: 16 }}>{status}</p>

        {progress >= 100 && (
          <div style={styles.preview}>
            <p>📽️ Preview (coming soon)</p>
            <div style={styles.fakeVideo}>Video Frame</div>
          </div>
        )}
      </div>
    </main>
  );
}

const styles: any = {
  main: {
    minHeight: "100vh",
    background: "linear-gradient(135deg, #000, #0f1f0f)",
    display: "flex",
    alignItems: "center",
    justifyContent: "center",
    color: "#fff",
    fontFamily: "Arial",
  },
  card: {
    background: "#111",
    padding: 40,
    borderRadius: 16,
    width: "100%",
    maxWidth: 600,
    boxShadow: "0 0 40px rgba(0,255,0,0.25)",
  },
  progressWrap: {
    marginTop: 16,
    height: 12,
    background: "#222",
    borderRadius: 6,
    overflow: "hidden",
  },
  progressBar: {
    height: "100%",
    background: "#00c853",
    transition: "width 0.4s ease",
  },
  preview: {
    marginTop: 24,
    padding: 20,
    background: "#000",
    borderRadius: 12,
    textAlign: "center",
  },
  fakeVideo: {
    marginTop: 10,
    height: 180,
    background: "#222",
    borderRadius: 8,
    display: "flex",
    alignItems: "center",
    justifyContent: "center",
    color: "#0f0",
  },
};
