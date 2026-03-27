const DEFAULT_MODEL = 'llama-3.3-70b-versatile';
const DEFAULT_SYSTEM_PROMPT = 'You are Hacknuma AI - a powerful assistant like Jarvis.';

function toSafeString(value, fallback = '') {
  return typeof value === 'string' ? value.trim() : fallback;
}

function buildGroqMessages(body, userMessage) {
  const systemPrompt = toSafeString(body.system, DEFAULT_SYSTEM_PROMPT) || DEFAULT_SYSTEM_PROMPT;
  const messages = [{ role: 'system', content: systemPrompt }];
  const history = Array.isArray(body.history) ? body.history.slice(-20) : [];

  history.forEach(item => {
    if (!item || typeof item !== 'object') return;
    const role = item.role === 'user' || item.role === 'assistant' ? item.role : '';
    const content = toSafeString(item.content);
    if (role && content) {
      messages.push({ role, content });
    }
  });

  if (!history.length) {
    messages.push({ role: 'user', content: userMessage });
  }

  return messages;
}

module.exports = async (req, res) => {
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Access-Control-Allow-Methods', 'POST, OPTIONS');
  res.setHeader('Access-Control-Allow-Headers', 'Content-Type');

  if (req.method === 'OPTIONS') {
    return res.status(204).end();
  }

  if (req.method !== 'POST') {
    return res.status(405).json({ error: 'Method not allowed' });
  }

  try {
    const API_KEY = process.env.API_KEY;
    if (!API_KEY) {
      return res.status(500).json({ error: 'API_KEY not configured in Vercel env' });
    }

    const body = req.body && typeof req.body === 'object' ? req.body : {};
    const userMessage = toSafeString(body.message);
    if (!userMessage) {
      return res.status(400).json({ error: 'Message required' });
    }

    const model = toSafeString(body.model, DEFAULT_MODEL) || DEFAULT_MODEL;
    const maxTokensRaw = Number(body.max_tokens);
    const maxTokens = Number.isFinite(maxTokensRaw) ? Math.min(Math.max(maxTokensRaw, 256), 8192) : 4096;
    const temperatureRaw = Number(body.temperature);
    const temperature = Number.isFinite(temperatureRaw) ? Math.min(Math.max(temperatureRaw, 0), 2) : 0.7;
    const messages = buildGroqMessages(body, userMessage);

    const groqRes = await fetch('https://api.groq.com/openai/v1/chat/completions', {
      method: 'POST',
      headers: {
        Authorization: `Bearer ${API_KEY}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        model,
        messages,
        max_tokens: maxTokens,
        temperature
      })
    });

    const data = await groqRes.json();
    if (!groqRes.ok) {
      const apiError = data?.error?.message || `Groq API error (${groqRes.status})`;
      return res.status(groqRes.status).json({ error: apiError });
    }

    const reply = data?.choices?.[0]?.message?.content || 'No response';
    return res.status(200).json({ reply });
  } catch (error) {
    return res.status(500).json({ error: 'Server error' });
  }
};
