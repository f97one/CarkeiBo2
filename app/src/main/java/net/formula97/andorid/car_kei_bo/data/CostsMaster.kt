package net.formula97.andorid.car_kei_bo.data

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import android.arch.persistence.room.TypeConverters
import net.formula97.andorid.car_kei_bo.data.converter.AppTypeConverter
import java.io.Serializable
import java.util.*

@Entity(tableName = "COSTS_MASTER")
@TypeConverters(value = [AppTypeConverter::class])
data class CostsMaster(
        @PrimaryKey(autoGenerate = true)
        @ColumnInfo(name = "RECORD_ID")
        var recordId: Int,
        @ColumnInfo(name = "CAR_ID")
        var carId: Int,
        @ColumnInfo(name = "REFUEL_DATE")
        var refuelDate: Date,
        @ColumnInfo(name = "RUNNING_COST")
        var runningCost: Double
) : Cloneable, Serializable {
        public override fun clone(): Any {
                return super.clone()
        }
}