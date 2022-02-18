package uiutils

import android.app.Dialog
import android.content.Context
import android.widget.Toast
import androidx.appcompat.app.AlertDialog


class MsgUtils {
    companion object {
        // short message
        fun showToast(msg: String?, context: Context) {
            val toast = Toast.makeText(context, msg, Toast.LENGTH_SHORT)
            toast.show()
        }

        // alert message
        fun createDialog(title: String, msg: String, context: Context): Dialog {
            val builder = AlertDialog.Builder(context)
            builder.setTitle(title)
            builder.setMessage(msg)
            builder.setPositiveButton(
                    " OK"
            ) { _, _ ->
                // do nothing, just close the alert
            }
            return builder.create()
        }

        // custom dialog message
        fun createCustomDialog(title: String, msg: String, context: Context): AlertDialog.Builder {
            val builder = AlertDialog.Builder(context)
            builder.setTitle(title)
            builder.setMessage(msg)
            return builder
        }
    }
}
