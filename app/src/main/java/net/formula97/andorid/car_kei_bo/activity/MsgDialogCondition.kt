package net.formula97.andorid.car_kei_bo.activity

import java.io.Serializable

/**
 * メッセージダイアログ表示コンディションのDTO。
 */
data class MsgDialogCondition(var messageResId: Int) : Serializable {
    /**
     * ダイアログタイトルのResId
     */
    var titleResId: Int = Int.MIN_VALUE
    /**
     * ダイアログタイトル文字列
     */
    var titleStr: String? = null
    /**
     * アイコンのResId
     */
    var iconResId: Int = Int.MIN_VALUE
    /**
     * ネガティブボタン作成要否
     */
    var needNegativeButton: Boolean = false
    /**
     * 中ボタン作成要否
     */
    var needMiddleButton: Boolean = false
    /**
     * ポジティブボタン表示名
     */
    var positiveButtonMsgResId: Int = android.R.string.ok
    /**
     * ネガティブボタン表示名
     */
    var negativeButtonMsgResId: Int = android.R.string.no
    /**
     * 中ボタン表示名
     */
    var middleButtonMsgResId: Int = android.R.string.cancel
    /**
     * ダイアログを Back でキャンセル可能か
     */
    var cancelable: Boolean = true
}