package com.example.greenampbluetoothcontroller.ui

import android.os.Bundle
import android.view.View
import android.widget.PopupMenu
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.greenampbluetoothcontroller.R
import com.example.greenampbluetoothcontroller.data.core.BatteryPack
import com.example.greenampbluetoothcontroller.data.packList
import com.example.greenampbluetoothcontroller.databinding.FragmentBatteryPackListBinding
import com.example.greenampbluetoothcontroller.util.*

class BatteryPackListFragment : Fragment(R.layout.fragment_battery_pack_list) {

    private lateinit var binding: FragmentBatteryPackListBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)

        binding = FragmentBatteryPackListBinding.bind(view)

        initialization()

        onClickListeners()

    }

    private fun onClickListeners() {

        with(binding) {

            toolbar.imgSearch.setOnClickListeners {
                toolbar.root.gone()
                searchBar.root.visible()
            }

            searchBar.imgClose.setOnClickListeners {
                searchBar.root.gone()
                searchBar.etSearch.setText("")
                toolbar.root.visible()
            }

            toolbar.imgPair.setOnClickListeners {
                navToPairDevice()
            }

            searchBar.imgPair.setOnClickListeners {
                navToPairDevice()
            }

            toolbar.imgMore.setOnClickListeners {
                val popupMenu = PopupMenu(requireContext(),it)
                popupMenu.menuInflater.inflate(R.menu.tools_menu,popupMenu.menu)
                popupMenu.show()
            }

        }
    }

    private fun initialization() {

        with(binding) {

            setContactList()

            searchBar.etSearch.doOnTextChanged { text, start, before, count ->
                updateList(searchBar.etSearch.text.toString().validateString())
            }

        }
    }

    private fun setContactList(batteryPackList: List<BatteryPack> = packList) {

        binding.recView.apply {

            layoutManager = GridLayoutManager(requireContext(), 2)

            adapter = BatteryPackListAdapter {
//                navToBatteryDetails()
            }.apply { submitList(batteryPackList) }

            setHasFixedSize(true)
        }
    }

    private fun updateList(searchQuery: String) {

        val batteryPackList = packList.filter {
            it.packName.validateString().lowercase().contains(
                searchQuery.validateString().lowercase()
            )
        }

        setContactList(batteryPackList)
    }
}