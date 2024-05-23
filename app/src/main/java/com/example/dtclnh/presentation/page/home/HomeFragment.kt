package com.example.dtclnh.presentation.page.home

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewbinding.ViewBinding
import com.example.dtclnh.databinding.FragmentHomeBinding
import com.example.dtclnh.databinding.FragmentLoginBinding
import com.example.dtclnh.domain.model.SmsModel
import com.example.dtclnh.presentation.base.ext.observe
import com.example.dtclnh.presentation.base.view.BaseFragment
import com.example.dtclnh.presentation.page.login.LoginViewModel
import com.example.dtclnh.presentation.page.login.LoginViewState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class HomeFragment : BaseFragment() {
    private lateinit var viewBinding: FragmentHomeBinding
    private val homeViewModel: HomeViewModel by viewModels()

    private lateinit var smsAdapter: SmsAdapter

    override fun setViewBinding(): ViewBinding {
        viewBinding = FragmentHomeBinding.inflate(layoutInflater)
        return viewBinding
    }

    override fun initView() {
        (activity as? AppCompatActivity)?.setSupportActionBar(viewBinding.topAppBar)
        (activity as? AppCompatActivity)?.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        (activity as? AppCompatActivity)?.supportActionBar?.setDisplayShowHomeEnabled(true)

        viewBinding.topAppBar.setNavigationOnClickListener {
            activity?.onBackPressed()
        }

        viewBinding.rcvSms.layoutManager = LinearLayoutManager(requireContext())
        smsAdapter = SmsAdapter()
        viewBinding.rcvSms.adapter = smsAdapter
        homeViewModel.fetchSms()
    }

    override fun initData() {
        observe(homeViewModel.stateLiveData, this::onNewStateSms)

    }

    private fun onNewStateSms(state: HomeViewState<MutableList<SmsModel>>) {

        lifecycleScope.launch {
            withContext(Dispatchers.Main) {
                smsAdapter.submitList(state.isSuccess)
            }
        }
    }

}