package com.example.dtclnh.presentation.base.view

import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding
import com.example.dtclnh.util.OnOneOffClickListener

abstract class BaseActivity : AppCompatActivity(), IView {
    abstract fun initView()
    abstract fun initData()
    abstract fun setViewBinding(): ViewBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(setViewBinding().root)
        initView()
        initData()
    }

    /**
     * hide keyboard
     */
    fun hideKeyboard() {
        val view = this.currentFocus
        view.let {
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager?
            imm?.hideSoftInputFromWindow(it?.windowToken, 0)
        }
    }

    /**
     * prevent double click on view
     **/
    fun View.setSafeOnClickListener(onSafeClick: (View) -> Unit) {
        try {
            val safeClickListener = OnOneOffClickListener {
                onSafeClick(it)
            }
            setOnClickListener(safeClickListener)
        } catch (E: Exception) {
        }
    }
}