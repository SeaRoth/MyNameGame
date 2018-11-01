package wt.cr.com.mynamegame.infrastructure.network.storage

import android.arch.persistence.room.*

@Entity(
        tableName = "profiles",
        indices = [
            Index(
                    value = ["id"],
                    name = "idx_id"
            ),
            Index(
                    value = ["first_name"],
                    name = "idx_first_name"
            ),
            Index(
                    value = ["last_name"],
                    name = "idx_last_name"
            ),
            Index(
                    value = ["url"],
                    name = "idx_url"
            )
        ]
)
data class ProfileEntity(
        @PrimaryKey(autoGenerate = false)
        @ColumnInfo(name = "id")
        var id: String,

        @ColumnInfo(name = "firstName")
        var firstName: String,

        @ColumnInfo(name = "lastName")
        var lastName: String,

        @ColumnInfo(name = "url")
        var url: String
)