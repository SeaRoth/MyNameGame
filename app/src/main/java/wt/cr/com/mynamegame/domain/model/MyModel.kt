package wt.cr.com.mynamegame.domain.model

object MyModel {
    data class Result(val query: Query)
    data class Query(val data: ArrayList<Person>)
    data class Person(val id: String, val firstName: String, val headshot: Headshot)
    data class Headshot(val url: String)
}