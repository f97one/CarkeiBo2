package net.formula97.andorid.car_kei_bo.repository

import android.arch.persistence.db.SupportSQLiteDatabase
import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.migration.Migration

import net.formula97.andorid.car_kei_bo.data.CarMaster
import net.formula97.andorid.car_kei_bo.data.CarMasterDao
import net.formula97.andorid.car_kei_bo.data.CostsMaster
import net.formula97.andorid.car_kei_bo.data.CostsMasterDao
import net.formula97.andorid.car_kei_bo.data.LubMaster
import net.formula97.andorid.car_kei_bo.data.LubMasterDao

@Database(entities = [CarMaster::class, CostsMaster::class, LubMaster::class],
        version = AppDatabase.CURRENT_DATABASE_VERSION)
abstract class AppDatabase : RoomDatabase() {

    abstract fun carMasterDao(): CarMasterDao

    abstract fun costsMasterDao(): CostsMasterDao

    abstract fun lubMasterDao(): LubMasterDao

    companion object {
        const val CURRENT_DATABASE_VERSION = 2
        const val DATABASE_FILENAME = "fuel_mileage.db"

        /**
         * 1 → 2 のマイグレーション
         *
         * マイグレーション管理テーブルを作るだけなので、特に何もしない。
         */
        val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {

            }
        }

    }
}
