package com.example.greenampbluetoothcontroller.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.greenampbluetoothcontroller.R
import com.example.greenampbluetoothcontroller.databinding.FragmentBatteryDetailsBinding
import com.example.greenampbluetoothcontroller.databinding.FragmentBatteryPackListBinding
import com.example.greenampbluetoothcontroller.util.navToBatteryPackList
import com.example.greenampbluetoothcontroller.util.setOnClickListeners

class BatteryDetailsFragment : Fragment(R.layout.fragment_battery_details) {

    private lateinit var binding : FragmentBatteryDetailsBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentBatteryDetailsBinding.bind(view)

        initialization()
    }

    private fun initialization() {

        with(binding){

            toolBar.imgBack.setOnClickListeners {
                navToBatteryPackList()
            }
        }
    }
}