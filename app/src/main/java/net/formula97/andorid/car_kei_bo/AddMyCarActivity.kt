package net.formula97.andorid.car_kei_bo

import android.os.Bundle
import android.view.View
import android.widget.*
import net.formula97.andorid.car_kei_bo.activity.AbstractAppActivity
import net.formula97.andorid.car_kei_bo.data.CarMaster
import net.formula97.andorid.car_kei_bo.logic.CarListLogic

/**
 * 「クルマを追加」画面を扱うクラス。
 * @author kazutoshi
 */
class AddMyCarActivity : AbstractAppActivity() {

    // ウィジェットを扱うための定義
    internal lateinit var textview_addCarName: TextView
    internal lateinit var checkbox_setDefault: CheckBox
    internal lateinit var button_addCar: Button
    internal lateinit var button_cancel_addCar: Button
    internal lateinit var spinner_price_Unit: Spinner
    internal lateinit var spinner_distanceUnit: Spinner
    internal lateinit var spinner_volumeUnit: Spinner

    /** Called when the activity is first created.  */
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.addcar)

        // ウィジェットを扱うための定義
        //   プログラムから扱うための定数を検索してセット
        textview_addCarName = findViewById<View>(R.id.textview_addCarName) as TextView
        checkbox_setDefault = findViewById<View>(R.id.checkBox_SetDefault) as CheckBox
        button_addCar = findViewById<View>(R.id.button_addCar) as Button
        button_cancel_addCar = findViewById<View>(R.id.button_cancel_addCar) as Button
        spinner_price_Unit = findViewById<View>(R.id.spinner_priceUnit) as Spinner
        spinner_distanceUnit = findViewById<View>(R.id.spinner_distanceUnit) as Spinner
        spinner_volumeUnit = findViewById<View>(R.id.spinner_volumeUnit) as Spinner

        button_addCar.setOnClickListener {
            onClickAddCar()
        }
        button_cancel_addCar.setOnClickListener {
            onClickCancel()
        }

    }

    /**
     * ほかのActivityへ遷移するなどで一時的に処理を停止するときに、システムからコールされる。
     * DBの閉じ忘れを防止するため、DBが開いていたらここでクローズする。
     * @see android.app.Activity.onPause
     */
    override fun onPause() {
        super.onPause()
    }

    /**
     * 画面描画を行うときに必ずシステムからコールされる。
     * 上記特徴を利用し、画面表示されるコントロール類の挙動を設定している。
     * @see android.app.Activity.onResume
     */
    override fun onResume() {
        super.onResume()

        /*
		 * 「デフォルトカー」チェックの挙動決定
		 *   CAR_MASTERにまったくレコードがない場合、デフォルトカーチェックを入れていないと、
		 * クルマリストに戻ったときにデフォルトカーが特定できず異常終了する。
		 *   CAR_MASTERにレコードがまったくない場合、最初に追加されるクルマがデフォルトになるのは
		 * 暗黙の了解ともいえるため、
		 *   １　CAR_MASTERにレコードがない場合は、デフォルトカーチェックをオン
		 *   ２　CAR_MASTERにレコードがある場合は、デフォルトカーチェックをオフのままにする
		 * という処理を行う。
		 */
        val carListLogic = CarListLogic(getAppDb())
        checkbox_setDefault.isChecked = !carListLogic.getCarList().isEmpty()
    }

    /**
     * 「クルマを追加」ボタンを押したときの処理。
     */
    private fun onClickAddCar() {
        // 追加用データを組み立てる
        val carName = textview_addCarName.text.toString()
        val price = spinner_price_Unit.selectedItem as String
        val distance = spinner_distanceUnit.selectedItem as String
        val volume = spinner_volumeUnit.selectedItem as String
        val fuelMileageLabel = "$distance/$volume"
        val runningCostLabel = "$price/$distance"

        val newCar = CarMaster(carName = carName, defaultFlag = checkbox_setDefault.isChecked,
                priceUnit = price, distanceUnit = distance, volumeUnit = volume, carId = null,
                fuelMileageLabel = fuelMileageLabel, runningCostLabel = runningCostLabel,
                currentRunningCost = 0.0, currentFuelMileage = 0.0
        )

        CarListLogic(getAppDb()).addNewCar(newCar)

        // テキストボックスを空にし、デフォルトカーチェックをはずす
        onClickCancel()

        // トーストを表示する
        showToastmsg(carName)
    }

    /**
     * 「キャンセル」を押したときの処理。
     */
    private fun onClickCancel() {
        // 入力されている値を消す
        //   「消す」=「空の値をセット」ということらしい
        textview_addCarName.text = ""
        // チェックされていない状態にする
        checkbox_setDefault.isChecked = false
    }

    /**
     * クルマの追加をトースト表示で通知する。
     * @param carName String型、トースト中に表示するクルマの名前
     */
    private fun showToastmsg(carName: String) {
        // トースト表示の組み立てに使うString変数の宣言
        val line1: String = carName + " " + getString(R.string.toastmsg_addcar1)
        val line2: String = getString(R.string.toastmsg_addcar2)
        val line3: String = getString(R.string.toastmsg_addcar3)

        // トーストを作成する
        Toast.makeText(this, line1 + "\n" + line2 + "\n" + line3, Toast.LENGTH_LONG).show()
    }

}