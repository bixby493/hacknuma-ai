"use client";
import { useState } from "react";

export default function Generate() {
  const [script, setScript] = useState("");
  const [status, setStatus] = useState("");

  function submit() {
    if (!script.trim()) {
      setStatus("❌ Please enter a script first");
      return;
    }
    setStatus("⏳ Processing... (backend coming soon)");
  }

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
          width: "600px",
          padding: "32px",
          background: "#111",
          borderRadius: "12px",
          boxShadow: "0 0 40px rgba(0,255,0,0.15)",
        }}
      >
        <h2 style={{ marginBottom: "10px" }}>Generate Video 🎬</h2>
        <p style={{ color: "#aaa", marginBottom: "16px" }}>
          Paste your script below
        </p>

        <textarea
          value={script}
          onChange={(e) => setScript(e.target.value)}
          placeholder="Write your video script here..."
          style={{
            width: "100%",
            height: "140px",
            padding: "12px",
            borderRadius: "8px",
            border: "none",
            outline: "none",
            resize: "none",
            fontSize: "14px",
          }}
        />

        <button
          onClick={submit}
          style={{
            marginTop: "16px",
            padding: "12px 24px",
            background: "#00c853",
            color: "#000",
            fontWeight: "bold",
            border: "none",
            borderRadius: "8px",
            cursor: "pointer",
          }}
        >
          Generate
        </button>

        {status && (
          <p style={{ marginTop: "16px", color: "#0f0" }}>{status}</p>
        )}
      </div>
    </main>
  );
}
