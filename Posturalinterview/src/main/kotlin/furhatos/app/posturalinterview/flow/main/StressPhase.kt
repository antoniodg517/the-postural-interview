package furhatos.app.posturalinterview.flow.main

import furhatos.app.posturalinterview.flow.Parent
import furhatos.app.posturalinterview.flow.participantCV
import furhatos.app.posturalinterview.nlu.SolveBrickProblem
import furhatos.app.posturalinterview.nlu.ExplainCompetency
import furhatos.app.posturalinterview.nlu.ExpressUncertainty
import furhatos.app.posturalinterview.util.EventLogger
import furhatos.app.posturalinterview.util.GPTService
import furhatos.app.posturalinterview.data.CVLoader
import furhatos.flow.kotlin.*
import furhatos.gestures.Gestures
import furhatos.records.Location

/**
 * FASE 2: STRESS (4-5 minuti)
 * Include 3 tipi di stressor:
 * 1. Carico cognitivo elevato (problema logico con timer)
 * 2. Sfida alla competenza
 * 3. Silenzio valutativo
 */
val StressPhase: State = state(Parent) {
    
    onEntry {
        EventLogger.logPhaseStart("STRESS")
        goto(CognitiveLoadStressor)
    }
}

/**
 * STRESSOR 1: Carico Cognitivo - Problema del mattone
 * Induce postura di concentrazione tesa, spesso inclinata in avanti
 */
val CognitiveLoadStressor: State = state(Parent) {
    
    var attempts = 0
    
    onEntry {
        EventLogger.logStressor("COGNITIVE_LOAD", "Brick problem - 60 seconds")
        
        furhat.gesture(Gestures.BrowRaise)
        furhat.say {
            +"Ora le darò un breve problema logico da risolvere."
            +"Ha sessanta secondi di tempo."
        }
        
        delay(1000)
        
        EventLogger.logQuestion("Problema del mattone")
        furhat.ask("Se un mattone pesa un chilo più mezzo mattone, quanto pesa un mattone e mezzo?", 
                   timeout = 60000, endSil = 2000) // 60 seconds, 2s silenzio
    }

    onResponse<SolveBrickProblem> {
        EventLogger.logResponse("Answer: ${it.text}")
        
        val answerLower = it.text.lowercase()
        val isCorrect = answerLower.contains("tre") || answerLower.contains("3")
        
        when {
            isCorrect -> {
                furhat.gesture(Gestures.Nod)
                furhat.say("Corretto. Tre chili.")
                delay(800)
                goto(CompetencyChallengeStressor)
            }
            attempts < 1 -> {
                EventLogger.logStressor("COGNITIVE_LOAD", "Wrong answer, retry")
                furhat.gesture(Gestures.BrowFrown)
                furhat.say("Non è corretto. Ci riprovi, pensi con calma.")
                attempts++
                furhat.listen(timeout = 40000, endSil = 2000)
            }
            else -> {
                EventLogger.logStressor("COGNITIVE_LOAD", "Failed after retries")
                furhat.say("La risposta era tre chili. Un mattone pesa due chili.")
                delay(1000)
                goto(CompetencyChallengeStressor)
            }
        }
    }

    onResponse<ExpressUncertainty> {
        EventLogger.logResponse("Uncertainty: ${it.text}")
        EventLogger.logStressor("COGNITIVE_LOAD", "Participant expressed uncertainty")
        
        furhat.say("Prenda il suo tempo. Ragioni ad alta voce se vuole.")
        furhat.listen(timeout = 40000, endSil = 2000)
    }

    onResponse {
        EventLogger.logResponse("Non-standard: ${it.text}")
        
        if (attempts < 1) {
            furhat.say("Non ho capito la risposta. Mi dica un numero.")
            attempts++
            furhat.listen(timeout = 40000, endSil = 2000)
        } else {
            furhat.say("Va bene, passiamo oltre. La risposta era tre chili.")
            delay(800)
            goto(CompetencyChallengeStressor)
        }
    }

    onNoResponse {
        EventLogger.logEvent("NO_RESPONSE", "COGNITIVE_LOAD", "Timeout after ${attempts + 1} attempts")
        
        if (attempts < 1) {
            EventLogger.logStressor("COGNITIVE_LOAD", "First timeout")
            furhat.gesture(Gestures.BrowFrown)
            furhat.say("Il tempo sta per scadere. Ha una risposta?")
            attempts++
            furhat.listen(timeout = 20000, endSil = 2000)
        } else {
            EventLogger.logStressor("COGNITIVE_LOAD", "Final timeout")
            furhat.say("Il tempo è scaduto. La risposta era tre chili.")
            delay(1000)
            goto(CompetencyChallengeStressor)
        }
    }
}

/**
 * STRESSOR 2: Sfida alla Competenza
 * Induce risposta difensiva, spesso con spalle alzate
 * Usa informazioni dal CV se disponibili
 */
val CompetencyChallengeStressor: State = state(Parent) {
    
    onEntry {
        EventLogger.logStressor("COMPETENCY_CHALLENGE", "Questioning expertise")
        
        delay(500)
        
        // Cambio nel tono e nell'espressione
        furhat.gesture(Gestures.BrowFrown)
        
        EventLogger.logQuestion("Sfida competenza")
        
        // Usa GPT per generare domanda di sfida basata sul CV
        if (participantCV != null) {
            val skill = CVLoader.getRandomTechnicalSkill(participantCV!!)
            val experience = CVLoader.getLatestWorkExperience(participantCV!!)
            
            if (skill != null && experience != null) {
                val challengeQuestion = GPTService.generateCompetencyChallenge(
                    skill, 
                    "${experience.role} - ${experience.description}"
                )
                
                EventLogger.logEvent("CV_INFO", "SKILL_CHALLENGED", skill)
                
                furhat.say {
                    +"Vedo dal suo portfolio che ha dichiarato competenze in $skill."
                }
                delay(800)
                furhat.say {
                    +"Tuttavia, dalle sue risposte precedenti, mi sembra una competenza piuttosto basilare."
                }
                delay(1000)
                furhat.ask(challengeQuestion, timeout = 20000, endSil = 1500)
            } else if (skill != null) {
                // Fallback con solo skill
                EventLogger.logEvent("CV_INFO", "SKILL_CHALLENGED", skill)
                furhat.say {
                    +"Vedo dal suo portfolio che ha dichiarato competenze in $skill."
                }
                delay(800)
                furhat.say {
                    +"Tuttavia, dalle sue risposte precedenti, mi sembra una competenza piuttosto basilare."
                }
                delay(1000)
                furhat.ask("Può convincermi che è davvero esperto in $skill come sostiene?", 
                           timeout = 20000, endSil = 1500)
            } else {
                // Fallback generico
                furhat.say {
                    +"Vedo dal suo portfolio che ha dichiarato diverse competenze."
                }
                delay(800)
                furhat.say {
                    +"Tuttavia, dalle sue risposte precedenti, mi sembrano competenze piuttosto basilari."
                }
                delay(1000)
                furhat.ask("Può convincermi che è davvero esperto come sostiene?", 
                           timeout = 20000, endSil = 1500)
            }
        } else {
            // Domanda generica senza CV
            furhat.say {
                +"Vedo dal suo portfolio che ha dichiarato diverse competenze."
            }
            delay(800)
            furhat.say {
                +"Tuttavia, dalle sue risposte precedenti, mi sembrano competenze piuttosto basilari."
            }
            delay(1000)
            furhat.ask("Può convincermi che è davvero esperto come sostiene?", 
                       timeout = 20000, endSil = 1500)
        }
    }

    onResponse<ExplainCompetency> {
        EventLogger.logResponse("Competency defense: ${it.text}")
        
        // Ulteriore pressione con silenzio valutativo breve
        delay(2000)
        EventLogger.logStressor("COMPETENCY_CHALLENGE", "Short evaluative silence 2s")
        
        furhat.gesture(Gestures.Thoughtful)
        delay(2000)
        
        furhat.say("Capisco.")
        delay(1000)
        
        goto(EvaluativeSilenceStressor)
    }

    onResponse<ExpressUncertainty> {
        EventLogger.logResponse("Uncertainty to challenge: ${it.text}")
        EventLogger.logStressor("COMPETENCY_CHALLENGE", "Defensive uncertainty")
        
        furhat.say("Non deve essere insicuro. Mi dica cosa sa fare concretamente.")
        furhat.listen(timeout = 20000, endSil = 1500)
    }

    onResponse {
        EventLogger.logResponse("Generic defense: ${it.text}")
        
        // Breve silenzio valutativo
        delay(2000)
        EventLogger.logStressor("COMPETENCY_CHALLENGE", "Post-response silence 2s")
        
        furhat.say("Va bene.")
        delay(1000)
        
        goto(EvaluativeSilenceStressor)
    }

    onNoResponse {
        EventLogger.logEvent("NO_RESPONSE", "COMPETENCY_CHALLENGE", "No defense provided")
        
        furhat.gesture(Gestures.ExpressSad)
        furhat.say("Capisco. Passiamo ad altro.")
        delay(1000)
        
        goto(EvaluativeSilenceStressor)
    }
}

/**
 * STRESSOR 3: Silenzio Valutativo Prolungato
 * Crea tensione posturale palpabile - 7 secondi di silenzio con movimenti valutativi
 */
val EvaluativeSilenceStressor: State = state(Parent) {
    
    onEntry {
        EventLogger.logStressor("EVALUATIVE_SILENCE", "Starting evaluative question")
        
        EventLogger.logQuestion("Gestione di un conflitto")
        furhat.ask("Mi racconti di una situazione difficile che ha dovuto gestire. Come l'ha risolta?")
    }

    onResponse {
        EventLogger.logResponse(it.text)
        
        // Feedback neutro
        furhat.say("Interessante.")
        
        // SILENZIO VALUTATIVO DI 7 SECONDI
        EventLogger.logSilenceStart(7)
        EventLogger.logStressor("EVALUATIVE_SILENCE", "7-second evaluative silence begins")
        
        delay(1000)
        // Leggero movimento della testa come se stesse valutando
        furhat.gesture(Gestures.Thoughtful, async = true)
        
        delay(2000)
        // Sguardo di lato (aumenta tensione)
        furhat.glance(Location(0.5, 0.0, 1.0))
        
        delay(2000)
        // Torna a guardare il partecipante
        furhat.attend(users.current)
        
        delay(2000)
        // Leggero cenno della testa ambiguo
        furhat.gesture(Gestures.Nod, async = true)
        
        EventLogger.logSilenceEnd()
        EventLogger.logStressor("EVALUATIVE_SILENCE", "7-second silence completed")
        
        delay(1000)
        
        // Feedback ambiguo finale
        furhat.say("Hm. Va bene.")
        
        delay(1000)
        
        EventLogger.logPhaseEnd("STRESS")
        goto(DecompressionPhase)
    }

    onNoResponse {
        EventLogger.logEvent("NO_RESPONSE", "EVALUATIVE_SILENCE", "No answer to conflict question")
        
        furhat.say("Non ha mai affrontato situazioni difficili?")
        furhat.listen(timeout = 20000, endSil = 1500)
    }
}
