package net.formula97.andorid.car_kei_bo.data.converter

import android.arch.persistence.room.TypeConverter
import net.formula97.andorid.car_kei_bo.logic.DateManager
import java.util.*

/**
 * アプリ固有のTypeConverter。
 */
class AppTypeConverter {

    /**
     * Boolean.TRUE を 1 に、それ以外を 0 に、それぞれ変換する。
     *
     * @param value 変換したいBoolean
     */
    @TypeConverter
    fun fromBooleanToInt(value: Boolean): Int {
        return if (value) 1 else 0
    }

    /**
     * 1: Int を true に、それ以外を false に変換する。
     *
     * @param value 変換したいInt
     */
    @TypeConverter
    fun fromIntToBoolean(value: Int): Boolean {
        return value == 1
    }

    @TypeConverter
    fun fromDateToReal(value: Date): Double {
        val cal: Calendar = Calendar.getInstance()
        cal.time = value

        return DateManager().toJulianDay(cal)
    }

    @TypeConverter
    fun fromRealToDate(value: Double): Date {
        val cal = DateManager().jd2Calendar(value)

        return cal.time
    }
}