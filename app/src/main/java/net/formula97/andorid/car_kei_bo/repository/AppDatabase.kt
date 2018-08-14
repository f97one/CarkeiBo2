package net.formula97.andorid.car_kei_bo.repository

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase

import net.formula97.andorid.car_kei_bo.data.CarMaster
import net.formula97.andorid.car_kei_bo.data.CarMasterDao
import net.formula97.andorid.car_kei_bo.data.CostsMaster
import net.formula97.andorid.car_kei_bo.data.CostsMasterDao
import net.formula97.andorid.car_kei_bo.data.LubMaster
import net.formula97.andorid.car_kei_bo.data.LubMasterDao

@Database(entities = arrayOf(CarMaster::class, CostsMaster::class, LubMaster::class),
        version = AppDatabase.CURRENT_DATABASE_VERSION)
abstract class AppDatabase : RoomDatabase() {

    abstract fun carMasterDao(): CarMasterDao

    abstract fun costsMasterDao(): CostsMasterDao

    abstract fun lubMasterDao(): LubMasterDao

    companion object {
        const val CURRENT_DATABASE_VERSION = 2
        const val DATABASE_FILENAME = "fuel_mileage.db"
    }
}
