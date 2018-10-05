package wt.cr.com.mynamegame.infrastructure.di

import android.app.Application
import kotlinx.coroutines.experimental.CoroutineDispatcher
import kotlinx.coroutines.experimental.android.UI
import moe.banana.jsonapi2.JsonApiConverterFactory
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import wt.cr.com.mynamegame.infrastructure.network.client.ApiClient
import wt.cr.com.mynamegame.infrastructure.repository.HumanRepo
import wt.cr.com.mynamegame.infrastructure.repository.HumanRepository

class ServiceInitializer {
    companion object {
        fun initServices(application: Application) {
            initApplication(application)
            initNetwork()
            initCoroutineContext()
            initRepositories()
        }

        private fun initNetwork(){
            val retrofit = Retrofit.Builder()
                    .baseUrl("http://randastat.com/")
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
            val apiClient = retrofit.create(ApiClient::class.java)
            WTServiceLocator.put(ApiClient::class.java, apiClient)
        }

        private fun initCoroutineContext() {
            WTServiceLocator.put(CoroutineDispatcher::class.java, UI)
        }

        private fun initRepositories() {
            WTServiceLocator.put(HumanRepo::class.java, HumanRepository())
        }

        private fun initApplication(application: Application) {
            WTServiceLocator.put(Application::class.java, application)
        }
    }
}
