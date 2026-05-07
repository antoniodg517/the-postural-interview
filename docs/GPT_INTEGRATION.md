# Integrazione ChatGPT - Intervista Posturale

## Panoramica

L'intervista usa **GPT-4 in modalità ibrida**: mantiene il protocollo strutturato dell'esperimento ma genera risposte naturali e contestuali durante l'interazione.

## Dove viene usato GPT

### 1. **Fase Warmup (Riscaldamento)**
- Genera follow-up naturali alle risposte del candidato
- Invece di risposte fisse ("Capisco", "Interessante"), il robot risponde in modo contestuale
- Mantiene coerenza con le 5 domande strutturate

**Esempio:**
```
Candidato: "Ho lavorato 2 anni come developer in React"
GPT: "Interessante. Due anni sono un buon periodo per acquisire esperienza solida."
```

### 2. **Fase Stress - Competency Challenge**
- Genera domande di sfida personalizzate basate sul CV
- La domanda è tecnica e provocatoria, costruita su:
  - Skill specifiche dal CV
  - Esperienza lavorativa dichiarata

**Esempio:**
```
CV skill: "Python"
CV exp: "Software Developer presso Tech Solutions"
GPT genera: "Vedo che indica Python tra le competenze. Dato il suo ruolo in Tech Solutions, 
mi descriva un progetto complesso dove ha applicato Python a livello enterprise."
```

### 3. **Fase Decompression - Risposta a domande**
- Quando il candidato fa domande, GPT genera risposte sensate
- Mantiene tono professionale e rassicurante
- Reindirizza a comunicazioni via email quando appropriato

**Esempio:**
```
Candidato: "Quando riceverò feedback sul colloquio?"
GPT: "Capisco il suo interesse. Riceverà comunicazioni dettagliate via email 
entro la prossima settimana con tutte le informazioni necessarie."
```

## Configurazione

### File: `config/openai.properties`
```properties
openai.api.key=YOUR_API_KEY_HERE
openai.model=gpt-4
openai.temperature=0.7
openai.max.tokens=150
```

### Parametri:
- **model**: `gpt-4` (consigliato) o `gpt-3.5-turbo` (più economico)
- **temperature**: `0.7` = creatività moderata, risposte naturali ma controllate
- **max_tokens**: `150` = risposte brevi (2-3 frasi max)

## System Prompt

Il robot è configurato con questo comportamento:

```
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
```

## Cronologia conversazione

GPT mantiene memoria della conversazione per coerenza:
- Inizializza all'inizio della sessione
- Si resetta a ogni nuovo partecipante
- Mantiene contesto durante tutte le fasi

## Fallback

Se l'API di OpenAI non risponde:
- Usa risposte predefinite ("Capisco", "Interessante")
- L'esperimento continua normalmente
- Viene loggato nel console: `[GPT] ERROR`

## Logging

Eventi GPT loggati:
```csv
timestamp_ms,elapsed_ms,event_type,event_name,details
1234567890,5000,GPT_GENERATED,CHALLENGE_QUESTION,Python
1234567891,5100,GPT_RESPONSE,DECOMPRESSION,Answered candidate question
```

## Costi stimati

Con GPT-4:
- Input: ~$0.03 / 1K tokens
- Output: ~$0.06 / 1K tokens
- Stima per intervista completa: ~$0.10-0.20

Con GPT-3.5-turbo:
- Input: ~$0.0015 / 1K tokens  
- Output: ~$0.002 / 1K tokens
- Stima per intervista completa: ~$0.01-0.02

## Test

Per testare l'integrazione:
```bash
# 1. Verifica config
cat config/openai.properties

# 2. Build
./gradlew build

# 3. Run (con Furhat server attivo)
./gradlew run

# Controlla output console per:
# ✓ GPT Service inizializzato con modello: gpt-4
# [GPT] Input: ...
# [GPT] Response: ...
```

## Disabilitare GPT

Se vuoi tornare a risposte fisse:

1. **Commenta import in WarmupPhase.kt:**
```kotlin
// import furhatos.app.posturalinterview.util.GPTService
```

2. **Sostituisci chiamate GPT con:**
```kotlin
// val gptResponse = GPTService.generateWarmupFollowup(it.text, questionTopic)
furhat.say("Capisco.")
```

3. **Ripeti per DecompressionPhase.kt e StressPhase.kt**

## Troubleshooting

### Errore: "File config/openai.properties non trovato"
- Crea il file in `config/openai.properties`
- Inserisci la tua API key

### Errore: "Connection refused" / "Timeout"
- Verifica connessione internet
- Controlla che l'API key sia valida
- Prova a ridurre `max_tokens` se troppo lento

### GPT genera risposte troppo lunghe
- Riduci `max_tokens` a 100-120
- Aumenta la specificità nel system prompt

### GPT non rispetta il tono formale
- Aumenta enfasi su "dare del lei" nel system prompt
- Riduci `temperature` a 0.5 per risposte più prevedibili

## Sicurezza API Key

⚠️ **IMPORTANTE:**
- Non committare `config/openai.properties` su Git
- Aggiungi al `.gitignore`:
```
config/openai.properties
```

- Per distribuire: usa variabili d'ambiente o secret management
