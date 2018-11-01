package wt.cr.com.mynamegame.infrastructure.network.storage

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase

@Database(
        entities = [
            ProfileEntity::class
        ],
        version = 1
)
abstract class ProfileDatabase : RoomDatabase() {
    abstract fun profileDao(): ProfileDao
}