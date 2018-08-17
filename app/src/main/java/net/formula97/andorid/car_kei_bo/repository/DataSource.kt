package net.formula97.andorid.car_kei_bo.repository

/**
 * 基礎的なデータアクセス処理を規定するinterface。
 *
 * T : Entityの型
 *
 * P : プライマリキーの型
 */
interface DataSource<T, P> {
    /**
     * Entityをinsertする。
     */
    fun addItem(entity : T)

    /**
     * EntityのListをまとめてinsertする。
     */
    fun addItems(entities : List<T>)

    /**
     * Entityをupdateする。
     */
    fun updateItem(entity: T)

    /**
     * Entityのリストをまとめてupdateする。
     */
    fun updateItems(entities: List<T>)

    /**
     * Entityをdeleteする。
     */
    fun removeItem(entity: T)

    /**
     * プライマリキーでEntityを探す。
     */
    fun findById(id: P) : T?

    /**
     * レコードがあるかどうかを返す。
     */
    fun hasRecord() : Boolean
}