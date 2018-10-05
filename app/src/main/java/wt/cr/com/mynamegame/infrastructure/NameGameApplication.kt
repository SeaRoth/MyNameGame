package wt.cr.com.mynamegame.infrastructure

import android.app.Application
import com.jakewharton.threetenabp.AndroidThreeTen
import timber.log.Timber
import wt.cr.com.mynamegame.infrastructure.di.ServiceInitializer

class NameGameApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
        AndroidThreeTen.init(this);
        ServiceInitializer.initServices(this)
    }
}