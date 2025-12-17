const express = require('express');
const app = express();
app.use(express.json());

app.post('/jobs', (req, res) => {
  res.json({ job_id: 'job_demo' });
});

app.get('/jobs/:id', (req, res) => {
  res.json({ stage: 'idle' });
});

app.listen(3001, () => console.log('API running on :3001'));