package wt.cr.com.mynamegame.domain.model

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

object MyModel {
    data class Result(val query: Query)
    data class Query(val data: ArrayList<Person>)

    @Entity(tableName = "Person")
    data class Person(
            @PrimaryKey(autoGenerate = false) @ColumnInfo(name = "id") val id: String,
            @ColumnInfo(name = "firstName") val firstName: String,
            @ColumnInfo(name = "lastName") val lastName: String,
            @ColumnInfo(name = "headshot") val headshot: Headshot?,
            @ColumnInfo(name = "socialLinks") val socialLinks: ArrayList<Social>) {
        constructor() : this("","","", Headshot(""), ArrayList())
    }
    data class Headshot(val url: String?)
    data class Social(val type: String, val callToAction: String, val url: String)
    data class Score(
            val current: Int,
            val high: Int,
            val mostKnownPerson: String?,
            val lifetimeCorrect: Int,
            val lifetimeIncorrect: Int
    )
}