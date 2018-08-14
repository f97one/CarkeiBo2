package net.formula97.andorid.car_kei_bo.repository

import net.formula97.andorid.car_kei_bo.data.CostsMaster
import javax.inject.Inject

class CostsMasterDataSource @Inject constructor(private val appDatabase: AppDatabase) : DataSource<CostsMaster, Int> {

    override fun addItem(entity: CostsMaster) {
        appDatabase.costsMasterDao().addItem(entity)
    }

    override fun addItems(entities: List<CostsMaster>) {
        for (e in entities) {
            appDatabase.costsMasterDao().addItem(e)
        }
    }

    override fun updateItem(entity: CostsMaster) {
        appDatabase.costsMasterDao().updateItem(entity)
    }

    override fun updateItems(entities: List<CostsMaster>) {
        for (e in entities) {
            appDatabase.costsMasterDao().updateItem(e)
        }
    }

    override fun removeItem(entity: CostsMaster) {
        appDatabase.costsMasterDao().deleteItem(entity)
    }

    override fun findById(id: Int): CostsMaster? {
        return appDatabase.costsMasterDao().findById(id)
    }

}