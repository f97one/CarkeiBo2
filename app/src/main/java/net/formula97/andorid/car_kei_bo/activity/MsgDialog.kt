package net.formula97.andorid.car_kei_bo.activity

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import java.util.*

class MsgDialog : DialogFragment() {

    /**
     * このダイアログのボタンを押したときの処理
     */
    interface OnDialogButtonClickCallback : EventListener {
        /**
         * ポジティブボタンを押したときの処理
         *
         * @param msgResId 表示しているメッセージのResId
         */
        fun onPositiveClick(msgResId: Int)

        /**
         * ネガティブボタンを押したときの処理
         *
         * @param msgResId 表示しているメッセージのResId
         */
        fun onNegativeClick(msgResId: Int)

        /**
         * 中ボタンを押したときの処理
         *
         * @param msgResId 表示しているメッセージのResId
         */
        fun onMiddleClick(msgResId: Int)
    }

    companion object {
        /**
         * このDialogFragmentを探すときのタグ
         */
        const val DIALOG_TAG = "net.formula97.andorid.car_kei_bo.activity.MsgDialog.DIALOG_TAG"

        private val conditionBundleTag = "conditionBundleTag"

        private lateinit var btnCallback: OnDialogButtonClickCallback

        fun getInstance(condition: MsgDialogCondition, callback: OnDialogButtonClickCallback): MsgDialog {
            btnCallback = callback

            val dialog = MsgDialog()

            val bundle: Bundle = Bundle()
            bundle.putSerializable(conditionBundleTag, condition)
            dialog.arguments = bundle

            dialog.isCancelable = condition.cancelable

            return dialog
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder : AlertDialog.Builder = AlertDialog.Builder(activity)

        val args = arguments!!.getSerializable(conditionBundleTag) as MsgDialogCondition

        // アイコン
        if (args.iconResId != Int.MIN_VALUE) {
            builder.setIcon(args.iconResId)
        }
        // タイトル
        //   文字列 -> resId の順で評価
        if (args.titleStr != null) {
            builder.setTitle(args.titleStr)
        } else if (args.titleResId != Int.MIN_VALUE) {
            builder.setTitle(args.titleResId)
        }

        builder.setMessage(args.messageResId)

        // ボタンの生成
        builder.setPositiveButton(args.positiveButtonMsgResId) { dialog, which -> btnCallback.onPositiveClick(args.messageResId) }
        if (args.needNegativeButton) {
            builder.setNegativeButton(args.negativeButtonMsgResId) { dialog, which -> btnCallback.onNegativeClick(args.messageResId) }
        }
        if (args.needMiddleButton) {
            builder.setNeutralButton(args.middleButtonMsgResId) { dialog, which -> btnCallback.onMiddleClick(args.messageResId) }
        }

        return builder.create()
    }
}