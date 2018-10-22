package wt.cr.com.mynamegame.infrastructure.di

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.OkHttp3Downloader
import com.squareup.picasso.Picasso
import kotlinx.coroutines.experimental.CoroutineDispatcher
import kotlinx.coroutines.experimental.android.UI
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import wt.cr.com.mynamegame.infrastructure.network.client.ApiClient
import wt.cr.com.mynamegame.infrastructure.repository.HumanRepo
import wt.cr.com.mynamegame.infrastructure.repository.HumanRepository
import wt.cr.com.mynamegame.infrastructure.ui.home.PREFS_SCORE

class ServiceInitializer {
    companion object {
        fun initServices(application: Application) {
            initApplication(application)
            initScoreSharedPreferences(application)
            initNetwork(application)
            initCoroutineContext()
            initRepositories()
            initFirestore(application)
        }

        private fun initNetwork(application: Context){
            val retrofit = Retrofit.Builder()
                    .baseUrl("http://randastat.com/")
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
            val apiClient = retrofit.create(ApiClient::class.java)
            WTServiceLocator.put(ApiClient::class.java, apiClient)

            application.let {
                val picasso = Picasso.Builder(it)
                        .downloader(OkHttp3Downloader(OkHttpClient.Builder()
                                .build()))
                        .build()
                WTServiceLocator.put(Picasso::class.java, picasso)
            }
        }

        private fun initFirestore(application: Context){
            var db = FirebaseFirestore.getInstance()
            WTServiceLocator.put(FirebaseFirestore::class.java, db)
        }

        private fun initScoreSharedPreferences(application: Application) {
            WTServiceLocator.put(SharedPreferences::class.java, application.getSharedPreferences(PREFS_SCORE, 0))
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
