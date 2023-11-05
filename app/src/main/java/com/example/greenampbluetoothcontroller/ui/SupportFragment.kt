package com.example.greenampbluetoothcontroller.ui

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.greenampbluetoothcontroller.R
import com.example.greenampbluetoothcontroller.databinding.FragmentSupportBinding
import com.example.greenampbluetoothcontroller.util.setOnClickListeners
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SupportFragment : Fragment(R.layout.fragment_support) {

    private lateinit var binding: FragmentSupportBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSupportBinding.bind(view)
        setLinks()
    }

    private fun setLinks() {

        with(binding) {

            imgInstagram.setOnClickListeners { openInstagramProfile("greenamptechnos") }

            imgLinkedIn.setOnClickListeners { openLinkedInCompanyPage("https://www.linkedin.com/company/greenamp-technosprivate-limited/") }

            imgWhatsApp.setOnClickListeners { openWhatsAppOrBrowser("+91 9629359108") }


        }
    }


    private fun openInstagramProfile(username: String) {
        // Construct the Instagram profile URL
        val instagramProfileUrl = "https://www.instagram.com/$username/"

        try {
            // Check if the Instagram app is installed
            val packageManager = requireActivity().packageManager
            val isInstagramInstalled = try {
                packageManager.getPackageInfo("com.instagram.android", 0)
                true
            } catch (e: PackageManager.NameNotFoundException) {
                false
            }

            // Create an Intent to open the Instagram app or a web browser
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(instagramProfileUrl))
            if (isInstagramInstalled) {
                // If the Instagram app is installed, open the profile in the app
                intent.setPackage("com.instagram.android")
            } else {
                // If the Instagram app is not installed, open the profile in a web browser
                intent.setPackage(null)
            }

            // Start the activity
            startActivity(intent)
        } catch (e: Exception) {
            // Handle any exceptions, such as a malformed URL or other errors
            e.printStackTrace()
        }
    }


    private fun openLinkedInCompanyPage(companyUrl: String) {
        // Check if the LinkedIn app is installed on the device
        val packageManager: PackageManager = requireActivity().packageManager
        val linkedInAppPackageName = "com.linkedin.android"
        val isLinkedInInstalled: Boolean = try {
            packageManager.getPackageInfo(linkedInAppPackageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }

        // Create an Intent to open the LinkedIn app or a web browser
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(companyUrl))
        if (isLinkedInInstalled) {
            // If the LinkedIn app is installed, open the company page in the app
            intent.setPackage(linkedInAppPackageName)
        } else {
            // If the LinkedIn app is not installed, open the company page in a web browser
            intent.setPackage(null)
        }

        // Start the activity
        try {
            startActivity(intent)
        } catch (e: Exception) {
            // Handle any exceptions, such as a malformed URL or other errors
            e.printStackTrace()
        }
    }



    private fun openWhatsAppOrBrowser(phoneNumber: String) {
        val whatsappPackage = "com.whatsapp"

        val packageManager: PackageManager = requireActivity().packageManager

        // Check if WhatsApp is installed
        val isWhatsAppInstalled: Boolean = try {
            packageManager.getPackageInfo(whatsappPackage, PackageManager.GET_ACTIVITIES)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }

        val whatsappUrl = "https://api.whatsapp.com/send?phone=$phoneNumber"

        val intent = Intent(Intent.ACTION_VIEW)
        if (isWhatsAppInstalled) {
            // If WhatsApp is installed, open it with the specified phone number
            intent.data = Uri.parse("whatsapp://send?phone=$phoneNumber")
            intent.`package` = whatsappPackage
        } else {
            // If WhatsApp is not installed, open a web browser with the WhatsApp web link
            intent.data = Uri.parse(whatsappUrl)
        }

        // Start the activity
        try {
            startActivity(intent)
        } catch (e: Exception) {
            // Handle any exceptions, such as a malformed URL or other errors
            e.printStackTrace()
        }
    }


}