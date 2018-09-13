package net.formula97.andorid.car_kei_bo.logic

import android.arch.persistence.room.Room
import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import net.formula97.andorid.car_kei_bo.data.CarMaster
import net.formula97.andorid.car_kei_bo.data.CostsMaster
import net.formula97.andorid.car_kei_bo.data.LubMaster
import net.formula97.andorid.car_kei_bo.repository.AppDatabase
import org.hamcrest.Matchers.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*
import kotlin.math.log

@RunWith(AndroidJUnit4::class)
class CarListLogicTest {

    private val appDb: AppDatabase by lazy {
        // in-memory dbで初期化
        Room.inMemoryDatabaseBuilder(InstrumentationRegistry.getInstrumentation().targetContext, AppDatabase::class.java).build()
    }

    private val fixture1 = CarMaster(
            carId = 1, carName = "Car1", currentFuelMileage = 0.0, currentRunningCost = 0.0,
            defaultFlag = false, distanceUnit = "km", fuelMileageLabel = "km/l", priceUnit = "円",
            runningCostLabel = "円/km", volumeUnit = "l"
    )
    private val fixture2 = CarMaster(
            carId = 3, carName = "Car2", currentFuelMileage = 35.5, currentRunningCost = 11.3,
            defaultFlag = true, distanceUnit = "ML", fuelMileageLabel = "ML/Gal", priceUnit = "USD.",
            runningCostLabel = "USD./ML", volumeUnit = "Gal."
    )

    @Before
    fun setUp() {
        val carMasterDao = appDb.carMasterDao()

        carMasterDao.addItem(fixture1)
        carMasterDao.addItem(fixture2)
    }

    @After
    fun tearDown() {
        val carMasterDao = appDb.carMasterDao()

        val items: List<CarMaster> = carMasterDao.findAll()
        for (i in items) {
            carMasterDao.deleteItem(i)
        }

        val lubMasterDao = appDb.lubMasterDao()

        val lubMasters = lubMasterDao.findAll()
        for (i in lubMasters) {
            lubMasterDao.deleteItem(i)
        }

        val costsMasterDao = appDb.costsMasterDao()

        val costsMasters = costsMasterDao.findAll()
        for (i in costsMasters) {
            costsMasterDao.deleteItem(i)
        }
    }

    /**
     * データがないとき空のクルマリストを取得できる
     */
    @Test
    fun testN0001() {
        val carMasterDao = appDb.carMasterDao()

        // Fixtureをけす
        val fixtures = carMasterDao.findAll()
        for (f in fixtures) {
            carMasterDao.deleteItem(f)
        }

        val logic = CarListLogic(appDb)

        val items = logic.getCarList()

        assertThat("nullではない", items, `is`(notNullValue()))
        assertThat("サイズは0", items.size, `is`(0))
    }

    /**
     * データがあるときにクルマリストが昇順で取得できる
     */
    @Test
    fun testN0002() {
        val logic = CarListLogic(appDb)

        val items = logic.getCarList()

        assertThat("nullではない", items, `is`(notNullValue()))
        assertThat("サイズは2", items.size, `is`(2))

        val i1 = items[0]
        assertTrue("一つ目のFixtureとひとしい", i1 == fixture1)
        val i2 = items[1]
        assertTrue("二つ目のFixtureにひとしい", i2 == fixture2)
    }

    /**
     * データがあるときにクルマリストが降順で取得できる
     */
    @Test
    fun testN0003() {
        val logic = CarListLogic(appDb)

        val items = logic.getCarList(true)

        assertThat("nullではない", items, `is`(notNullValue()))
        assertThat("サイズは2", items.size, `is`(2))

        val i1 = items[0]
        assertTrue("二つ目のFixtureとひとしい", i1 == fixture2)
        val i2 = items[1]
        assertTrue("一つ目のFixtureにひとしい", i2 == fixture1)
    }

    /**
     * デフォルトフラグありのクルマが取得できる（正常ケース）
     */
    @Test
    fun testN0004() {
        val logic = CarListLogic(appDb)

        val item = logic.findForDefault()

        assertThat("nullではない", item, `is`(notNullValue()))
        assertTrue("二つ目のFixtureにひとしい", item == fixture2)
    }

    /**
     * デフォルトフラグありのレコードがないときにnullが返る
     */
    @Test
    fun testN0005() {
        val carMasterDao = appDb.carMasterDao()
        // デフォルトフラグを下げる
        val fixture2dash = fixture2.clone() as CarMaster
        fixture2dash.defaultFlag = false
        carMasterDao.updateItem(fixture2dash)

        val logic = CarListLogic(appDb)

        val item = logic.findForDefault()

        assertThat("nullである", item, `is`(nullValue()))

    }

    /**
     * デフォルトフラグありのレコードが2以上ある場合若いIDのレコードが返る
     */
    @Test
    fun testN0006() {
        val carMasterDao = appDb.carMasterDao()

        // デフォルトフラグありのレコードをfixtureより前のIDで追加
        val f3 = CarMaster(
                carId = 2, carName = "クルマ3", currentFuelMileage = 144.0, currentRunningCost = 12.8,
                defaultFlag = true, distanceUnit = "km", fuelMileageLabel = "km/l", priceUnit = "円",
                runningCostLabel = "円/km", volumeUnit = "l"
        )
        carMasterDao.addItem(f3)

        val logic = CarListLogic(appDb)

        val item = logic.findForDefault()

        assertThat("nullではない", item, `is`(notNullValue()))
        assertTrue("このメソッドで入れたFixtureにひとしい", item == f3)

    }

    /**
     * デフォルトフラグを指定IDに切り替えるテスト
     */
    @Test
    fun testN0007() {
        val logic = CarListLogic(appDb)
        logic.changeDefault(1)

        val resultItem = appDb.carMasterDao().findById(1)
        assertTrue("デフォルトフラグがID = 1に立っている", resultItem!!.defaultFlag)
    }

    /**
     * ないレコードのデフォルトフラグを操作しようとした場合IllegalArgumentExceptionが投げられる
     */
    @Test
    fun testN0008() {
        try {
            val logic = CarListLogic(appDb)
            logic.changeDefault(5)

            fail("例外は投げられなかった")
        } catch (e: Exception) {
            assertThat("IllegalArgumentExceptionが投げられる", e, `is`(instanceOf(IllegalArgumentException::class.java)))
            assertThat(e.message, `is`("Can't find record by specified CAR_ID = 5"))
        }
    }

    /**
     * クルマの燃費記録を消去する
     */
    @Test
    fun testN0009() {
        val lm11 = LubMaster(
                recordId = 1, carId = 1, refuelDate = Date(), unitPrice = 125.0, lubAmount = 35.0,
                tripMeter = 350.0, comments = ""
        )
        val lm12 = LubMaster(
                recordId = 2, carId = 1, refuelDate = Date(), unitPrice = 126.0, lubAmount = 36.0,
                tripMeter = 360.0, comments = ""
        )
        val lm31 = LubMaster(
                recordId = 3, carId = 3, refuelDate = Date(), unitPrice = 127.0, lubAmount = 37.0,
                tripMeter = 370.0, comments = ""
        )
        val lubMasterDao = appDb.lubMasterDao()
        lubMasterDao.addItem(lm11)
        lubMasterDao.addItem(lm12)
        lubMasterDao.addItem(lm31)

        val cost11 = CostsMaster(
                recordId = 1, carId = 1, refuelDate = Date(), runningCost = 11.5
        )
        val cost12 = CostsMaster(
                recordId = 2, carId = 1, refuelDate = Date(), runningCost = 11.6
        )
        val cost31 = CostsMaster(
                recordId = 3, carId = 3, refuelDate = Date(), runningCost = 11.7
        )
        val costsMasterDao = appDb.costsMasterDao()
        costsMasterDao.addItem(cost11)
        costsMasterDao.addItem(cost12)
        costsMasterDao.addItem(cost31)

        val logic = CarListLogic(appDb)
        logic.deleteCarMileage(1)

        // レコード数の減少で検査
        val carMasters = appDb.carMasterDao().findAll()
        assertThat(carMasters.size, `is`(1))

        val lubMasters = lubMasterDao.findAll()
        assertThat(lubMasters.size, `is`(1))

        val costsMasters = costsMasterDao.findAll()
        assertThat(costsMasters.size, `is`(1))
    }

    /**
     * デフォルトフラグ付きのクルマを追加できる
     */
    @Test
    fun textN0010() {
        val f3 = CarMaster(
                carId = 2, carName = "クルマ3", currentFuelMileage = 144.0, currentRunningCost = 12.8,
                defaultFlag = true, distanceUnit = "km", fuelMileageLabel = "km/l", priceUnit = "円",
                runningCostLabel = "円/km", volumeUnit = "l"
        )

        val logic = CarListLogic(appDb)
        logic.addNewCar(f3)

        val carMasterDao = appDb.carMasterDao()

        val carMasters = carMasterDao.findAll()
        assertThat(carMasters.size, `is`(3))
        val defaultCar = carMasters.find { r -> r.defaultFlag }
        assertThat(defaultCar!!.carId, `is`(2))

    }

    /**
     * デフォルトフラグなしのクルマを追加できる
     */
    @Test
    fun testN0011() {
        val f3 = CarMaster(
                carId = 2, carName = "クルマ3", currentFuelMileage = 144.0, currentRunningCost = 12.8,
                defaultFlag = false, distanceUnit = "km", fuelMileageLabel = "km/l", priceUnit = "円",
                runningCostLabel = "円/km", volumeUnit = "l"
        )

        val logic = CarListLogic(appDb)
        logic.addNewCar(f3)

        val carMasterDao = appDb.carMasterDao()

        val carMasters = carMasterDao.findAll()
        assertThat(carMasters.size, `is`(3))
        val defaultCar = carMasters.find { r -> r.defaultFlag }
        assertThat(defaultCar!!.carId, `is`(3))
    }
}