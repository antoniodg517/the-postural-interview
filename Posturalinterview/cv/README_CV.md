# Sistema CV - The Postural Interview

## Come Funziona

Il sistema carica automaticamente il CV del partecipante all'avvio e lo utilizza per:

1. **Personalizzare domande sul warmup** - Riferimenti a esperienze lavorative/formative reali
2. **Sfida alla competenza** - Mette in discussione una skill tecnica specifica dal CV
3. **Rendere l'intervista più credibile** - Il partecipante si sente davvero "valutato"

## Struttura Cartelle

```
Posturalinterview/
├── cv/
│   ├── CV_TEMPLATE.json      # Template vuoto da copiare
│   ├── CV_P001.json          # CV esempio
│   ├── CV_P002.json          # CV partecipante 2
│   └── CV_P003.json          # CV partecipante 3
```

## Come Creare un CV per un Partecipante

### 1. Copia il Template

```bash
cp cv/CV_TEMPLATE.json cv/CV_P002.json
```

### 2. Compila i Dati

Apri il file e sostituisci i placeholder con i dati reali:

```json
{
  "participantId": "P002",
  "personalInfo": {
    "name": "Laura",
    "surname": "Bianchi",
    "age": 26
  },
  "education": [
    {
      "degree": "Laurea Magistrale",
      "field": "Psicologia",
      "institution": "Università di Padova",
      "year": 2022
    }
  ],
  "workExperience": [
    {
      "role": "Psicologa tirocinante",
      "company": "Studio Psicologico XYZ",
      "duration": "1 anno",
      "description": "Supporto psicologico individuale e di gruppo"
    }
  ],
  "skills": {
    "technical": [
      "SPSS",
      "R",
      "Valutazione psicodiagnostica",
      "Terapia cognitivo-comportamentale"
    ],
    "languages": [
      {
        "name": "Italiano",
        "level": "Madrelingua"
      },
      {
        "name": "Inglese",
        "level": "C1"
      }
    ],
    "soft": [
      "Ascolto attivo",
      "Empatia",
      "Problem solving"
    ]
  },
  "projects": [
    {
      "name": "Tesi magistrale",
      "description": "Studio sull'ansia da prestazione",
      "technologies": ["SPSS", "Questionari validati"]
    }
  ]
}
```

### 3. Salva con Nome Corretto

**Importante**: Il nome del file deve essere `CV_{ID_PARTECIPANTE}.json`

- Partecipante P001 → `CV_P001.json`
- Partecipante P002 → `CV_P002.json`
- Partecipante P003 → `CV_P003.json`

## Avvio dell'Esperimento

Quando avvii l'esperimento:

```bash
./gradlew run
```

Il sistema:
1. Ti chiede l'ID partecipante (es. `P002`)
2. Cerca automaticamente il file `cv/CV_P002.json`
3. Carica i dati del CV
4. Personalizza le domande

### Output Console

```
==============================================
POSTURAL INTERVIEW EXPERIMENT
==============================================
Inserisci ID partecipante (es. P001): P002
Caricamento CV per partecipante P002...
✓ CV caricato correttamente
  Nome: Laura Bianchi
  Skills tecniche: SPSS, R, Valutazione psicodiagnostica, Terapia cognitivo-comportamentale
Sessione iniziata per partecipante: P002
Log salvato in: logs/postural_interview_P002_*.csv
==============================================
```

## Se il CV Non è Disponibile

Se il file CV non esiste, il sistema:
- ⚠️ Avvisa che userà domande generiche
- Continua l'esperimento normalmente
- Usa domande non personalizzate

```
⚠ ATTENZIONE: CV non trovato. L'intervista userà domande generiche.
  Posiziona il CV in: cv/CV_P002.json
```

## Campi del CV

### Obbligatori

- `participantId`: ID univoco
- `personalInfo.name`: Nome
- `personalInfo.surname`: Cognome
- `education`: Almeno un titolo di studio
- `workExperience`: Almeno un'esperienza (può essere "Nessuna esperienza")
- `skills.technical`: Almeno 1-2 competenze tecniche (cruciale per lo stressor!)

### Opzionali

- `personalInfo.age`: Età (non usata nell'intervista)
- `skills.languages`: Lingue parlate (non usate direttamente)
- `skills.soft`: Soft skills (non usate direttamente)
- `projects`: Progetti (non usati direttamente, ma arricchiscono il profilo)

## Come Viene Usato il CV

### Fase Warmup

**Domanda 1 - Esperienza**

**Con CV:**
```
"Vedo che ha lavorato come Psicologa tirocinante presso Studio Psicologico XYZ. 
Mi racconti di questa esperienza."
```

**Senza CV:**
```
"Per cominciare, mi descriva la sua ultima esperienza lavorativa o di studio."
```

### Fase Stress - Competency Challenge

**Con CV (skill casuale dal CV):**
```
"Nel suo portfolio vedo che ha dichiarato competenze in SPSS.
Tuttavia, dalle sue risposte precedenti, mi sembra una competenza piuttosto basilare.
Può convincermi che è davvero esperto in SPSS come sostiene?"
```

**Senza CV (generico):**
```
"Nel suo portfolio vedo che ha dichiarato competenze in diverse aree.
Tuttavia, dalle sue risposte precedenti, mi sembrano competenze piuttosto basilari.
Può convincermi che è davvero esperto come sostiene?"
```

## Logging

Il sistema registra anche quale skill è stata "sfidata":

```csv
timestamp_ms,elapsed_ms,event_type,event_name,details
1234570000,2000,CV_INFO,SKILL_CHALLENGED,"SPSS"
1234570001,2001,STRESSOR,COMPETENCY_CHALLENGE,"Questioning expertise"
```

Questo permette di correlare la risposta EMG con la specifica competenza messa in discussione.

## Suggerimenti per CV Realistici

### Skills Tecniche Efficaci

Scegli skill **specifiche e verificabili**:

✅ **BUONE**:
- Software specifici: "Python", "SPSS", "AutoCAD", "Photoshop"
- Tecnologie: "React", "Machine Learning", "SQL"
- Metodologie: "Agile", "Design Thinking", "Terapia CBT"

❌ **EVITARE** (troppo vaghe):
- "Informatica"
- "Comunicazione"
- "Creatività"

### Numero di Skills

**Raccomandato**: 4-8 skills tecniche

- Troppo poche (1-2): Lo stressor perde varietà
- Troppo molte (10+): CV poco credibile

### Coerenza

Assicurati che:
- Le skills siano **coerenti** con education e work experience
- Il livello di seniority sia realistico per l'età
- I progetti usino tecnologie presenti nelle skills

## Esempi di CV per Diversi Profili

### Profilo IT Junior (2 anni exp)

```json
"skills": {
  "technical": ["Java", "SQL", "Git", "Spring Boot", "HTML/CSS"]
}
```

### Profilo Psicologo (neolaureato)

```json
"skills": {
  "technical": ["SPSS", "Somministrazione test", "Colloquio clinico", "Mindfullness"]
}
```

### Profilo Ingegnere Meccanico

```json
"skills": {
  "technical": ["AutoCAD", "SolidWorks", "MATLAB", "Analisi FEM", "Stampa 3D"]
}
```

### Profilo Designer

```json
"skills": {
  "technical": ["Photoshop", "Illustrator", "Figma", "UI/UX Design", "Prototipazione"]
}
```

## Troubleshooting

### "Unresolved reference: google"
La dipendenza Gson non è stata caricata. Esegui:
```bash
./gradlew clean build
```

### "CV non trovato"
- Verifica che il file esista in `cv/`
- Verifica il nome: `CV_P001.json` (non `cv_p001.json` o `CV_P001.JSON`)
- Verifica che l'ID sia corretto

### "JSON parsing error"
Il JSON è malformato. Usa un validatore online:
- https://jsonlint.com/
- Controlla virgole, parentesi, virgolette

### Skills non vengono usate
Verifica che:
- `skills.technical` sia un array: `["skill1", "skill2"]`
- Non sia vuoto: `[]`
- Non contenga null o stringhe vuote

## Testing

Per testare il sistema CV senza far partire Furhat:

```kotlin
// In Kotlin REPL o test
import furhatos.app.posturalinterview.data.CVLoader

val cv = CVLoader.loadCVByParticipantId("P001")
println(cv?.personalInfo?.name)
println(cv?.skills?.technical)
```
