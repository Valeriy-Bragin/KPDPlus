package com.meriniguan.kpdplus.data.room

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import java.text.DateFormat

@Entity(
    indices = [Index("code")],
    tableName = "tools"
)
@Parcelize
data class Tool(
    val name: String,
    @ColumnInfo(collate = ColumnInfo.NOCASE) val code: String,
    @ColumnInfo(name = "holder_name") val holderName: String,
    val brand: String,
    @ColumnInfo(name = "photo_url") val photoUri: String = "empty",
    @ColumnInfo(name = "date_created")val dateCreated: Long = System.currentTimeMillis(),
    @PrimaryKey(autoGenerate = true) val id: Int = 0
) : Parcelable {

    val dateCreatedFormatted: String
        get() = DateFormat.getDateTimeInstance().format(dateCreated)

    fun hasPhoto(): Boolean = (photoUri != "empty") && (photoUri != "")
}