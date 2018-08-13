package net.formula97.andorid.car_kei_bo.domain

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import java.sql.Date

@Entity
data class CostsMaster(
        @PrimaryKey(autoGenerate = true)
        @ColumnInfo(name = "RECORD_ID")
        var recordId: Long,
        @ColumnInfo(name = "DATE")
        var date: Date,
        @ColumnInfo(name = "RUNNING_COST")
        var runningCost: Double
)