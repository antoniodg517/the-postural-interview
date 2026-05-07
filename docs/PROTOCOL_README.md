# The Postural Interview - Furhat Implementation

Implementazione completa dell'esperimento "The Postural Interview" per lo studio dei cambiamenti nella tensione muscolare posturale durante un colloquio stressante condotto dal robot sociale Furhat.

## Struttura del Protocollo

### Fase 0: Preparazione (Manuale)
- Allestimento stanza silenziosa e temperatura controllata
- Posizionamento sedia standard senza braccioli
- Applicazione elettrodi EMG su:
  - Muscolo trapezio superiore (bilaterale)
  - Muscoli erettori spinali lombari (bilaterale)
- Registrazione baseline posturale (3-5 min seduto rilassato)

### Fase 1: Riscaldamento (2-3 minuti) - `WarmupPhase.kt`
Domande standard non stressanti per ambientare il partecipante:
- Descrizione ultima esperienza lavorativa/di studio
- Motivazioni nel proprio campo
- Punti di forza personali

**Marcatori EMG**: `PHASE:START:WARMUP`, `BASELINE:PRE-INTERVIEW`

### Fase 2: Induzione Stress (4-5 minuti) - `StressPhase.kt`

#### Stressor 1: Carico Cognitivo (`CognitiveLoadStressor`)
- **Compito**: Problema logico del mattone (60 secondi)
- **Risposta attesa posturale**: Postura tesa, inclinazione in avanti, concentrazione
- **Marcatore**: `STRESSOR:COGNITIVE_LOAD`

#### Stressor 2: Sfida alla Competenza (`CompetencyChallengeStressor`)
- **Compito**: Messa in discussione delle competenze dichiarate
- **Risposta attesa posturale**: Spalle alzate, risposta difensiva
- **Marcatore**: `STRESSOR:COMPETENCY_CHALLENGE`

#### Stressor 3: Silenzio Valutativo (`EvaluativeSilenceStressor`)
- **Compito**: 7 secondi di silenzio con movimenti valutativi dopo una risposta
- **Risposta attesa posturale**: Tensione palpabile, irrigidimento
- **Marcatori**: `SILENCE:START`, `SILENCE:END`

### Fase 3: Decompressione (1-2 minuti) - `DecompressionPhase.kt`
- Domande conclusive rassicuranti
- Cambio di tono: più caldo e accogliente
- **Marcatore**: `PHASE:START:DECOMPRESSION`

### Fase 4: Recupero Post-Intervista (`PostInterviewRecovery`)
- 3 minuti di registrazione EMG con partecipante rilassato
- Misurazione tempo di ritorno ai livelli baseline
- **Marcatori**: `RECOVERY:START`, `RECOVERY:END`, `BASELINE:POST-INTERVIEW`

### Fase 5: Questionario Post-Esperimento (`PostExperimentQuestionnaire`)
Domande su:
1. Sensazioni fisiche percepite (tensione in spalle/schiena/collo)
2. Momento di massima tensione
3. Livello di stress percepito (scala 1-10)

### Fase 6: Debriefing (`FinalDebriefing`)
- Rivelazione del vero scopo dello studio
- Spiegazione della manipolazione dello stress
- Ringraziamenti e chiusura

## Sistema di Logging

### EventLogger (`util/EventLogger.kt`)
Sistema di logging sincronizzato con acquisizione EMG tramite timestamp precisi.

**File di output**: `logs/postural_interview_{ID}_{timestamp}.csv`

**Formato CSV**:
```csv
timestamp_ms,elapsed_ms,event_type,event_name,details
1234567890,0,SESSION,START,"Participant: P001"
1234567891,1,PHASE,START,WARMUP
1234568000,110,QUESTION,ASKED,"Descriva ultima esperienza"
1234570000,2110,RESPONSE,RECEIVED,"Ho lavorato come..."
1234572000,4110,STRESSOR,COGNITIVE_LOAD,"Brick problem - 60 seconds"
```

**Tipi di eventi**:
- `SESSION`: START, END
- `PHASE`: START, END (WARMUP, STRESS, DECOMPRESSION, etc.)
- `BASELINE`: PRE-INTERVIEW, POST-INTERVIEW-START, POST-INTERVIEW-END
- `QUESTION`: ASKED
- `RESPONSE`: RECEIVED
- `STRESSOR`: COGNITIVE_LOAD, COMPETENCY_CHALLENGE, EVALUATIVE_SILENCE
- `SILENCE`: START, END
- `RECOVERY`: START, END
- `NO_RESPONSE`: timeout o assenza di risposta

## NLU Intents (`nlu/Intents.kt`)

- `DescribeExperience`: Descrizioni di esperienze lavorative/studio
- `ExplainCompetency`: Spiegazioni di competenze
- `ExpressUncertainty`: Espressioni di incertezza/difficoltà
- `SolveBrickProblem`: Risposta al problema logico
- `ReadyConfirmation`: Conferma di essere pronti
- `PhysicalSensation`: Descrizioni di sensazioni fisiche

## Avvio del Sistema

### 1. Compilazione
```bash
./gradlew clean build
```

### 2. Esecuzione
```bash
./gradlew run
```

All'avvio, il sistema richiederà l'ID del partecipante:
```
==============================================
POSTURAL INTERVIEW EXPERIMENT
==============================================
Inserisci ID partecipante (es. P001): P001
Sessione iniziata per partecipante: P001
Log salvato in: logs/postural_interview_P001_*.csv
==============================================
```

### 3. Sincronizzazione con EMG
Per sincronizzare con il sistema EMG, utilizzare i timestamp nel file CSV generato.

**Opzione consigliata**: Usare **Lab Streaming Layer (LSL)** per sincronizzazione real-time.

## Analisi Dati

### Feature EMG da Estrarre
Per ogni finestra temporale (baseline, stressor, recupero):
1. **RMS (Root Mean Square)**: `sqrt(mean(signal^2))`
2. **iEMG (Integrated EMG)**: `sum(abs(signal))`
3. **Mean Absolute Value**: `mean(abs(signal))`
4. **Median Frequency**
5. **Peak Amplitude**

### Finestre Temporali di Interesse
- **Baseline pre**: 30s prima di `PHASE:START:WARMUP`
- **Durante stressor**: da `STRESSOR:START` a +15s
- **Post-stressor**: +15s a +30s dopo fine stressor
- **Recovery**: da `RECOVERY:START` a `RECOVERY:END`
- **Baseline post**: ultimi 30s di `RECOVERY`

### Confronti Chiave
1. Baseline vs. Peak stressor (per muscolo)
2. Confronto tra i 3 tipi di stressor
3. Tempo di ritorno a baseline dopo stress
4. Correlazione con dati soggettivi (questionario)

## Parametri Configurabili

In `setting/interactionParams.kt`:
- `DISTANCE_TO_ENGAGE`: Distanza di engagement (default: 1.5m)
- `MAX_NUMBER_OF_USERS`: Numero massimo utenti (default: 1)

### Timing Modificabili
Nel codice sorgente:
- `WarmupPhase`: numero domande (default: 3)
- `CognitiveLoadStressor`: timeout problema (default: 60s)
- `EvaluativeSilenceStressor`: durata silenzio (default: 7s)
- `PostInterviewRecovery`: durata recupero (default: 180s)

## Requisiti Tecnici

### Hardware
- Robot Furhat o Furhat Virtual
- Sistema EMG wireless (min. 4 canali)
- Sedia standard senza braccioli

### Software
- Furhat SDK 2.8.4
- Java 11
- Kotlin 1.8.21
- Gradle 6.9.4

## Note Etiche

⚠️ **IMPORTANTE**: Questo è un protocollo sperimentale che coinvolge manipolazione psicologica e induzione di stress.

**Requisiti obbligatori**:
1. Approvazione del comitato etico
2. Consenso informato firmato
3. Debriefing completo post-esperimento
4. Diritto di ritiro in qualsiasi momento
5. Screening per condizioni cardiovascolari/ansia
6. Presenza di personale qualificato

## Struttura File

```
src/main/kotlin/furhatos/app/posturalinterview/
├── main.kt                          # Entry point
├── flow/
│   ├── init.kt                      # Inizializzazione e richiesta ID
│   ├── parent.kt                    # State padre per engagement
│   └── main/
│       ├── idle.kt                  # Stato idle
│       ├── greeting.kt              # Saluto iniziale
│       ├── WarmupPhase.kt          # Fase riscaldamento
│       ├── StressPhase.kt          # Fase stress (3 stressor)
│       └── DecompressionPhase.kt   # Decompressione + recovery + debriefing
├── nlu/
│   └── Intents.kt                   # Intent NLU personalizzati
├── setting/
│   └── interactionParams.kt         # Parametri interazione
└── util/
    └── EventLogger.kt               # Sistema logging eventi

logs/                                 # Directory log generati (auto-creata)
└── postural_interview_*.csv         # File CSV per analisi
```

## Contributi e Citazioni

Questo protocollo è basato su "The Postural Interview" per lo studio quantitativo della tensione muscolare posturale in risposta a stress psicologico indotto.

Per citare questo lavoro:
```
Del Giudice, A. (2026). The Postural Interview: A Furhat-based protocol
for quantifying postural muscle tension during psychosocial stress.
```

## Licenza

Questo codice è fornito per scopi di ricerca scientifica.
