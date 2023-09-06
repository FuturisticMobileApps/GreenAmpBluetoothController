package com.example.greenampbluetoothcontroller.test

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class CommonAlertDialog(
    private val title: String,
    private val message: String,
    private val icon: Int? = null,
    private val isSingleButton: Boolean = false,
    private val positiveBtnText: String? = "OK",
    private val negativeBtnText: String? = "Cancel",
    private val onNegativeBtnClicked: (() -> Unit?)? = null,
    private val isCancellable: Boolean = true,
    private val onPositiveBtnClicked: (() -> Any?)? = null,
) : AppCompatDialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        return MaterialAlertDialogBuilder(requireContext()).apply {

            setTitle(title)

            setMessage(message)

            setCancelable(isCancellable)


            if (!isSingleButton)
                setNegativeButton(negativeBtnText) { _, _ ->

                    onNegativeBtnClicked?.invoke()

                    if (isCancellable)
                        dismiss()
                }


            setPositiveButton(positiveBtnText) { _, _ ->
                onPositiveBtnClicked?.invoke()
                if (isCancellable)
                    dismiss()
            }

            if (icon != null)
                setIcon(icon)

        }.create()

    }

}