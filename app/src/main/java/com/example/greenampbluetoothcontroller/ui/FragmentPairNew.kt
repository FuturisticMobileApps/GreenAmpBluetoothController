package com.example.greenampbluetoothcontroller.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.greenampbluetoothcontroller.R
import com.example.greenampbluetoothcontroller.data.deviceList
import com.example.greenampbluetoothcontroller.databinding.FragmentPairNewBinding
import com.example.greenampbluetoothcontroller.util.navToBatteryPackList
import com.example.greenampbluetoothcontroller.util.navToFragmentPinVerification

class FragmentPairNew : Fragment(R.layout.fragment_pair_new) {

    private lateinit var binding : FragmentPairNewBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentPairNewBinding.bind(view)

        initialization()
    }

    private fun initialization() {

        binding.toolBar.imgBack.setOnClickListener {
            navToBatteryPackList()
        }

        with(binding.recView){

            layoutManager = LinearLayoutManager(requireContext())

            adapter = PairNewDeviceListAdapter {
                navToFragmentPinVerification()
            }.apply {
                submitList(deviceList)
            }

            setHasFixedSize(true)
        }
    }
}