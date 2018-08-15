package net.formula97.andorid.car_kei_bo.repository

import net.formula97.andorid.car_kei_bo.data.LubMaster
import javax.inject.Inject

class LubMasterDataSource @Inject constructor(private val appDatabase: AppDatabase) : DataSource<LubMaster, Int> {
    override fun addItem(entity: LubMaster) {
        appDatabase.lubMasterDao().addItem(entity)
    }

    override fun addItems(entities: List<LubMaster>) {
        for (e in entities) {
            appDatabase.lubMasterDao().addItem(e)
        }
    }

    override fun updateItem(entity: LubMaster) {
        appDatabase.lubMasterDao().updateItem(entity)
    }

    override fun updateItems(entities: List<LubMaster>) {
        for (e in entities) {
            appDatabase.lubMasterDao().updateItem(e)
        }
    }

    override fun removeItem(entity: LubMaster) {
        appDatabase.lubMasterDao().deleteItem(entity)
    }

    override fun findById(id: Int): LubMaster? {
        return appDatabase.lubMasterDao().findById(id)
    }
}