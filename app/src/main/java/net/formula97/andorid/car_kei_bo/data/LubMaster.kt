package net.formula97.andorid.car_kei_bo.data

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import android.arch.persistence.room.TypeConverters
import net.formula97.andorid.car_kei_bo.data.converter.AppTypeConverter
import java.io.Serializable
import java.util.Date

@Entity(tableName = "LUB_MASTER")
@TypeConverters(value = [AppTypeConverter::class])
data class LubMaster(
        /**
         * PKとなるレコードの一意ID
         */
        @PrimaryKey(autoGenerate = true)
        @ColumnInfo(name = "RECORD_ID")
        var recordId: Int?,
        /**
         * 作成された日時
         */
        @ColumnInfo(name = "REFUEL_DATE")
        var refuelDate: Date,
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
        @ColumnInfo(name = "TRIPMETER")
        var tripMeter: Double,
        /**
         * コメント
         */
        @ColumnInfo(name = "COMMENTS")
        var comments: String
) : Cloneable, Serializable {
        public override fun clone(): Any {
                return super.clone()
        }
}