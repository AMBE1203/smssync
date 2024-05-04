package com.example.dtclnh


import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import androidx.viewbinding.ViewBinding
import com.example.dtclnh.databinding.ActivityMainBinding
import com.example.dtclnh.presentation.base.view.BaseActivity
import com.example.dtclnh.presentation.broadcast.SyncService
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : BaseActivity() {

    private lateinit var viewBinding: ActivityMainBinding

    override fun initView() {


    }

    private val requestForegroundPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                // Khởi động SyncService
                val serviceIntent = Intent(this, SyncService::class.java)
                startService(serviceIntent)
            } else {
                // todo navigate to setting
            }
        }

    override fun initData() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestForegroundPermissionLauncher.launch(Manifest.permission.FOREGROUND_SERVICE)
        } else {

            // Khởi động SyncService
            val serviceIntent = Intent(this, SyncService::class.java)
            startService(serviceIntent)

        }
    }

    override fun setViewBinding(): ViewBinding {
        viewBinding = ActivityMainBinding.inflate(layoutInflater)

        return viewBinding
    }

    companion object {
        private const val TAG = "MainActivity"
    }

}