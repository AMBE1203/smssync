package com.example.dtclnh.presentation.base.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.example.dtclnh.util.OnOneOffClickListener

abstract class BaseFragment : Fragment(), IView {
    private var mViewBinding: ViewBinding? = null
    abstract fun setViewBinding(): ViewBinding?
    abstract fun initView()
    abstract fun initData()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mViewBinding = setViewBinding()
        return mViewBinding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mViewBinding = null
    }

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