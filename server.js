import express from "express";
import fetch from "node-fetch";
import cors from "cors";

const app = express();
app.use(cors());
app.use(express.json());

const API_KEY = "PUT_YOUR_GROQ_API_KEY_HERE";

app.post("/chat", async (req, res) => {
    try {
        const userMsg = req.body.message;

        const response = await fetch("https://api.groq.com/openai/v1/chat/completions", {
            method: "POST",
            headers: {
                "Authorization": `Bearer ${API_KEY}`,
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                model: "llama3-70b-8192",
                messages: [
                    { role: "system", content: "You are a helpful AI assistant like Jarvis." },
                    { role: "user", content: userMsg }
                ]
            })
        });

        const data = await response.json();
        res.json(data);

    } catch (err) {
        res.status(500).json({ error: "Something went wrong" });
    }
});

app.listen(3000, () => console.log("Server running on http://localhost:3000"));
