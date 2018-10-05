package wt.cr.com.mynamegame.infrastructure.network.client

import retrofit2.http.GET
import wt.cr.com.mynamegame.domain.model.MyModel
import io.reactivex.Observable

interface ApiClient {

    @GET("profiles.json")
    fun getProfiles(): Observable<MyModel.Query>
}