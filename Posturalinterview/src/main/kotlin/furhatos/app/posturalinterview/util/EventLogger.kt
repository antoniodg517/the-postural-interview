package furhatos.app.posturalinterview.util

import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * EventLogger for synchronizing Furhat events with EMG recordings
 * Logs events with precise timestamps for post-hoc analysis
 */
object EventLogger {
    private var logFile: File? = null
    private var sessionStartTime: Long = 0
    private var participantId: String = ""
    
    /**
     * Initialize a new session with participant ID
     */
    fun startSession(participantId: String) {
        this.participantId = participantId
        sessionStartTime = System.currentTimeMillis()
        
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
        val fileName = "postural_interview_${participantId}_${timestamp}.csv"
        logFile = File("logs/$fileName")
        
        // Create logs directory if it doesn't exist
        logFile?.parentFile?.mkdirs()
        
        // Write CSV header
        logFile?.writeText("timestamp_ms,elapsed_ms,event_type,event_name,details\n")
        
        logEvent("SESSION", "START", "Participant: $participantId")
    }
    
    /**
     * Log an event with timestamp
     */
    fun logEvent(eventType: String, eventName: String, details: String = "") {
        val timestamp = System.currentTimeMillis()
        val elapsed = timestamp - sessionStartTime
        
        val logLine = "$timestamp,$elapsed,$eventType,$eventName,\"$details\"\n"
        logFile?.appendText(logLine)
        
        // Also print to console for real-time monitoring
        println("[EMG-SYNC] $elapsed ms | $eventType | $eventName | $details")
    }
    
    /**
     * End the current session
     */
    fun endSession() {
        logEvent("SESSION", "END", "Total duration: ${System.currentTimeMillis() - sessionStartTime} ms")
        println("Session log saved to: ${logFile?.absolutePath}")
    }
    
    // Convenience methods for specific event types
    fun logPhaseStart(phaseName: String) = logEvent("PHASE", "START", phaseName)
    fun logPhaseEnd(phaseName: String) = logEvent("PHASE", "END", phaseName)
    fun logQuestion(questionText: String) = logEvent("QUESTION", "ASKED", questionText)
    fun logResponse(responseText: String) = logEvent("RESPONSE", "RECEIVED", responseText)
    fun logStressor(stressorType: String, details: String = "") = logEvent("STRESSOR", stressorType, details)
    fun logSilenceStart(duration: Int) = logEvent("SILENCE", "START", "Duration: $duration seconds")
    fun logSilenceEnd() = logEvent("SILENCE", "END", "")
    fun logBaseline(type: String) = logEvent("BASELINE", type, "")
}
