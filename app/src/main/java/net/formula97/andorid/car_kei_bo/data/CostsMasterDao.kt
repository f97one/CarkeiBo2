package net.formula97.andorid.car_kei_bo.data

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Query

@Dao
interface CostsMasterDao : BaseDao<CostsMaster> {

    @Query("SELECT * FROM COSTS_MASTER WHERE RECORD_ID = :id")
    fun findById(id: Int) : CostsMaster?

    @Query("SELECT * FROM COSTS_MASTER")
    fun findAll() : List<LubMaster>
}