package furhatos.app.posturalinterview.nlu

import furhatos.nlu.*
import furhatos.nlu.common.Number
import furhatos.util.Language

/**
 * Intent for describing work/study experiences
 */
class DescribeExperience : Intent() {
    override fun getExamples(lang: Language): List<String> {
        return listOf(
            "Ho lavorato come @profession per @duration",
            "La mia ultima esperienza è stata in @field",
            "Ho studiato @subject all'università",
            "Ho fatto uno stage in @company",
            "Attualmente lavoro come @role"
        )
    }
}

/**
 * Intent for answering competency questions
 */
class ExplainCompetency : Intent() {
    override fun getExamples(lang: Language): List<String> {
        return listOf(
            "Ho esperienza con @skill da @duration",
            "Sono esperto in @technology",
            "Ho completato diversi progetti usando @tool",
            "Posso dimostrare le mie competenze con @example",
            "Ho certificazioni in @area"
        )
    }
}

/**
 * Intent for expressing uncertainty or difficulty
 */
class ExpressUncertainty : Intent() {
    override fun getExamples(lang: Language): List<String> {
        return listOf(
            "Non sono sicuro",
            "Devo pensarci",
            "È una domanda difficile",
            "Non saprei",
            "Fammi riflettere",
            "Mmm...",
            "Ehm...",
            "Cioè..."
        )
    }
}

/**
 * Intent for solving the brick problem
 */
class SolveBrickProblem : Intent() {
    var answerText: String? = null
    
    override fun getExamples(lang: Language): List<String> {
        return listOf(
            "tre chili",
            "tre",
            "3",
            "tre chili e mezzo",
            "sono tre chili",
            "la risposta è tre",
            "pesa tre chili",
            "due",
            "quattro",
            "uno",
            "cinque"
        )
    }
}

/**
 * Intent for ready confirmation
 */
class ReadyConfirmation : Intent() {
    override fun getExamples(lang: Language): List<String> {
        return listOf(
            "Sono pronto",
            "Sì, pronto",
            "Possiamo iniziare",
            "Va bene",
            "Ok"
        )
    }
}

/**
 * Intent for answering about physical sensations
 */
class PhysicalSensation : Intent() {
    var bodyPart: String? = null
    
    override fun getExamples(lang: Language): List<String> {
        return listOf(
            "Ho sentito tensione alle @bodyPart",
            "Mi facevano male le @bodyPart",
            "Tensione alla @bodyPart",
            "Nessuna tensione particolare",
            "Mi sentivo rilassato"
        )
    }
}
