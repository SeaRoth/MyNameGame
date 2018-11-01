package wt.cr.com.mynamegame.infrastructure.network

import retrofit2.http.GET
import io.reactivex.Observable
import io.reactivex.Single
import wt.cr.com.mynamegame.domain.model.Query

interface ApiClient {
    @GET("profiles.json")
    fun getProfiles(): Observable<Query>

    @GET("profiles.json")
    fun getProfilesNew(): Single<ProfileResponse>
}