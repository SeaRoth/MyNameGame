package wt.cr.com.mynamegame.infrastructure.network.storage

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query
import android.arch.persistence.room.Transaction
import io.reactivex.Flowable
import wt.cr.com.mynamegame.domain.model.Profile

@Dao
abstract class ProfileDao{
    @Query("SELECT * FROM profiles")
    abstract fun returnAllProfiles(): Flowable<List<ProfileEntity>>

    @Insert
    abstract fun insert(vararg profiles: ProfileEntity)

    @Transaction
    @Insert
    fun insert(profiles: List<Profile>) {
        val list = profiles.map {
            ProfileEntity(
                    id = it.id,
                    firstName = it.firstName,
                    lastName = it.lastName,
                    url = it.headshot?.url?:"")
        }
        insert(*list.toTypedArray())
    }
}