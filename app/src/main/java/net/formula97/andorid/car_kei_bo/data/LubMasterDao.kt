package net.formula97.andorid.car_kei_bo.data

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Query

@Dao
interface LubMasterDao : BaseDao<LubMaster> {
    @Query("SELECT * FROM LUB_MASTER WHERE RECORD_ID = :id")
    fun findById(id: Long) : LubMaster?

    @Query("SELECT * FROM LUB_MASTER")
    fun findAll() : List<LubMaster>
}