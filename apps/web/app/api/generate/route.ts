import { NextResponse } from "next/server";

export async function POST(request: Request) {
  const body = await request.json().catch(() => ({}));

  return NextResponse.json({
    success: true,
    jobId: Math.floor(Math.random() * 100000),
    receivedScript: body?.script || null,
    message: "Video generation started (demo)",
  });
}

