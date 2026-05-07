package furhatos.app.posturalinterview

import furhatos.app.posturalinterview.flow.Init
import furhatos.flow.kotlin.Flow
import furhatos.skills.Skill

class PosturalinterviewSkill : Skill() {
    override fun start() {
        Flow().run(Init)
    }
}

fun main(args: Array<String>) {
    Skill.main(args)
}
