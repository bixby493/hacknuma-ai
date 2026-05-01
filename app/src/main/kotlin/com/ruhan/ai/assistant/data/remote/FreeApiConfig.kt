package com.ruhan.ai.assistant.data.remote

/**
 * Free API providers for RUHAN AI
 * Reference: https://github.com/cheahjs/free-llm-api-resources
 *
 * All these providers offer free tier API access.
 * Users can get their own API keys from the links below.
 */
object FreeApiConfig {

    // ==========================================
    // FREE LLM (Chat/Command) PROVIDERS
    // ==========================================

    /**
     * GROQ — Free tier: 30 RPM, 14.4K RPD for llama-3.1-8b
     * Sign up: https://console.groq.com/keys
     * Models: llama-3.3-70b-versatile, llama-3.1-8b-instant, gemma2-9b-it
     */
    const val GROQ_BASE_URL = "https://api.groq.com/openai/v1/"
    const val GROQ_FREE_MODEL = "llama-3.1-8b-instant"
    const val GROQ_PREMIUM_MODEL = "llama-3.3-70b-versatile"
    const val GROQ_SIGNUP_URL = "https://console.groq.com/keys"

    /**
     * GOOGLE GEMINI — Free tier: 15 RPM, 1M TPM, 1500 RPD
     * Sign up: https://aistudio.google.com/apikey
     * Models: gemini-2.0-flash, gemini-1.5-flash, gemini-1.5-pro
     */
    const val GEMINI_BASE_URL = "https://generativelanguage.googleapis.com/v1beta/"
    const val GEMINI_FREE_MODEL = "gemini-2.0-flash"
    const val GEMINI_VISION_MODEL = "gemini-2.0-flash"
    const val GEMINI_SIGNUP_URL = "https://aistudio.google.com/apikey"

    /**
     * NVIDIA NIM — Free: 1000 API calls/day for registered developers
     * Sign up: https://build.nvidia.com/
     * Models: meta/llama-3.1-8b-instruct, deepseek-ai/deepseek-v3
     */
    const val NVIDIA_BASE_URL = "https://integrate.api.nvidia.com/v1/"
    const val NVIDIA_FREE_MODEL = "meta/llama-3.1-8b-instruct"
    const val NVIDIA_SIGNUP_URL = "https://build.nvidia.com/"

    /**
     * OPENROUTER — Free models: 50 RPD (1000 RPD with $10 topup)
     * Sign up: https://openrouter.ai/keys
     * Free models: gemma-3-12b, llama-3.3-70b, mistral-small, phi-4
     */
    const val OPENROUTER_BASE_URL = "https://openrouter.ai/api/v1/"
    const val OPENROUTER_FREE_MODEL = "openrouter/free"
    const val OPENROUTER_SIGNUP_URL = "https://openrouter.ai/keys"

    /**
     * CEREBRAS — Free: 30 RPM, 1M TPM
     * Sign up: https://cloud.cerebras.ai/
     * Models: llama-3.3-70b, llama-3.1-8b
     */
    const val CEREBRAS_BASE_URL = "https://api.cerebras.ai/v1/"
    const val CEREBRAS_FREE_MODEL = "llama-3.3-70b"
    const val CEREBRAS_SIGNUP_URL = "https://cloud.cerebras.ai/"

    /**
     * MISTRAL — Free: 1 RPM (La Plateforme)
     * Sign up: https://console.mistral.ai/api-keys/
     * Free models: pixtral, mistral-small-latest
     */
    const val MISTRAL_BASE_URL = "https://api.mistral.ai/v1/"
    const val MISTRAL_FREE_MODEL = "mistral-small-latest"
    const val MISTRAL_SIGNUP_URL = "https://console.mistral.ai/api-keys/"

    /**
     * COHERE — Free: 20 RPM, 1000 RPM
     * Sign up: https://dashboard.cohere.com/api-keys
     * Models: command-r-plus, command-r
     */
    const val COHERE_BASE_URL = "https://api.cohere.ai/v1/"
    const val COHERE_FREE_MODEL = "command-r"
    const val COHERE_SIGNUP_URL = "https://dashboard.cohere.com/api-keys"

    /**
     * GITHUB MODELS — Free: 15 RPM, 150 RPD
     * Sign up: https://github.com/marketplace/models
     * Models: gpt-4o-mini, Phi-4, Llama-3.3-70B
     */
    const val GITHUB_MODELS_BASE_URL = "https://models.inference.ai.azure.com/"
    const val GITHUB_MODELS_FREE_MODEL = "gpt-4o-mini"
    const val GITHUB_MODELS_SIGNUP_URL = "https://github.com/marketplace/models"

    // ==========================================
    // FREE TTS (Text-to-Speech) PROVIDERS
    // ==========================================

    /**
     * HUGGINGFACE — Free inference API
     * Sign up: https://huggingface.co/settings/tokens
     * Models: facebook/mms-tts-hin (Hindi), facebook/mms-tts-urd (Urdu)
     */
    const val HF_BASE_URL = "https://api-inference.huggingface.co/"
    const val HF_TTS_MODEL = "facebook/mms-tts-hin"
    const val HF_SIGNUP_URL = "https://huggingface.co/settings/tokens"

    // ==========================================
    // FREE VISION (Image Analysis) PROVIDERS
    // ==========================================

    /**
     * Google Gemini Vision — Same API key as Gemini chat
     * Free tier supports image analysis
     */
    const val VISION_PROVIDER = "gemini"

    // ==========================================
    // FREE SEARCH API
    // ==========================================

    /**
     * TAVILY — Free: 1000 API calls/month
     * Sign up: https://tavily.com/#api
     */
    const val TAVILY_BASE_URL = "https://api.tavily.com/"
    const val TAVILY_SIGNUP_URL = "https://tavily.com/#api"

    // ==========================================
    // PROVIDER LIST FOR UI
    // ==========================================

    data class ApiProvider(
        val name: String,
        val description: String,
        val signupUrl: String,
        val baseUrl: String,
        val freeModel: String,
        val category: String,
        val freeTierLimit: String,
        val isFree: Boolean = true
    )

    val allProviders = listOf(
        ApiProvider(
            name = "Groq",
            description = "Fastest free LLM inference. Llama 3.1 8B instant responses.",
            signupUrl = GROQ_SIGNUP_URL,
            baseUrl = GROQ_BASE_URL,
            freeModel = GROQ_FREE_MODEL,
            category = "chat",
            freeTierLimit = "30 RPM, 14.4K requests/day"
        ),
        ApiProvider(
            name = "Google Gemini",
            description = "Gemini 2.0 Flash — best free model. Vision + chat in one key.",
            signupUrl = GEMINI_SIGNUP_URL,
            baseUrl = GEMINI_BASE_URL,
            freeModel = GEMINI_FREE_MODEL,
            category = "chat+vision",
            freeTierLimit = "15 RPM, 1500 requests/day, 1M tokens/min"
        ),
        ApiProvider(
            name = "NVIDIA NIM",
            description = "NVIDIA ke servers pe free inference. Llama, DeepSeek available.",
            signupUrl = NVIDIA_SIGNUP_URL,
            baseUrl = NVIDIA_BASE_URL,
            freeModel = NVIDIA_FREE_MODEL,
            category = "chat",
            freeTierLimit = "1000 API calls/day"
        ),
        ApiProvider(
            name = "OpenRouter",
            description = "Multiple free models via one API key. Auto-selects best model.",
            signupUrl = OPENROUTER_SIGNUP_URL,
            baseUrl = OPENROUTER_BASE_URL,
            freeModel = OPENROUTER_FREE_MODEL,
            category = "chat",
            freeTierLimit = "50 requests/day (1000 with $10 topup)"
        ),
        ApiProvider(
            name = "Cerebras",
            description = "Ultra-fast inference on Cerebras hardware. Llama 3.3 70B free.",
            signupUrl = CEREBRAS_SIGNUP_URL,
            baseUrl = CEREBRAS_BASE_URL,
            freeModel = CEREBRAS_FREE_MODEL,
            category = "chat",
            freeTierLimit = "30 RPM, 1M tokens/min"
        ),
        ApiProvider(
            name = "Mistral",
            description = "French AI company. Mistral Small free for developers.",
            signupUrl = MISTRAL_SIGNUP_URL,
            baseUrl = MISTRAL_BASE_URL,
            freeModel = MISTRAL_FREE_MODEL,
            category = "chat",
            freeTierLimit = "1 RPM free tier"
        ),
        ApiProvider(
            name = "Cohere",
            description = "Command-R models for chat. Generous free tier.",
            signupUrl = COHERE_SIGNUP_URL,
            baseUrl = COHERE_BASE_URL,
            freeModel = COHERE_FREE_MODEL,
            category = "chat",
            freeTierLimit = "20 RPM, 1000 RPM"
        ),
        ApiProvider(
            name = "GitHub Models",
            description = "Free AI models via GitHub account. GPT-4o-mini, Phi-4.",
            signupUrl = GITHUB_MODELS_SIGNUP_URL,
            baseUrl = GITHUB_MODELS_BASE_URL,
            freeModel = GITHUB_MODELS_FREE_MODEL,
            category = "chat",
            freeTierLimit = "15 RPM, 150 requests/day"
        ),
        ApiProvider(
            name = "HuggingFace",
            description = "Free Hindi TTS (text-to-speech). Ruhan ki awaaz yahan se aati hai.",
            signupUrl = HF_SIGNUP_URL,
            baseUrl = HF_BASE_URL,
            freeModel = HF_TTS_MODEL,
            category = "tts",
            freeTierLimit = "Free inference API (rate limited)"
        ),
        ApiProvider(
            name = "Tavily",
            description = "AI-powered web search. 1000 free searches/month.",
            signupUrl = TAVILY_SIGNUP_URL,
            baseUrl = TAVILY_BASE_URL,
            freeModel = "search",
            category = "search",
            freeTierLimit = "1000 API calls/month"
        )
    )

    /**
     * GitHub repo with all free API resources (reference)
     * https://github.com/cheahjs/free-llm-api-resources
     */
    const val FREE_API_RESOURCES_REPO = "https://github.com/cheahjs/free-llm-api-resources"
}
