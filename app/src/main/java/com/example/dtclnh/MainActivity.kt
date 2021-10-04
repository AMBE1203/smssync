package com.example.dtclnh

import androidx.viewbinding.ViewBinding
import com.example.dtclnh.databinding.ActivityMainBinding
import com.example.dtclnh.presentation.base.view.BaseActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : BaseActivity() {

    private lateinit var viewBinding: ActivityMainBinding

    override fun initView() {


    }

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