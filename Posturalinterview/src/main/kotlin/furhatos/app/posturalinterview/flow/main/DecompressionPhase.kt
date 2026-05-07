package furhatos.app.posturalinterview.flow.main

import furhatos.app.posturalinterview.flow.Parent
import furhatos.app.posturalinterview.nlu.PhysicalSensation
import furhatos.app.posturalinterview.util.EventLogger
import furhatos.app.posturalinterview.util.GPTService
import furhatos.flow.kotlin.*
import furhatos.gestures.Gestures

/**
 * FASE 3: DECOMPRESSIONE (1-2 minuti)
 * Domande conclusive e rassicuranti + raccolta sensazioni fisiche
 */
val DecompressionPhase: State = state(Parent) {
    
    var step = 0
    
    onEntry {
        EventLogger.logPhaseStart("DECOMPRESSION")
        
        when (step) {
            0 -> {
                // Cambio di tono: più caldo e rassicurante
                furhat.gesture(Gestures.Smile)
                delay(500)
                
                EventLogger.logQuestion("Aspettative dal colloquio")
                furhat.ask("Stiamo per concludere. Quali sono le sue aspettative da questo colloquio?", timeout = 20000, endSil = 1500)
            }
            1 -> {
                EventLogger.logQuestion("Chiarimenti necessari")
                furhat.ask("C'è qualcosa del colloquio che vorrebbe chiarire o che le è sembrato poco chiaro?", timeout = 20000, endSil = 1500)
            }
            else -> {
                goto(PostInterviewRecovery)
            }
        }
    }

    onResponse {
        EventLogger.logResponse(it.text)
        
        furhat.gesture(Gestures.Nod)
        
        // Usa GPT per la domanda sui chiarimenti (step 1)
        if (step == 1) {
            // GPT risponde in modo sensato alla domanda del candidato
            val gptAnswer = GPTService.generateDecompressionAnswer(it.text)
            if (gptAnswer != null) {
                furhat.say(gptAnswer)
            } else {
                furhat.say("Capisco. Tutte le informazioni le verranno comunicate via email nei prossimi giorni.")
            }
            EventLogger.logEvent("GPT_RESPONSE", "DECOMPRESSION", "Answered candidate question")
        } else {
            // Per altre risposte, usa GPT per follow-up naturale
            val gptResponse = GPTService.generateWarmupFollowup(it.text, "aspettative colloquio")
            if (gptResponse != null) {
                furhat.say(gptResponse)
            } else {
                furhat.say("Capisco perfettamente.")
            }
        }
        
        delay(800)
        step++
        reentry()
    }

    onNoResponse {
        EventLogger.logEvent("NO_RESPONSE", "DECOMPRESSION", "Step $step")
        
        if (step == 1) {
            furhat.say("Va bene. Le informazioni le verranno comunicate via email.")
        } else {
            furhat.say("Va bene, nessun problema.")
        }
        delay(500)
        step++
        reentry()
    }
}

/**
 * Fase di recupero post-intervista
 * Il partecipante rimane seduto rilassato per 3-5 minuti mentre continuiamo la registrazione EMG
 */
val PostInterviewRecovery: State = state(Parent) {
    
    onEntry {
        EventLogger.logPhaseEnd("DECOMPRESSION")
        EventLogger.logBaseline("POST-INTERVIEW-START")
        EventLogger.logEvent("RECOVERY", "START", "3-minute recovery period")
        
        furhat.gesture(Gestures.Smile)
        furhat.say {
            +"Perfetto. Il colloquio è terminato."
            +"La ringrazio per la sua partecipazione."
        }
        
        delay(1000)
        
        furhat.say {
            +"Ora le chiedo cortesemente di rimanere seduto in posizione rilassata per circa tre minuti."
            +"Respiri normalmente e cerchi di rilassarsi."
        }
        
        delay(2000)
        
        // Avvio timer di 3 minuti per recupero
        delay(180000) // 3 minuti = 180,000 ms
        
        EventLogger.logEvent("RECOVERY", "END", "3-minute recovery completed")
        EventLogger.logBaseline("POST-INTERVIEW-END")
        
        goto(PostExperimentQuestionnaire)
    }
}

/**
 * Questionario post-esperimento sulle sensazioni fisiche
 */
val PostExperimentQuestionnaire: State = state(Parent) {
    
    var questionNumber = 0
    
    onEntry {
        EventLogger.logPhaseStart("POST_QUESTIONNAIRE")
        
        when (questionNumber) {
            0 -> {
                furhat.gesture(Gestures.Smile)
                furhat.say {
                    +"Ora le farò alcune brevi domande sulla sua esperienza."
                }
                delay(1000)
                
                EventLogger.logQuestion("Sensazioni fisiche durante colloquio")
                furhat.ask("Durante il colloquio, ha notato tensione in qualche parte specifica del suo corpo? Per esempio, alle spalle, alla schiena, al collo, o alla mascella?", timeout = 20000, endSil = 1500)
            }
            1 -> {
                EventLogger.logQuestion("Momento di massima tensione")
                furhat.ask("In quale momento del colloquio si è sentito più in tensione?", timeout = 20000, endSil = 1500)
            }
            2 -> {
                EventLogger.logQuestion("Valutazione stress percepito")
                furhat.ask("Su una scala da uno a dieci, quanto si è sentito stressato durante il colloquio?", timeout = 15000, endSil = 1500)
            }
            else -> {
                goto(FinalDebriefing)
            }
        }
    }

    onResponse<PhysicalSensation> {
        EventLogger.logResponse("Physical sensation: ${it.text} - Body part: ${it.intent.bodyPart}")
        
        furhat.gesture(Gestures.Nod)
        furhat.say("Capisco, grazie per l'informazione.")
        
        delay(800)
        questionNumber++
        reentry()
    }

    onResponse {
        EventLogger.logResponse(it.text)
        
        furhat.gesture(Gestures.Nod)
        furhat.say("Va bene, grazie.")
        
        delay(800)
        questionNumber++
        reentry()
    }

    onNoResponse {
        EventLogger.logEvent("NO_RESPONSE", "POST_QUESTIONNAIRE", "Question $questionNumber")
        
        furhat.say("Nessun problema.")
        delay(500)
        questionNumber++
        reentry()
    }
}

/**
 * Debriefing finale - rivelazione dello studio
 */
val FinalDebriefing: State = state(Parent) {
    
    onEntry {
        EventLogger.logPhaseEnd("POST_QUESTIONNAIRE")
        EventLogger.logPhaseStart("DEBRIEFING")
        
        furhat.gesture(Gestures.Smile)
        
        furhat.say {
            +"Perfetto. Ora posso rivelarle il vero scopo di questo studio."
        }
        
        delay(1000)
        
        furhat.say {
            +"Questo non era un vero colloquio di lavoro."
            +"Era un esperimento scientifico per studiare come il corpo reagisce allo stress psicologico."
        }
        
        delay(1500)
        
        furhat.say {
            +"Durante l'intervista, abbiamo registrato l'attività elettrica dei muscoli della sua schiena e delle sue spalle."
            +"L'obiettivo era quantificare i cambiamenti nella tensione muscolare in risposta a diversi tipi di stress."
        }
        
        delay(1500)
        
        furhat.say {
            +"Alcune domande erano volutamente difficili o provocatorie per indurre una reazione di stress."
            +"Questo faceva parte del protocollo sperimentale."
        }
        
        delay(1500)
        
        furhat.gesture(Gestures.Nod)
        furhat.say {
            +"La ringrazio moltissimo per la sua partecipazione."
            +"I suoi dati saranno trattati in modo anonimo e contribuiranno alla ricerca scientifica."
        }
        
        delay(1000)
        
        EventLogger.endSession()
        EventLogger.logPhaseEnd("DEBRIEFING")
        
        furhat.say("L'esperimento è concluso. Può rimuovere gli elettrodi. Grazie ancora.")
        
        delay(3000)
        
        goto(Idle)
    }
}
