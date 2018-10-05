package wt.cr.com.mynamegame.domain.model

object MyModel {
    data class Result(val query: Query)
    data class Query(val coworker: List<Person>)
    data class Person(val id: String, val firstName: String)
}