import { NextResponse } from "next/server";

export async function POST(req: Request) {
  const body = await req.json();
  const { script } = body;

  if (!script || script.length < 10) {
    return NextResponse.json(
      { success: false, message: "Script too short" },
      { status: 400 }
    );
  }

  // fake job id (future me DB / queue)
  const jobId = Math.floor(10000 + Math.random() * 90000);

  return NextResponse.json({
    success: true,
    jobId,
    steps: [
      { label: "Analyzing script", progress: 10 },
      { label: "Creating scenes", progress: 40 },
      { label: "Generating voice", progress: 70 },
      { label: "Rendering video", progress: 100 }
    ]
  });
}
