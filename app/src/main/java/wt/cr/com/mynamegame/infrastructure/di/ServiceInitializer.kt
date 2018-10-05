package wt.cr.com.mynamegame.infrastructure.di

import android.app.Application
import kotlinx.coroutines.experimental.CoroutineDispatcher
import kotlinx.coroutines.experimental.android.UI
import wt.cr.com.mynamegame.infrastructure.repository.HumanRepo
import wt.cr.com.mynamegame.infrastructure.repository.HumanRepository

class ServiceInitializer {
    companion object {
        fun initServices(application: Application) {
            initApplication(application)
            initCoroutineContext()
            initRepositories()
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
