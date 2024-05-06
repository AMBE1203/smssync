package com.example.dtclnh


import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.viewbinding.ViewBinding
import com.example.dtclnh.databinding.ActivityMainBinding
import com.example.dtclnh.presentation.base.view.BaseActivity
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : BaseActivity() {

    private lateinit var viewBinding: ActivityMainBinding

    override fun initView() {
    }


    @RequiresApi(Build.VERSION_CODES.P)
    override fun initData() {

    }

    override fun setViewBinding(): ViewBinding {
        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        return viewBinding
    }

    companion object {
        private const val TAG = "MainActivity"
    }

}