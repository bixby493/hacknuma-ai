"use client";
import { useSearchParams } from "next/navigation";
import { useEffect, useState } from "react";

export default function ResultPage() {
  const job = useSearchParams().get("job");
  const [progress, setProgress] = useState(10);

  useEffect(() => {
    const timer = setInterval(() => {
      setProgress(p => {
        if (p >= 100) {
          clearInterval(timer);
          return 100;
        }
        return p + 20;
      });
    }, 800);

    return () => clearInterval(timer);
  }, []);

  return (
    <main style={{minHeight:"100vh",display:"flex",justifyContent:"center",alignItems:"center"}}>
      <div className="card" style={{maxWidth:600,textAlign:"center"}}>
        <h2>Processing Video…</h2>
        <p>Job ID: {job}</p>

        <div style={{
          background:"#222",
          height:12,
          borderRadius:6,
          overflow:"hidden",
          margin:"24px 0"
        }}>
          <div style={{
            height:"100%",
            width:`${progress}%`,
            background:"#00c853"
          }} />
        </div>

        {progress === 100 ? (
          <>
            <p>✅ Video ready (demo)</p>
            <button className="btn" disabled>
              Download (coming soon)
            </button>
          </>
        ) : (
          <p>{progress}%</p>
        )}
      </div>
    </main>
  );
}
