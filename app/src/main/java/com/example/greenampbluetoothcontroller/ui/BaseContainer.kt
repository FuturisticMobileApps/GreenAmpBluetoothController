package com.example.greenampbluetoothcontroller.ui

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.example.greenampbluetoothcontroller.databinding.ActivityBaseContainerBinding
import com.example.greenampbluetoothcontroller.util.makeToast
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlin.system.exitProcess

@AndroidEntryPoint
@SuppressLint("MissingPermission")
class BaseContainer : AppCompatActivity() {

    private lateinit var binding: ActivityBaseContainerBinding

    @Inject
    lateinit var bluetoothAdapter: BluetoothAdapter

    private var backPressedTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityBaseContainerBinding.inflate(layoutInflater)

        setContentView(binding.root)

        dismissOnBackPressed()

    }


    private fun dismissOnBackPressed() {

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {

            override fun handleOnBackPressed() {

                if (backPressedTime + 2000 > System.currentTimeMillis()) {

                    finishAffinity()

                    exitProcess(0)

                } else {

                    makeToast("Press once again to exit!")
                }

                backPressedTime = System.currentTimeMillis()

            }
        })
    }
}