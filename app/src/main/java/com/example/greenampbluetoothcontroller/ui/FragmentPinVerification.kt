package com.example.greenampbluetoothcontroller.ui

import android.os.Bundle
import android.view.View
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import com.example.greenampbluetoothcontroller.R
import com.example.greenampbluetoothcontroller.databinding.FragmentPinVerificationBinding
import com.example.greenampbluetoothcontroller.util.navToPairDevice
import com.example.greenampbluetoothcontroller.util.setOnClickListeners

class FragmentPinVerification : Fragment(R.layout.fragment_pin_verification) {

    private lateinit var binding: FragmentPinVerificationBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)

        binding = FragmentPinVerificationBinding.bind(view)

        initialization()
    }

    private fun initialization() {

        with(binding) {

            toolBar.imgBack.setOnClickListeners {
                navToPairDevice()
            }

            etOPt1.doOnTextChanged { text, start, before, count ->

                if (count == 0)
                    etOPt1.requestFocus();
                else if (count == 1)
                    etOPt2.requestFocus();
            }

            etOPt2.doOnTextChanged { text, start, before, count ->

                if (count == 0)
                    etOPt1.requestFocus();
                else if (count == 1)
                    etOPt3.requestFocus();
            }

            etOPt3.doOnTextChanged { text, start, before, count ->

                if (count == 0)
                    etOPt2.requestFocus();
                else if (count == 1)
                    etOPt4.requestFocus();
            }

            etOPt4.doOnTextChanged { text, start, before, count ->

                if (count == 0)
                    etOPt3.requestFocus();
                else if (count == 1)
                    etOPt5.requestFocus();
            }

            etOPt5.doOnTextChanged { text, start, before, count ->

                if (count == 0)
                    etOPt4.requestFocus();
                else if (count == 1)
                    etOPt6.requestFocus();
            }

            etOPt6.doOnTextChanged { text, start, before, count ->

                if (etOPt6.length() == 0)
                    etOPt5.requestFocus();
            }

        }
    }


}

data class Category(
    val name : String = "dnpd",
    val age : String = "sd"
)