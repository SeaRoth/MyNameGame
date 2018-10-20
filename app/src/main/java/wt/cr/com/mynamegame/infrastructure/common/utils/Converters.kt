package wt.cr.com.mynamegame.infrastructure.common.utils

import android.arch.persistence.room.TypeConverter
import wt.cr.com.mynamegame.domain.model.MyModel

class Converters {

    @TypeConverter
    fun fromHeadshot(headshot: MyModel.Headshot): String {
        return "${headshot.url}"
    }

    @TypeConverter
    fun toHeadshot(str: String) : MyModel.Headshot {
        return MyModel.Headshot(str)
    }
}