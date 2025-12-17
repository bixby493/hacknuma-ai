'use client';
import { useState } from 'react';

export default function Generate() {
  const [script, setScript] = useState('');
  const [msg, setMsg] = useState('');

  async function submit() {
    setMsg('Job submitted (backend required)');
  }

  return (
    <main style={{minHeight:'100vh',background:'#000',color:'#fff',padding:24}}>
      <h2>Script → Video</h2>
      <textarea value={script} onChange={e=>setScript(e.target.value)} style={{width:'100%',height:160}} />
      <br/><br/>
      <button onClick={submit}>Generate Video</button>
      <p>{msg}</p>
    </main>
  );
}