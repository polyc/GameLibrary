package com.example.gamelibrary.settings

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.example.gamelibrary.R

class DeleteAccountDialogFragment : DialogFragment() {

    internal lateinit var listener: NoticeDialogListener

    interface NoticeDialogListener{
        fun onDialogPositiveClick(dialog: DialogFragment)
        fun onDialogNegativeClick(dialog: DialogFragment)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            listener = context as NoticeDialogListener
        } catch (e: ClassCastException) {
            // The activity doesn't implement the interface, throw exception
            throw ClassCastException((context.toString() +
                    " must implement NoticeDialogListener"))
        }
    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            // Use the Builder class for convenient dialog construction
            val builder = AlertDialog.Builder(it)
            builder.setMessage(R.string.dialog_delete_account)
                .setPositiveButton(R.string.delete,
                    DialogInterface.OnClickListener { dialog, id ->
                        listener.onDialogPositiveClick(this)
                    })
                .setNegativeButton(R.string.cancel,
                    DialogInterface.OnClickListener { dialog, id ->
                        listener.onDialogNegativeClick(this)
                    })
            // Create the AlertDialog object and return it
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}
