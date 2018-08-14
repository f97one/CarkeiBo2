package net.formula97.andorid.car_kei_bo

import android.text.format.Time
import android.util.Log
import java.text.SimpleDateFormat
import java.util.*

/**
 * 日付処理に関するメソッドを管理するクラス
 * @author f97one
 */
class DateManager {

    /**
     * 現在日時を返す。
     * @return Calendar型、現在のロケールにおける現在日時
     */
    val now: Calendar
        get() = Calendar.getInstance()

    /**
     * ISO 8601形式の日付、および時刻フォーマットを持つ「文字列」を返す。
     * @param gcd Calendar型（日付）
     * @param withTime  boolean型、時刻が必要なときはtrue、不要なときはfalse
     * @return ISO 8601形式の文字列
     */
    fun getISO8601Date(gcd: Calendar, withTime: Boolean): String {
        // SimpleDateFormatが使いたいので、Calendar型の引数をDate型に変換する
        val dd = gcd.time

        // withTime引数の値によって、日付の書式を決める
        val dateFormat: String

        if (withTime) {
            dateFormat = "yyyy-MM-dd HH:mm:ss"
        } else {
            dateFormat = "yyyy-MM-dd"
        }

        val sdf = SimpleDateFormat(dateFormat)

        // 整形済み日付をStringにして返す
        return sdf.format(dd).toString()
    }

    /**
     * ISO 8601形式の日付、および時刻フォーマットを持つ「文字列」を返す。
     * @param julianDay double型、ユリウス通日表記の日付
     * @return ISO 8601形式の文字列
     */
    fun getISO8601Date(julianDay: Double): String {
        // ユリウス通日に対応するDate型オブジェクトを取得する
        //   milliSecOfDayは1日をミリ秒単位にしたものを、
        //   originDateは1970年1月1日 00:00:00 UTCを、それぞれあらわす。
        //   ※ちなみにこの方法を使えば、Calendar→ユリウス通日への変換も2、3行で記述できるんだが....。
        val milliSecOfDay = 86400000
        val originDate = 2440587.5

        val dateFromJ = ((julianDay - originDate) * milliSecOfDay).toLong()

        // 端末のタイムゾーン設定を取得する
        val currentTZ = Time.getCurrentTimezone()
        Log.d("getISO8601Date", "Current Time zone is $currentTZ")
        val current = TimeZone.getTimeZone(currentTZ)
        val offsetInMillis = current.rawOffset
        val offsetHour = offsetInMillis / 1000 / 60 / 60

        val cal = Calendar.getInstance()
        cal.timeInMillis = dateFromJ
        cal.set(Calendar.HOUR_OF_DAY, cal.get(Calendar.HOUR_OF_DAY) - offsetHour)

        return getISO8601Date(cal, true)
    }

    /**
     * 渡された日時をユリウス通日に変換する
     * @param gcd Calendar型（日付）
     * @return double型、SQLiteが認識可能なユリウス通日
     */
    fun toJulianDay(gcd: Calendar): Double {
        val jDate: Double                            // ユリウス通日
        val jYear: Int
        val jMonth: Int
        val jDay: Int = gcd.get(Calendar.DAY_OF_MONTH)                // ユリウス通日計算の元となる年、月、日
        val jHour: Double = gcd.get(Calendar.HOUR_OF_DAY).toDouble()
        val jMinute: Double = gcd.get(Calendar.MINUTE).toDouble()
        val jSecond: Double = gcd.get(Calendar.SECOND).toDouble()        // 同  時間、分、秒
        val a: Int
        val b: Int                                // グレゴリオ暦における、うるう年補正値

        // ユリウス通日を求める上で必要な「年」と「月」のセット
        //   グレゴリオ暦の「月」が2より大きい（＝Calendar.MONTHが1より大きい）場合は、
        //   そのままjYear, jMonthに年と月をセット
        if (gcd.get(Calendar.MONTH) > 1) {
            jYear = gcd.get(Calendar.YEAR)
            jMonth = gcd.get(Calendar.MONTH) + 1
        } else {
            // グレゴリオ暦の「月」が2以下の場合は、jYearを-1、jMonthを+12する
            jYear = gcd.get(Calendar.YEAR) - 1
            jMonth = gcd.get(Calendar.MONTH) + 13
        }
        // 日、時間、分、秒をセット
        // 時間、分、秒については、doubleで計算する必要があるのでdoubleでキャストする。

        // うるう年補正値の計算
        a = jYear / 100
        b = 2 - a + a / 4

        // グレゴリオ暦→ユリウス通日への変換公式は、以下のとおり。
        // JD = INT(365.25 y) + INT(30.6001 ( m + 1) ) + DD + (hh/24) + 1720994.5 + B
        //   ※ここでは、分、秒を考慮するため、jHourにjMinute/60とjSecond/3600を加算している。
        val tag = "toJulianDay"
        Log.d(tag, "Year : " + Math.floor(365.25 * jYear))
        Log.d(tag, "Month : " + Math.floor(30.6001 * (jMonth + 1)))
        Log.d(tag, "Day : $jDay")
        val jh = (jHour + jMinute / 60 + jSecond / 3600) / 24
        Log.d(tag, "Hour : " + jh.toString())

        jDate = (365.25 * jYear).toInt().toDouble() +
                (30.6001 * (jMonth + 1)).toInt().toDouble() +
                jDay.toDouble() + jh +
                1720994.5 + b.toDouble()

        return jDate
    }

    /**
     * ISO 8601形式の日時文字列をユリウス通日に変換する。
     * @param iso8601Date String型、変換元のISO 8601形式の日時文字列
     * @return double型、SQLiteが認識可能なユリウス通日
     */
    fun toJulianDay(iso8601Date: String): Double {
        var ret = 0.0

        // ISO 8601形式の日時を分解
        Log.d("toJulianDay", "Input string = $iso8601Date")
        val elementDate = iso8601Date.split("[-: ]".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        val year = Integer.parseInt(elementDate[0])
        val month = Integer.parseInt(elementDate[1])
        val day = Integer.parseInt(elementDate[2])
        val hour = Integer.parseInt(elementDate[3])
        val minute = Integer.parseInt(elementDate[4])
        val second = Integer.parseInt(elementDate[5])

        Log.d("toJulianDay", "year = " + year.toString())
        Log.d("toJulianDay", "month = " + month.toString())
        Log.d("toJulianDay", "day = " + day.toString())
        Log.d("toJulianDay", "hour = " + hour.toString())
        Log.d("toJulianDay", "minute = " + minute.toString())
        Log.d("toJulianDay", "second = " + second.toString())

        val currentDay = Calendar.getInstance()
        currentDay.set(year, month, day, hour, minute, second)

        // 本当は
        //ret = currentDay.getTimeInMillis() / 86400000 + 2440587.5;
        // とやれば終わるのだが、計算結果の一貫性を保つためtoJulianDay(Calendar)を
        // 呼び出す。
        ret = toJulianDay(currentDay)

        return ret
    }

    /**
     * ユリウス通日をCalendarオブジェクトに変換する。
     * @param julianDay 変換元のユリウス通日
     * @return Calendar型、ユリウス通日から換算したCalendarオブジェクト、
     * ただし、ミリ秒以下は「000」になっている
     */
    fun jd2Calendar(julianDay: Double): Calendar {
        // GMT+0:00とされているカサブランカのタイムゾーン文字列
        //String strGmt = "Africa/Casablanca";

        val currentTZ = Time.getCurrentTimezone()
        //TimeZone gmtTz = TimeZone.getTimeZone(strGmt);
        val current = TimeZone.getTimeZone(currentTZ)

        // UTC(≒GMT)からの時差を取得する
        val rawOffsetInMillis = current.rawOffset
        // オフセットはミリ秒単位なので、時間単位に計算しなおす
        val rawOffsetHour = rawOffsetInMillis / 1000 / 60 / 60
        Log.d("jd2Calendar", "Offset hour from GMT is " + rawOffsetHour.toString())

        //Calendar ret = Calendar.getInstance(gmtTz);
        val ret = Calendar.getInstance()

        // ユリウス通日に対応するDate型オブジェクトを取得する
        //   milliSecOfDayは1日をミリ秒単位にしたものを、
        //   originDateは1970年1月1日 00:00:00 UTCを、それぞれあらわす。
        //   ※ちなみにこの方法を使えば、Calendar→ユリウス通日への変換も2、3行で記述できるんだが....。
        val milliSecOfDay = 86400000
        val originDate = 2440587.5

        // 都合上、long型にキャストする必要があるのだが、ミリ秒単位がどうも切り捨てられてるっぽい....
        val dayMilli = ((julianDay - originDate) * milliSecOfDay).toLong()
        ret.timeInMillis = dayMilli
        //ret.setTimeZone(current);

        // 取得したオフセットだけ、時間を減算する
        ret.set(Calendar.HOUR_OF_DAY, ret.get(Calendar.HOUR_OF_DAY) - rawOffsetHour)

        return ret
    }

    /**
     * ISO 8601形式の日付文字列をCalendarオブジェクトに変換する。
     * @param iso8601Date String型、変換元のISO 8601形式の日付文字列
     * @return Calendar型、ISO 8601形式の日付文字列から換算したCalendarオブジェクト、
     * ただし、ミリ秒以下は「000」になっている
     */
    fun iso2Calendar(iso8601Date: String): Calendar {
        val ret = Calendar.getInstance()

        val elementDate = iso8601Date.split("[-: ]".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        val year = Integer.parseInt(elementDate[0])
        val month = Integer.parseInt(elementDate[1])
        val day = Integer.parseInt(elementDate[2])
        val hour = Integer.parseInt(elementDate[3])
        val minute = Integer.parseInt(elementDate[4])
        val second = Integer.parseInt(elementDate[5])

        ret.set(year, month, day, hour, minute, second)
        ret.set(Calendar.MILLISECOND, 0)

        return ret
    }

    /**
     * その月の1日 00:00:00.000であるユリウス通日を返す。
     * @param gcd Calendar型、取得する月を含むCalendarオブジェクト
     * @return double型、その月の1日 00:00:00.000であるユリウス通日
     */
    fun getFirstMomentOfMonth(gcd: Calendar): Double {
        // 年と月はそのままとし、残りはとりうる最小値にする
        gcd.set(gcd.get(Calendar.YEAR), // 年
                gcd.get(Calendar.MONTH), // 月
                gcd.getActualMinimum(Calendar.DAY_OF_MONTH), // 日
                gcd.getActualMinimum(Calendar.HOUR_OF_DAY), // 時
                gcd.getActualMinimum(Calendar.MINUTE), // 分
                gcd.getActualMinimum(Calendar.SECOND))            // 秒
        gcd.set(Calendar.MILLISECOND, gcd.getActualMinimum(Calendar.MILLISECOND))

        return toJulianDay(gcd)
    }

    /**
     * その月の最終日 23:59:59.999であるユリウス通日を返す。
     * @param gcd Calendar型、取得する月を含むCalendarオブジェクト
     * @return double型、その月の最終日 23:59:59.999であるユリウス通日
     */
    fun getLastMomentOfMonth(gcd: Calendar): Double {
        // 年と月はそのままとし、残りはとりうる最大値にする
        gcd.set(gcd.get(Calendar.YEAR), // 年
                gcd.get(Calendar.MONTH), // 月
                gcd.getActualMaximum(Calendar.DAY_OF_MONTH), // 日
                gcd.getActualMaximum(Calendar.HOUR_OF_DAY), // 時
                gcd.getActualMaximum(Calendar.MINUTE), // 分
                gcd.getActualMaximum(Calendar.SECOND))            // 秒
        gcd.set(Calendar.MILLISECOND, gcd.getActualMaximum(Calendar.MILLISECOND))

        return toJulianDay(gcd)
    }

    companion object {

        // 便宜上表記が必要なときに使用する、特殊な境界線上の時刻表記
        internal val START_HOUR = "00:00:00"
        internal val END_HOUR = "23:59:59"
    }
}
