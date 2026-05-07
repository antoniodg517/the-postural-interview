package furhatos.app.posturalinterview.flow

import furhatos.app.posturalinterview.flow.main.Idle
import furhatos.app.posturalinterview.flow.main.Greeting
import furhatos.app.posturalinterview.setting.DISTANCE_TO_ENGAGE
import furhatos.app.posturalinterview.setting.MAX_NUMBER_OF_USERS
import furhatos.app.posturalinterview.setting.ITALIAN_VOICE
import furhatos.app.posturalinterview.util.EventLogger
import furhatos.app.posturalinterview.util.GPTService
import furhatos.app.posturalinterview.data.CVLoader
import furhatos.app.posturalinterview.data.CV
import furhatos.flow.kotlin.*
import furhatos.util.Language

// Variabile globale per memorizzare il CV del partecipante
var participantCV: CV? = null

val Init: State = state {
    init {
        /** Set our default interaction parameters */
        users.setSimpleEngagementPolicy(DISTANCE_TO_ENGAGE, MAX_NUMBER_OF_USERS)
        
        /** Configure speech for Italian */
        furhat.voice = ITALIAN_VOICE
    }
    
    onEntry {
        // Prova a prendere ID partecipante da system property, altrimenti usa P001 di default
        println("==============================================")
        println("POSTURAL INTERVIEW EXPERIMENT")
        println("==============================================")
        
        val participantId = System.getProperty("participantId") ?: "P001"
        println("Usando participant ID: $participantId")
        
        // Carica CV del partecipante
        println("Caricamento CV per partecipante $participantId...")
        participantCV = CVLoader.loadCVByParticipantId(participantId)
        
        if (participantCV != null) {
            println("✓ CV caricato correttamente")
            println("  Nome: ${participantCV!!.personalInfo.name} ${participantCV!!.personalInfo.surname}")
            println("  Skills tecniche: ${participantCV!!.skills.technical.joinToString(", ")}")
        } else {
            println("⚠ ATTENZIONE: CV non trovato. L'intervista userà domande generiche.")
            println("  Posiziona il CV in: cv/CV_${participantId}.json")
        }
        
        // Inizializza il logger della sessione
        EventLogger.startSession(participantId)
        
        // Reset cronologia GPT per nuova sessione
        GPTService.resetConversation()
        
        println("Sessione iniziata per partecipante: $participantId")
        println("Log salvato in: logs/postural_interview_${participantId}_*.csv")
        println("==============================================")
        
        /** start interaction */
        when {
            furhat.isVirtual() -> goto(Greeting) // Convenient to bypass the need for user when running Virtual Furhat
            users.hasAny() -> {
                furhat.attend(users.random)
                goto(Greeting)
            }
            else -> goto(Idle)
        }
    }

}
