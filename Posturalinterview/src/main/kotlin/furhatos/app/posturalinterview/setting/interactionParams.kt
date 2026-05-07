package furhatos.app.posturalinterview.setting

import furhatos.flow.kotlin.voice.Voice

/** Engagement parameters */
const val MAX_NUMBER_OF_USERS = 2 // Max amount of people that Furhat will recognize as users simultaneously
const val DISTANCE_TO_ENGAGE = 1.0 // Min distance for people to be recognised as users

/** Speech recognition parameters */
const val DEFAULT_LISTEN_TIMEOUT = 10000 // 10 secondi invece di default
const val NO_SPEECH_TIMEOUT = 5000 // 5 secondi di silenzio prima di timeout
const val MAX_SPEECH_TIMEOUT = 30000 // Massimo 30 secondi di ascolto

/** Language settings */
val ITALIAN_VOICE = Voice(language = furhatos.util.Language.ITALIAN) // Voce italiana per TTS