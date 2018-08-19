package net.formula97.andorid.car_kei_bo.logic

import android.arch.persistence.room.Room
import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import net.formula97.andorid.car_kei_bo.data.CarMaster
import net.formula97.andorid.car_kei_bo.repository.AppDatabase
import org.hamcrest.Matchers.*
import org.junit.After
import org.junit.Assert.assertThat
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CarListLogicTest {

    private var appDb: AppDatabase? = null

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
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        appDb = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()

        if (appDb != null) {
            // Fixtureを投入
            val carMasterDao = appDb!!.carMasterDao()

            carMasterDao.addItem(fixture1)
            carMasterDao.addItem(fixture2)
        }
    }

    @After
    fun tearDown() {
        val carMasterDao = appDb?.carMasterDao()

        if (carMasterDao != null) {
            val items: List<CarMaster> = carMasterDao.findAll() ?: ArrayList()

            for (i in items) {
                carMasterDao.deleteItem(i)
            }
        }
    }

    /**
     * データがないとき空のクルマリストを取得できる
     */
    @Test
    fun testN0001() {
        val carMasterDao = appDb?.carMasterDao()

        if (carMasterDao != null) {
            // Fixtureをけす
            val fixtures = carMasterDao.findAll()
            for (f in fixtures) {
                carMasterDao.deleteItem(f)
            }

            val logic = CarListLogic(appDb!!)

            val items = logic.getCarList()

            assertThat("nullではない", items, `is`(notNullValue()))
            assertThat("サイズは0", items.size, `is`(0))
        }
    }

    /**
     * データがあるときにクルマリストが昇順で取得できる
     */
    @Test
    fun testN0002() {
        val logic = CarListLogic(appDb!!)

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
        val logic = CarListLogic(appDb!!)

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
        val logic = CarListLogic(appDb!!)

        val item = logic.findForDefault()

        assertThat("nullではない", item, `is`(notNullValue()))
        assertTrue("二つ目のFixtureにひとしい", item == fixture2)
    }

    /**
     * デフォルトフラグありのレコードがないときにnullが返る
     */
    @Test
    fun testN0005() {
        val carMasterDao = appDb!!.carMasterDao()
        // デフォルトフラグを下げる
        val fixture2dash = fixture2.clone() as CarMaster
        fixture2dash.defaultFlag = false
        carMasterDao.updateItem(fixture2dash)

        val logic = CarListLogic(appDb!!)

        val item = logic.findForDefault()

        assertThat("nullである", item, `is`(nullValue()))

    }

    /**
     * デフォルトフラグありのレコードが2以上ある場合若いIDのレコードが返る
     */
    @Test
    fun testN0006() {
        val carMasterDao = appDb!!.carMasterDao()

        // デフォルトフラグありのレコードをfixtureより前のIDで追加
        val f3 = CarMaster(
                carId = 2, carName = "クルマ3", currentFuelMileage = 144.0, currentRunningCost = 12.8,
                defaultFlag = true, distanceUnit = "km", fuelMileageLabel = "km/l", priceUnit = "円",
                runningCostLabel = "円/km", volumeUnit = "l"
        )
        carMasterDao.addItem(f3)

        val logic = CarListLogic(appDb!!)

        val item = logic.findForDefault()

        assertThat("nullではない", item, `is`(notNullValue()))
        assertTrue("このメソッドで入れたFixtureにひとしい", item == f3)

    }
}