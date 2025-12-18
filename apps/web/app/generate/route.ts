import { NextResponse } from "next/server";

export async function POST() {
  return NextResponse.json({
    success: true,
    jobId: Math.floor(Math.random() * 100000),
  });
}
