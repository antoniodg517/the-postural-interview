package furhatos.app.posturalinterview.data

import com.google.gson.Gson
import java.io.File

/**
 * Data class per rappresentare il CV del partecipante
 */
data class CV(
    val participantId: String,
    val personalInfo: PersonalInfo,
    val education: List<Education>,
    val workExperience: List<WorkExperience>,
    val skills: Skills,
    val projects: List<Project>? = null
)

data class PersonalInfo(
    val name: String,
    val surname: String,
    val age: Int? = null
)

data class Education(
    val degree: String,
    val field: String,
    val institution: String,
    val year: Int
)

data class WorkExperience(
    val role: String,
    val company: String,
    val duration: String,
    val description: String
)

data class Skills(
    val technical: List<String>,
    val languages: List<Language>,
    val soft: List<String>
)

data class Language(
    val name: String,
    val level: String
)

data class Project(
    val name: String,
    val description: String,
    val technologies: List<String>
)

/**
 * Loader per caricare CV da file JSON
 */
object CVLoader {
    private val gson = Gson()
    
    /**
     * Carica CV da file JSON
     */
    fun loadCV(filePath: String): CV? {
        return try {
            val jsonContent = File(filePath).readText()
            gson.fromJson(jsonContent, CV::class.java)
        } catch (e: Exception) {
            println("ERROR: Impossibile caricare CV da $filePath")
            println("Errore: ${e.message}")
            null
        }
    }
    
    /**
     * Carica CV usando l'ID partecipante
     * Cerca il file in: cv/CV_{participantId}.json
     */
    fun loadCVByParticipantId(participantId: String): CV? {
        // Prova prima con percorso assoluto, poi relativo
        val paths = listOf(
            "${System.getProperty("user.dir")}/cv/CV_${participantId}.json",
            "cv/CV_${participantId}.json",
            "${System.getProperty("user.home")}/Desktop/Posturalinterview/cv/CV_${participantId}.json"
        )
        
        for (path in paths) {
            val file = File(path)
            if (file.exists()) {
                println("DEBUG: Trovato CV in: $path")
                return loadCV(path)
            }
        }
        
        println("DEBUG: CV non trovato nei percorsi:")
        paths.forEach { println("  - $it") }
        return null
    }
    
    /**
     * Seleziona una skill tecnica casuale per la sfida
     */
    fun getRandomTechnicalSkill(cv: CV): String? {
        return cv.skills.technical.randomOrNull()
    }
    
    /**
     * Ottiene l'ultima esperienza lavorativa
     */
    fun getLatestWorkExperience(cv: CV): WorkExperience? {
        return cv.workExperience.firstOrNull()
    }
    
    /**
     * Ottiene l'ultimo titolo di studio
     */
    fun getLatestEducation(cv: CV): Education? {
        return cv.education.firstOrNull()
    }
}
