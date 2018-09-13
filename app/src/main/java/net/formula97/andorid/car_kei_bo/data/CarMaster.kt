package net.formula97.andorid.car_kei_bo.data

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "CAR_MASTER")
data class CarMaster(
        @PrimaryKey(autoGenerate = true)
        @ColumnInfo(name = "CAR_ID")
        var carId: Int?,
        @ColumnInfo(name = "CAR_NAME")
        var carName: String,
        @ColumnInfo(name = "DEFAULT_FLAG")
        var defaultFlag: Boolean,
        @ColumnInfo(name = "CURRENT_FUEL_MILEAGE")
        var currentFuelMileage: Double,
        @ColumnInfo(name = "CURRENT_RUNNING_COST")
        var currentRunningCost: Double,
        @ColumnInfo(name = "PRICEUNIT")
        var priceUnit: String,
        @ColumnInfo(name = "DISTANCEUNIT")
        var distanceUnit: String,
        @ColumnInfo(name = "VOLUMEUNIT")
        var volumeUnit: String,
        @ColumnInfo(name = "FUELMILEAGE_LABEL")
        var fuelMileageLabel: String,
        @ColumnInfo(name = "RUNNINGCOST_LABEL")
        var runningCostLabel: String
) : Cloneable, Serializable {
        public override fun clone(): Any {
                return super.clone()
        }
}