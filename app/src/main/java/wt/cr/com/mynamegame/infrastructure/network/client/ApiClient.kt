package wt.cr.com.mynamegame.infrastructure.network.client

import retrofit2.http.GET
import wt.cr.com.mynamegame.domain.model.MyModel
import io.reactivex.Observable
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory


interface ApiClient {

    @GET("profiles.json")
    fun getProfiles(): Observable<MyModel.Result>
}