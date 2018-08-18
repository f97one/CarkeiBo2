package net.formula97.andorid.car_kei_bo

import android.app.Application
import android.arch.persistence.room.Room
import net.formula97.andorid.car_kei_bo.repository.AppDatabase

class TheApp : Application() {

    lateinit var appDatabase: AppDatabase

    override fun onCreate() {
        super.onCreate()

        // Room の初期化
        appDatabase = Room.databaseBuilder(applicationContext, AppDatabase::class.java, AppDatabase.DATABASE_FILENAME)
                .addMigrations(AppDatabase.MIGRATION_1_2)
                .build()
    }

    /**
     * ローカルデータソースとしてSQLite Databaseを取得する。
     */
    fun getLocalDatasource() : AppDatabase {
        return this.appDatabase
    }
}