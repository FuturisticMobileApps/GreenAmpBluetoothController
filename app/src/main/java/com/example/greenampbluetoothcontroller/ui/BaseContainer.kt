package com.example.greenampbluetoothcontroller.ui

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.greenampbluetoothcontroller.databinding.ActivityBaseContainerBinding
import com.example.greenampbluetoothcontroller.util.gone
import com.example.greenampbluetoothcontroller.util.makeToast
import com.example.greenampbluetoothcontroller.util.visible
import dagger.hilt.android.AndroidEntryPoint
import kotlin.system.exitProcess


@AndroidEntryPoint
@SuppressLint("MissingPermission")
class BaseContainer : AppCompatActivity() {

    private lateinit var binding: ActivityBaseContainerBinding

    private var backPressedTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityBaseContainerBinding.inflate(layoutInflater)

        setContentView(binding.root)

//        dismissOnBackPressed()

        setBottomNavigation()

    }

    private fun setBottomNavigation() {

        with(binding) {

            val navHostFragment =
                supportFragmentManager.findFragmentById(com.example.greenampbluetoothcontroller.R.id.navHOstFragment) as NavHostFragment

            bottomNavigationView.setupWithNavController(navHostFragment.navController)

            navHostFragment.navController.addOnDestinationChangedListener { controller, destination, arguments ->
                if (destination.id == com.example.greenampbluetoothcontroller.R.id.batteryDetailsFragment) {
                    bottomNavigationView.gone()
                } else {
                    bottomNavigationView.visible()
                }
            }

        }


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