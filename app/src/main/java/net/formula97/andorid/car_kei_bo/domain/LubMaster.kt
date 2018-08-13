package net.formula97.andorid.car_kei_bo.domain

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import java.sql.Date

@Entity(tableName = "CAR_MASTER")
data class LubMaster(
        /**
         * PKとなるレコードの一意ID
         */
        @PrimaryKey(autoGenerate = true)
        @ColumnInfo(name = "RECORD_ID")
        var recordId: Long,
        /**
         * 作成された日時
         */
        @ColumnInfo(name = "DATE")
        var date: Date,
        /**
         * 記録が紐づくクルマのID
         */
        @ColumnInfo(name = "CAR_ID")
        var carId: Int,
        /**
         * 給油量
         */
        @ColumnInfo(name = "LUB_AMOUNT")
        var lubAmount: Double,
        /**
         * 給油時単価
         */
        @ColumnInfo(name = "UNIT_PRICE")
        var unitPrice: Double,
        /**
         * 給油時オドメーター値
         */
        @ColumnInfo(name = "ODOMETER")
        var odometer: Double,
        /**
         * コメント
         */
        @ColumnInfo(name = "COMMENTS")
        var comments: String
)