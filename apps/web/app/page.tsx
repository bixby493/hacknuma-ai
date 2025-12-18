"use client";

import { useState } from "react";

export default function GeneratePage() {
  const [status, setStatus] = useState("");

  return (
    <main
      style={{
        minHeight: "100vh",
        background: "linear-gradient(135deg, #000000, #0f1f0f)",
        display: "flex",
        alignItems: "center",
        justifyContent: "center",
        fontFamily: "Arial, sans-serif",
      }}
    >
      <div
        style={{
          background: "#111",
          padding: "40px",
          borderRadius: "16px",
          width: "100%",
          maxWidth: "700px",
          boxShadow: "0 0 40px rgba(0,255,0,0.2)",
        }}
      >
        <h1 style={{ color: "#fff", marginBottom: "8px" }}>
          Generate Video 🎬
        </h1>

        <p style={{ color: "#aaa", marginBottom: "20px" }}>
          Paste your script below
        </p>

        <textarea
          placeholder="Write your video script here..."
          style={{
            width: "100%",
            height: "200px",
            padding: "16px",
            borderRadius: "10px",
            border: "none",
            outline: "none",
            fontSize: "16px",
            marginBottom: "20px",
          }}
        />

        <button
          onClick={() => {
            setStatus("⏳ Generating video...");
            setTimeout(() => {
              setStatus("✅ Video generated (demo). Backend coming soon.");
            }, 2000);
          }}
          style={{
            padding: "14px 32px",
            background: "#00c853",
            color: "#000",
            fontWeight: "bold",
            border: "none",
            borderRadius: "10px",
            cursor: "pointer",
            fontSize: "16px",
          }}
        >
          Generate
        </button>

        {status && (
          <p style={{ marginTop: "16px", color: "#9f9" }}>
            {status}
          </p>
        )}
      </div>
    </main>
  );
}
