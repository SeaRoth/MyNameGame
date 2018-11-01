package wt.cr.com.mynamegame.domain.model


data class Result(
        val query: Query
)
data class Query(
        val data: ArrayList<Profile>
)
data class Profile(
        val id: String,
        val firstName: String,
        val lastName: String,
        val headshot: Headshot?,
        val socialLinks: List<Social>?
)
data class Headshot(
        val url: String?
)
data class Social(
        val type: String,
        val callToAction: String,
        val url: String
)
data class Score(
        val current: Int,
        val high: Int,
        val mostKnownPerson: String?,
        val lifetimeCorrect: Int,
        val lifetimeIncorrect: Int
)
data class Player(
        val name: String,
        val email: String,
        val location: String,
        val highScore: Int,
        val twoCents: String
)

sealed class ProfileError {
    object NetworkError : ProfileError()
    object EmptyResultsError : ProfileError()
}