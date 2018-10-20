package wt.cr.com.mynamegame.domain.model

import wt.cr.com.mynamegame.infrastructure.ui.home.PersonViewModel

object MyModel {
    data class Result(val query: Query)
    data class Query(val data: ArrayList<Person>)
    data class Person(
            val id: String,
            val firstName: String,
            val lastName: String,
            val headshot: Headshot,
            val socialLinks: ArrayList<Social>
    )
    data class Headshot(val url: String?)
    data class Social(val type: String, val callToAction: String, val url: String)
    data class Score(
            val current: Int,
            val high: Int,
            val mostKnownPerson: String?
    )
}