package net.formula97.andorid.car_kei_bo.activity

import android.support.v7.app.AppCompatActivity
import net.formula97.andorid.car_kei_bo.TheApp
import net.formula97.andorid.car_kei_bo.repository.AppDatabase

abstract class AbstractAppActivity : AppCompatActivity() {

    fun getAppDb(): AppDatabase {
        return (application as TheApp).getLocalDatasource()
    }
}