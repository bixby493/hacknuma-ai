require("dotenv").config();

const express = require("express");
const fetch = require("node-fetch");
const cors = require("cors");

const app = express();

app.use(cors());
app.use(express.json());

// API KEY (secure)
const API_KEY = process.env.API_KEY;

// TEST ROUTE
app.get("/", (req, res) => {
    res.send("Hacknuma AI Backend Running 🚀");
});

// CHAT ROUTE
app.post("/chat", async (req, res) => {
    try {
        const userMessage = req.body.message;

        if (!userMessage) {
            return res.status(400).json({ error: "Message required" });
        }

        const response = await fetch("https://api.groq.com/openai/v1/chat/completions", {
            method: "POST",
            headers: {
                "Authorization": `Bearer ${API_KEY}`,
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                model: "llama3-70b-8192",
                messages: [
                    {
                        role: "system",
                        content: "You are Hacknuma AI — a powerful assistant like Jarvis."
                    },
                    {
                        role: "user",
                        content: userMessage
                    }
                ]
            })
        });

        const data = await response.json();

        res.json({
            reply: data.choices?.[0]?.message?.content || "No response"
        });

    } catch (error) {
        console.error(error);
        res.status(500).json({ error: "Server error" });
    }
});

// PORT
const PORT = process.env.PORT || 3000;

app.listen(PORT, () => {
    console.log(`Server running on http://localhost:${PORT}`);
});
