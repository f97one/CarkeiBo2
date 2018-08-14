package net.formula97.andorid.car_kei_bo.data

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity(tableName = "CAR_MASTER")
data class CarMaster(
        @PrimaryKey(autoGenerate = true)
        @ColumnInfo(name = "CAR_ID")
        var carId: Long,
        @ColumnInfo(name = "CAR_NAME")
        var carName: String
)