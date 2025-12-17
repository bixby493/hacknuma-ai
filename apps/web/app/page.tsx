export default function Home() {
  return (
    <main style={{minHeight:'100vh',display:'flex',alignItems:'center',justifyContent:'center',background:'#000',color:'#fff'}}>
      <div style={{textAlign:'center',maxWidth:720}}>
        <h1>Hacknuma AI</h1>
        <p>Script → Scene → Motion → Voice → MP4</p>
        <a href="/generate">Generate Video</a>
      </div>
    </main>
  );
}