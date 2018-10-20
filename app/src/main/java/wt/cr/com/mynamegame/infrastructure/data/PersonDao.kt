package wt.cr.com.mynamegame.infrastructure.data

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import wt.cr.com.mynamegame.domain.model.MyModel

@Dao
interface PersonDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPersonData(person: MyModel.Person): Long

    @Query("SELECT * FROM Person")
    fun getAll(): LiveData<MutableList<MyModel.Person>>
}