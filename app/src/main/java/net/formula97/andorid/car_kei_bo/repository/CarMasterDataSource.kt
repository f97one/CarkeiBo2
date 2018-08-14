package net.formula97.andorid.car_kei_bo.repository

import net.formula97.andorid.car_kei_bo.data.CarMaster
import javax.inject.Inject

class CarMasterDataSource @Inject
constructor(private val appDatabase: AppDatabase) : DataSource<CarMaster, Int> {

    override fun addItem(entity: CarMaster) {
        appDatabase.carMasterDao().addItem(entity)
    }

    override fun addItems(entities: List<CarMaster>) {
        for (e in entities) {
            appDatabase.carMasterDao().addItem(e)
        }
    }

    override fun updateItem(entity: CarMaster) {
        appDatabase.carMasterDao().updateItem(entity)
    }

    override fun updateItems(entities: List<CarMaster>) {
        for (e in entities) {
            appDatabase.carMasterDao().updateItem(e)
        }
    }

    override fun removeItem(entity: CarMaster) {
        appDatabase.carMasterDao().deleteItem(entity)
    }

    override fun findById(id: Int) : CarMaster? {
        return appDatabase.carMasterDao().findById(id)
    }
}
