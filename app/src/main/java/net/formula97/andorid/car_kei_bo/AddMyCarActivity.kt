package net.formula97.andorid.car_kei_bo

import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.SpannableStringBuilder
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.Button
import android.widget.CheckBox
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast

/**
 * 「クルマを追加」画面を扱うクラス。
 * @author kazutoshi
 */
class AddMyCarActivity : AppCompatActivity(), OnItemSelectedListener {

    private val dbman = DbManager(this)

    // ウィジェットを扱うための定義
    internal var textview_addCarName: TextView
    internal var checkbox_setDefault: CheckBox
    internal var button_addCar: Button
    internal var button_cancel_addCar: Button
    internal var spinner_price_Unit: Spinner
    internal var spinner_distanceUnit: Spinner
    internal var spinner_volumeUnit: Spinner

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

        // 各スピナーへonClickListenerを定義
        spinner_price_Unit.onItemSelectedListener = this
        spinner_distanceUnit.onItemSelectedListener = this
        spinner_volumeUnit.onItemSelectedListener = this
    }

    /**
     * ほかのActivityへ遷移するなどで一時的に処理を停止するときに、システムからコールされる。
     * DBの閉じ忘れを防止するため、DBが開いていたらここでクローズする。
     * @see android.app.Activity.onPause
     */
    override fun onPause() {
        // TODO 自動生成されたメソッド・スタブ
        super.onPause()

        if (db.isOpen) {
            dbman.close()
        }
    }

    /**
     * 画面描画を行うときに必ずシステムからコールされる。
     * 上記特徴を利用し、画面表示されるコントロール類の挙動を設定している。
     * @see android.app.Activity.onResume
     */
    override fun onResume() {
        // TODO 自動生成されたメソッド・スタブ
        super.onResume()

        /*
		 * ボタンの配置を画面幅の1/2にする処理
		 *
		 * onCreate()ではなくこちらに書くのは、最終的な画面設定が行われるのがこちらという
		 * Androidのくせによるものである。
		 */
        // 画面幅を取得
        val displayWidth = windowManager.defaultDisplay.width

        // ボタンの幅を、取得した画面幅の1/2にセット
        button_addCar.width = displayWidth / 2
        button_cancel_addCar.width = displayWidth / 2

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
        db = dbman.readableDatabase

        if (dbman.hasCarRecords(db)) {
            checkbox_setDefault.isChecked = FLAG_DEFAULT_OFF
        } else {
            checkbox_setDefault.isChecked = FLAG_DEFAULT_ON
        }

        dbman.close()
    }

    /**
     * 「クルマを追加」ボタンを押したときの処理。
     * OnClickListenerをインターフェース実装していない関係で、GUIに紐付けしてボタン処理を書いている。
     * ただ、このやり方だとpublicメソッドにしなきゃいけないようだ。
     * @param v View型、ボタンを押されたときのId？
     */
    fun onClickAddCar(v: View) {
        val carName: String
        val defaultFlags: Boolean
        var volume = ""
        var distance = ""
        var price = ""

        db = dbman.writableDatabase

        // TextViewに入力された値を取得
        //   getText()はCaheSequence型になるので、Stringにキャストする
        val sp = textview_addCarName.text as SpannableStringBuilder
        carName = sp.toString()
        Log.w("CarListActivity", "New Car name = $carName")

        // チェックボックスの状態を取得
        if (checkbox_setDefault.isChecked) {
            /*
			 * チェックボックスにチェックがあれば、
			 *   1.デフォルトフラグがすでにセットされているかを調べ、
			 *   2.セットされていればいったんすべてのデフォルトフラグを下げる
			 */
            if (dbman.isExistDefaultCarFlag(db)) {
                val iRet = dbman.clearAllDefaultFlags(db)
                // デフォルトフラグを下げたことをログに出力する
                Log.w("CAR_MASTER", "Default Car flags cleared, " + iRet.toString() + "rows updated.")
            }
            defaultFlags = FLAG_DEFAULT_ON
        } else {
            defaultFlags = FLAG_DEFAULT_OFF
        }

        // 各スピナーから値を取得する。
        price = spinner_price_Unit.selectedItem as String
        distance = spinner_distanceUnit.selectedItem as String
        volume = spinner_volumeUnit.selectedItem as String

        // クルマデータをCAR_MASTERに追加
        val lRet = dbman.addNewCar(db, carName, defaultFlags, price, distance, volume)
        Log.i("CAR_MASTER", "Car record inserted, New Car Name = " + carName + " , New row ID = " + lRet.toString())

        dbman.close()

        // テキストボックスを空にし、デフォルトカーチェックをはずす
        textview_addCarName.text = ""
        checkbox_setDefault.isChecked = FLAG_DEFAULT_OFF

        // トーストを表示する
        showToastmsg(carName)
    }

    /**
     * 「キャンセル」を押したときの処理。
     * onClickAddCar()同様、OnClickListenerをインターフェース実装していない関係で、
     * GUIに紐付けしてボタン処理を書いている。
     * ただ、このやり方だとpublicメソッドにしなきゃいけないようだ。
     * @param v View型、ボタンを押されたときのId？
     */
    fun onClickCancel(v: View) {
        // 入力されている値を消す
        //   「消す」=「空の値をセット」ということらしい
        textview_addCarName.text = ""
        // チェックされていない状態にする
        checkbox_setDefault.isChecked = FLAG_DEFAULT_OFF
    }

    /**
     * スピナーのアイテムを選択したときに発生するイベント
     * @param parent
     * @param view
     * @param position
     * @param id
     */
    //@Override
    override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
        // TODO 自動生成されたメソッド・スタブ

    }

    /**
     * スピナーのアイテムを何も選択しなかったときに発生するイベント
     * @param arg0
     */
    //@Override
    override fun onNothingSelected(arg0: AdapterView<*>) {
        // TODO 自動生成されたメソッド・スタブ

    }

    /**
     * クルマの追加をトースト表示で通知する。
     * @param carName String型、トースト中に表示するクルマの名前
     */
    protected fun showToastmsg(carName: String) {
        // トースト表示の組み立てに使うString変数の宣言
        val line1: String
        val line2: String
        val line3: String

        line1 = carName + " " + getString(R.string.toastmsg_addcar1)
        line2 = getString(R.string.toastmsg_addcar2)
        line3 = getString(R.string.toastmsg_addcar3)

        // トーストを作成する
        Toast.makeText(this, line1 + "\n" + line2 + "\n" + line3, Toast.LENGTH_LONG).show()
    }

    companion object {
        var db: SQLiteDatabase

        private val FLAG_DEFAULT_ON = true
        private val FLAG_DEFAULT_OFF = false
    }

}