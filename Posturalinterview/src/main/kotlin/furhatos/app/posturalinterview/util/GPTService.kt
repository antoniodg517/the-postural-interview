package furhatos.app.posturalinterview.util

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.util.Properties
import java.util.concurrent.TimeUnit

/**
 * Servizio per l'integrazione con OpenAI GPT
 * Gestisce chiamate API per risposte contestuali e naturali
 */
object GPTService {
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()
    
    private val gson = Gson()
    private val apiKey: String
    private val model: String
    private val temperature: Double
    private val maxTokens: Int
    
    // Contesto conversazione per mantenere coerenza
    private val conversationHistory = mutableListOf<Message>()
    
    init {
        val props = Properties()
        val configFile = File("config/openai.properties")
        
        if (configFile.exists()) {
            configFile.inputStream().use { props.load(it) }
            apiKey = props.getProperty("openai.api.key")
            model = props.getProperty("openai.model", "gpt-4")
            temperature = props.getProperty("openai.temperature", "0.7").toDouble()
            maxTokens = props.getProperty("openai.max.tokens", "150").toInt()
            println("✓ GPT Service inizializzato con modello: $model")
        } else {
            throw IllegalStateException("File config/openai.properties non trovato")
        }
    }
    
    /**
     * Sistema prompt per il comportamento del robot intervistatore
     */
    private val systemPrompt = """
        Sei un robot intervistatore professionale di nome Furhat. 
        Conduci un colloquio di lavoro formale ma empatico.
        Le tue risposte devono essere:
        - Brevi (max 2-3 frasi)
        - Professionali ma umane
        - Focalizzate sul candidato
        - In italiano formale (dare del "lei")
        
        NON fare nuove domande, rispondi solo a quello che il candidato dice.
        NON ripetere quello che ha detto il candidato.
        Mostra interesse genuino e comprensione.
    """.trimIndent()
    
    /**
     * Genera risposta GPT basata su input utente
     */
    fun generateResponse(
        userInput: String, 
        context: String = "",
        useHistory: Boolean = true
    ): String? {
        return try {
            // Aggiungi contesto alla conversazione
            if (context.isNotEmpty() && conversationHistory.isEmpty()) {
                conversationHistory.add(Message("system", systemPrompt))
                conversationHistory.add(Message("system", "Contesto: $context"))
            }
            
            // Aggiungi messaggio utente
            conversationHistory.add(Message("user", userInput))
            
            // Prepara richiesta
            val messages = if (useHistory) {
                conversationHistory
            } else {
                listOf(
                    Message("system", systemPrompt),
                    Message("system", "Contesto: $context"),
                    Message("user", userInput)
                )
            }
            
            val requestBody = GPTRequest(
                model = model,
                messages = messages,
                temperature = temperature,
                maxTokens = maxTokens
            )
            
            val json = gson.toJson(requestBody)
            val body = json.toRequestBody("application/json".toMediaType())
            
            val request = Request.Builder()
                .url("https://api.openai.com/v1/chat/completions")
                .header("Authorization", "Bearer $apiKey")
                .header("Content-Type", "application/json")
                .post(body)
                .build()
            
            // Esegui chiamata
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()
            
            if (response.isSuccessful && responseBody != null) {
                val gptResponse = gson.fromJson(responseBody, GPTResponse::class.java)
                val assistantMessage = gptResponse.choices.firstOrNull()?.message?.content
                
                // Aggiungi risposta alla cronologia
                if (assistantMessage != null && useHistory) {
                    conversationHistory.add(Message("assistant", assistantMessage))
                }
                
                println("[GPT] Input: $userInput")
                println("[GPT] Response: $assistantMessage")
                
                assistantMessage
            } else {
                println("[GPT] ERROR: ${response.code} - $responseBody")
                null
            }
            
        } catch (e: Exception) {
            println("[GPT] Exception: ${e.message}")
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Genera follow-up naturale durante warmup
     */
    fun generateWarmupFollowup(userResponse: String, questionTopic: String): String {
        val context = "Fase warmup. Argomento domanda: $questionTopic. Genera un breve commento di transizione naturale."
        return generateResponse(userResponse, context, useHistory = false) 
            ?: listOf("Capisco.", "Interessante.", "Va bene.").random()
    }
    
    /**
     * Genera risposta a domanda del candidato nella decompression
     */
    fun generateDecompressionAnswer(candidateQuestion: String): String {
        val context = """
            Il candidato ha fatto una domanda alla fine del colloquio.
            Rispondi in modo professionale ma rassicurante.
            Se chiede informazioni sul processo, rispondi che riceverà comunicazioni via email.
            Se chiede feedback, sii positivo ma generico.
        """.trimIndent()
        
        return generateResponse(candidateQuestion, context, useHistory = true)
            ?: "Capisco. Tutte le informazioni le verranno comunicate via email nei prossimi giorni."
    }
    
    /**
     * Genera variazione di domanda challenge basata su CV
     */
    fun generateCompetencyChallenge(skill: String, experience: String): String {
        val context = """
            Genera UNA domanda di sfida sulla competenza "$skill".
            Il candidato ha esperienza in: $experience.
            La domanda deve essere:
            - Specifica e tecnica
            - Leggermente provocatoria (metti alla prova)
            - Massimo 2 frasi
            - Usa il "lei" formale
        """.trimIndent()
        
        return generateResponse(
            "Genera domanda di sfida su $skill", 
            context, 
            useHistory = false
        ) ?: "Mi descriva in dettaglio la sua esperienza con $skill. Come lo applica concretamente?"
    }
    
    /**
     * Reset cronologia conversazione (per nuova sessione)
     */
    fun resetConversation() {
        conversationHistory.clear()
        println("[GPT] Cronologia conversazione resettata")
    }
    
    // Data classes per API OpenAI
    private data class GPTRequest(
        val model: String,
        val messages: List<Message>,
        val temperature: Double,
        @SerializedName("max_tokens") val maxTokens: Int
    )
    
    private data class Message(
        val role: String,  // "system", "user", "assistant"
        val content: String
    )
    
    private data class GPTResponse(
        val choices: List<Choice>
    )
    
    private data class Choice(
        val message: Message
    )
}
