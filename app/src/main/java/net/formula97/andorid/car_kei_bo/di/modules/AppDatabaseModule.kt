package net.formula97.andorid.car_kei_bo.di.modules

import android.app.Application
import android.arch.persistence.db.SupportSQLiteDatabase
import android.arch.persistence.room.Room
import android.arch.persistence.room.migration.Migration

import net.formula97.andorid.car_kei_bo.data.CarMasterDao
import net.formula97.andorid.car_kei_bo.data.CostsMasterDao
import net.formula97.andorid.car_kei_bo.data.LubMasterDao
import net.formula97.andorid.car_kei_bo.repository.AppDatabase

import javax.inject.Singleton

import dagger.Module
import dagger.Provides

@Module
class AppDatabaseModule(application: Application) {

    private val appDatabase: AppDatabase

    init {
        appDatabase = Room.databaseBuilder(application.applicationContext, AppDatabase::class.java, AppDatabase.DATABASE_FILENAME)
                .addMigrations(MIGRATION_1_2)
                .build()
    }

    @Provides
    @Singleton
    fun providesAppDatabase(): AppDatabase {
        return this.appDatabase
    }

    @Provides
    @Singleton
    fun providesCarMasterDao(): CarMasterDao {
        return this.appDatabase.carMasterDao()
    }

    @Provides
    @Singleton
    fun providesCostsMasterDao(): CostsMasterDao {
        return this.appDatabase.costsMasterDao()
    }

    @Provides
    @Singleton
    fun providesLubMasterDao(): LubMasterDao {
        return this.appDatabase.lubMasterDao()
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
