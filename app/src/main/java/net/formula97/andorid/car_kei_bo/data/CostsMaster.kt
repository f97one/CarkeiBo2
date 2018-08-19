package net.formula97.andorid.car_kei_bo.data

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import java.sql.Date

@Entity(tableName = "COSTS_MASTER")
data class CostsMaster(
        @PrimaryKey(autoGenerate = true)
        @ColumnInfo(name = "RECORD_ID")
        var recordId: Int,
        @ColumnInfo(name = "CAR_ID")
        var carId: Int,
        @ColumnInfo(name = "REFUEL_DATE")
        var refuelDouble: Double,
        @ColumnInfo(name = "RUNNING_COST")
        var runningCost: Double
) : Cloneable {
        public override fun clone(): Any {
                return super.clone()
        }
}