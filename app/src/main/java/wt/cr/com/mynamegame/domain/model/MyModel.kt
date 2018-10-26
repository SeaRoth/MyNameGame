package wt.cr.com.mynamegame.domain.model

object MyModel {
    data class Result(val query: Query)
    data class Query(val data: ArrayList<Person>)
    data class Person(
            val id: String,
            val firstName: String,
            val lastName: String,
            val headshot: Headshot?,
            val socialLinks: ArrayList<Social>)
    data class Headshot(val url: String?)
    data class Social(val type: String, val callToAction: String, val url: String)
    data class Score(
            val current: Int,
            val high: Int,
            val mostKnownPerson: String?,
            val lifetimeCorrect: Int,
            val lifetimeIncorrect: Int
    )
    data class Player(
            val name: String,
            val location: String?,
            val highScore: Int,
            val twoCents: String
    )
}