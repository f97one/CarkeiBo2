package net.formula97.andorid.car_kei_bo.di.modules

import android.app.Application
import android.arch.persistence.db.SupportSQLiteDatabase
import android.arch.persistence.room.Room
import android.arch.persistence.room.migration.Migration

import javax.inject.Singleton

import dagger.Module
import dagger.Provides
import net.formula97.andorid.car_kei_bo.data.CarMasterDao
import net.formula97.andorid.car_kei_bo.data.CostsMasterDao
import net.formula97.andorid.car_kei_bo.data.LubMasterDao
import net.formula97.andorid.car_kei_bo.repository.AppDatabase

@Module
class AppModule(internal var application: Application) {

    lateinit private var appDatabase: AppDatabase

    @Provides
    @Singleton
    internal fun providesApplication(): Application {
        return this.application
    }

    // Room Persistence Library
    /**
     * RoomDatabaseのインスタンスをシングルトンで返す。
     *
     * @param application ApplicationContextを得るためのapplicationインスタンス
     */
    @Provides
    @Singleton
    fun providesAppDatabase(application: Application) : AppDatabase {
        if (appDatabase == null) {
            appDatabase = Room.databaseBuilder(
                    application.applicationContext, AppDatabase::class.java,
                    AppDatabase.DATABASE_FILENAME)
                    .addMigrations(MIGRATION_1_2)
                    .build()
        }

        return appDatabase
    }

    companion object {

        /**
         * 1 から 2へのマイグレーション<br></br>
         * スキーマ管理テーブルを追加するので、特に何も処理をしない。
         */
        internal val MIGRATION_1_2: Migration = object : Migration(1, 2) {

            override fun migrate(database: SupportSQLiteDatabase) {

            }
        }
    }

}
