package wt.cr.com.mynamegame.infrastructure.di

import android.app.Application
import android.arch.persistence.room.Room
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
import wt.cr.com.mynamegame.infrastructure.network.ApiClient
import wt.cr.com.mynamegame.infrastructure.network.SearchLocalDataSource
import wt.cr.com.mynamegame.infrastructure.network.SearchRemoteDataSource
import wt.cr.com.mynamegame.infrastructure.network.storage.ProfileDatabase
import wt.cr.com.mynamegame.infrastructure.repository.HumanRepository
import wt.cr.com.mynamegame.infrastructure.ui.home.PREFS_SCORE

const val CONTEXT_PARAM: String = "context"
class ServiceInitializer {
    companion object {
        fun initServices(application: Application) {
            initApplication(application)
            initScoreSharedPreferences(application)
            initNetwork(application)
            initCoroutineContext()
            initRepositories(application)
            initFirestore(application)
        }

        private fun initNetwork(application: Context){
            application.let {
                val picasso = Picasso.Builder(it)
                        .downloader(OkHttp3Downloader(OkHttpClient.Builder()
                                .build()))
                        .build()
                WTServiceLocator.put(Picasso::class.java, picasso)
            }
        }

        private fun initFirestore(application: Context){
            val db = FirebaseFirestore.getInstance()
            WTServiceLocator.put(FirebaseFirestore::class.java, db)
        }

        private fun initScoreSharedPreferences(application: Application) {
            WTServiceLocator.put(SharedPreferences::class.java, application.getSharedPreferences(PREFS_SCORE, 0))
        }

        private fun initCoroutineContext() {
            WTServiceLocator.put(CoroutineDispatcher::class.java, UI)
        }

        private fun initRepositories(application: Context) {
            val retrofit = Retrofit.Builder()
                    .baseUrl("http://randastat.com/")
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
            val apiClient = retrofit.create(ApiClient::class.java)
            WTServiceLocator.put(ApiClient::class.java, apiClient)
            val db = Room.databaseBuilder(application, ProfileDatabase::class.java, "profiles").build()

            WTServiceLocator.put(HumanRepository::class.java,
                        HumanRepository(SearchLocalDataSource(db),SearchRemoteDataSource(apiClient)))

        }

        private fun initApplication(application: Application) {
            WTServiceLocator.put(Application::class.java, application)
        }

    }
}
