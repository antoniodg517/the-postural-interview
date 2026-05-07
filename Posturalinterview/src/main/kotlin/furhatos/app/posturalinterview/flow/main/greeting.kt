package furhatos.app.posturalinterview.flow.main

import furhatos.app.posturalinterview.flow.Parent
import furhatos.app.posturalinterview.nlu.ReadyConfirmation
import furhatos.app.posturalinterview.util.EventLogger
import furhatos.flow.kotlin.*
import furhatos.gestures.Gestures
import furhatos.nlu.common.Yes

val Greeting: State = state(Parent) {
    
    var retries = 0
    
    onEntry {
        EventLogger.logPhaseStart("GREETING")
        
        furhat.gesture(Gestures.Smile)
        furhat.say("Buongiorno. Benvenuto a questo colloquio. Mi chiamo Furhat e oggi sarò il suo intervistatore.")
        
        delay(800)
        
        furhat.say {
            +"Prima di iniziare, vorrei che si mettesse comodo sulla sedia."
            +"Tenga la schiena dritta ma rilassata, e le mani appoggiate sulle gambe."
        }
        
        delay(1000)
        
        EventLogger.logQuestion("Pronto per iniziare?")
        furhat.ask("È pronto per iniziare il colloquio?", timeout = 15000, endSil = 1500)
    }

    onResponse<ReadyConfirmation> {
        EventLogger.logResponse("Pronto")
        furhat.say("Perfetto. Iniziamo con alcune domande introduttive.")
        delay(500)
        goto(WarmupPhase)
    }

    onResponse<Yes> {
        EventLogger.logResponse("Sì")
        furhat.say("Perfetto. Iniziamo.")
        delay(500)
        goto(WarmupPhase)
    }

    onResponse {
        EventLogger.logResponse(it.text)
        
        if (retries < 2) {
            furhat.say("Va bene, prenda il suo tempo.")
            retries++
            delay(1000)
            furhat.ask("Quando è pronto, mi faccia un cenno.", timeout = 15000, endSil = 1500)
        } else {
            // Dopo 2 tentativi, procedi comunque
            furhat.say("Bene, procediamo.")
            delay(500)
            goto(WarmupPhase)
        }
    }
    
    onNoResponse {
        EventLogger.logEvent("NO_RESPONSE", "GREETING", "Retry $retries")
        
        if (retries < 2) {
            furhat.say("Non ho sentito la sua risposta. È pronto?")
            retries++
            delay(500)
            furhat.listen(timeout = 15000, endSil = 1500)
        } else {
            // Dopo 2 tentativi, procedi comunque
            furhat.say("Bene, iniziamo comunque.")
            delay(500)
            goto(WarmupPhase)
        }
    }
}

