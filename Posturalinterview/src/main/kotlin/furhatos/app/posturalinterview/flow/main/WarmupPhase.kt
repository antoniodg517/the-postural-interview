package furhatos.app.posturalinterview.flow.main

import furhatos.app.posturalinterview.flow.Parent
import furhatos.app.posturalinterview.flow.participantCV
import furhatos.app.posturalinterview.nlu.DescribeExperience
import furhatos.app.posturalinterview.util.EventLogger
import furhatos.app.posturalinterview.util.GPTService
import furhatos.app.posturalinterview.data.CVLoader
import furhatos.flow.kotlin.*
import furhatos.gestures.Gestures

/**
 * FASE 1: RISCALDAMENTO (2-3 minuti)
 * Domande standard per far ambientare la persona
 * Se disponibile, usa informazioni dal CV per personalizzare le domande
 */
val WarmupPhase: State = state(Parent) {
    
    var questionsAsked = 0
    
    onEntry {
        EventLogger.logPhaseStart("WARMUP")
        EventLogger.logBaseline("PRE-INTERVIEW")
        
        furhat.gesture(Gestures.Nod)
        delay(500)
        
        when (questionsAsked) {
            0 -> {
                // Saluto diretto con nome dal CV + prima domanda sull'esperienza
                if (participantCV != null) {
                    val name = participantCV!!.personalInfo.name
                    val latestExp = CVLoader.getLatestWorkExperience(participantCV!!)
                    
                    EventLogger.logQuestion("Esperienza lavorativa con nome")
                    EventLogger.logEvent("CV_INFO", "NAME_USED", name)
                    
                    if (latestExp != null) {
                        EventLogger.logEvent("CV_INFO", "EXPERIENCE_DISCUSSED", "${latestExp.role} - ${latestExp.company}")
                        furhat.ask("Buongiorno ${name}. Vedo dal suo CV che ha lavorato come ${latestExp.role} presso ${latestExp.company}. Mi racconti di questa esperienza.", timeout = 20000, endSil = 1500)
                    } else {
                        val latestEdu = CVLoader.getLatestEducation(participantCV!!)
                        EventLogger.logEvent("CV_INFO", "EDUCATION_DISCUSSED", "${latestEdu?.field}")
                        furhat.ask("Buongiorno ${name}. Vedo che ha studiato ${latestEdu?.field}. Mi parli del suo percorso di studi.", timeout = 20000, endSil = 1500)
                    }
                } else {
                    // Domanda generica senza CV
                    EventLogger.logQuestion("Prima esperienza generica")
                    furhat.ask("Buongiorno. Per iniziare, mi descriva la sua ultima esperienza lavorativa o di studio.", timeout = 20000, endSil = 1500)
                }
            }
            1 -> {
                EventLogger.logQuestion("Motivazione")
                furhat.ask("Interessante. E cosa la motiva maggiormente nel suo lavoro?", timeout = 20000, endSil = 1500)
            }
            2 -> {
                // Domanda specifica su progetto dal CV
                if (participantCV != null && participantCV!!.projects?.isNotEmpty() == true) {
                    val project = participantCV!!.projects!!.first()
                    EventLogger.logQuestion("Progetto specifico dal CV")
                    EventLogger.logEvent("CV_INFO", "PROJECT_DISCUSSED", project.name)
                    furhat.ask("Vedo che ha lavorato al progetto '${project.name}'. Me ne parli, quali tecnologie ha utilizzato?", timeout = 20000, endSil = 1500)
                } else if (participantCV != null && participantCV!!.skills.technical.isNotEmpty()) {
                    val skills = participantCV!!.skills.technical.take(3).joinToString(", ")
                    EventLogger.logQuestion("Skills tecniche dal CV")
                    EventLogger.logEvent("CV_INFO", "SKILLS_DISCUSSED", skills)
                    furhat.ask("Nel suo CV indica competenze in ${skills}. Come ha acquisito queste competenze?", timeout = 20000, endSil = 1500)
                } else {
                    EventLogger.logQuestion("Competenze tecniche")
                    furhat.ask("Quali sono le sue principali competenze tecniche?", timeout = 20000, endSil = 1500)
                }
            }
            3 -> {
                EventLogger.logQuestion("Ambiente di lavoro")
                furhat.ask("In che tipo di ambiente lavorativo si trova meglio?", timeout = 20000, endSil = 1500)
            }
            4 -> {
                EventLogger.logQuestion("Obiettivi futuri")
                furhat.ask("Per concludere, quali sono i suoi obiettivi professionali per i prossimi anni?", timeout = 20000, endSil = 1500)
            }
            else -> {
                EventLogger.logPhaseEnd("WARMUP")
                furhat.say("Bene. Ora passeremo ad alcune domande più specifiche.")
                delay(1000)
                goto(StressPhase)
            }
        }
    }

    onResponse<DescribeExperience> {
        EventLogger.logResponse(it.text)
        
        // Usa GPT per generare follow-up naturale basato sulla risposta
        val questionTopic = when (questionsAsked) {
            0 -> "esperienza lavorativa e background"
            1 -> "motivazione professionale"
            2 -> "progetti o competenze tecniche"
            3 -> "ambiente di lavoro preferito"
            4 -> "obiettivi futuri"
            else -> "generale"
        }
        
        val gptResponse = GPTService.generateWarmupFollowup(it.text, questionTopic)
        if (gptResponse != null) {
            furhat.say(gptResponse)
        } else {
            furhat.say("Capisco.")
        }
        
        delay(800)
        questionsAsked++
        reentry()
    }

    onResponse {
        EventLogger.logResponse(it.text)
        
        // Fallback con GPT per risposte generiche
        val questionTopic = when (questionsAsked) {
            0 -> "esperienza lavorativa"
            1 -> "motivazione"
            2 -> "competenze"
            3 -> "ambiente di lavoro"
            4 -> "obiettivi"
            else -> "generale"
        }
        
        val gptResponse = GPTService.generateWarmupFollowup(it.text, questionTopic)
        furhat.gesture(Gestures.Nod)
        if (gptResponse != null) {
            furhat.say(gptResponse)
        } else {
            furhat.say("Capisco.")
        }
        
        delay(800)
        questionsAsked++
        reentry()
    }

    onNoResponse {
        EventLogger.logEvent("NO_RESPONSE", "WARMUP", "Question ${questionsAsked + 1}")
        furhat.say("Prenda il suo tempo.")
        furhat.listen(timeout = 20000, endSil = 1500)
    }
}
