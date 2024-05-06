package com.example.dtclnh.presentation.page.login

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.viewbinding.ViewBinding
import com.example.dtclnh.core.Constants
import com.example.dtclnh.core.Errors
import com.example.dtclnh.databinding.FragmentLoginBinding
import com.example.dtclnh.domain.model.BaseResponse
import com.example.dtclnh.domain.model.SmsModel
import com.example.dtclnh.presentation.base.ext.observe
import com.example.dtclnh.presentation.base.view.BaseFragment
import com.example.dtclnh.presentation.broadcast.SyncService
import com.example.dtclnh.util.showToast
import dagger.hilt.android.AndroidEntryPoint
import retrofit2.HttpException


@AndroidEntryPoint
class LoginFragment : BaseFragment() {

    private lateinit var viewBinding: FragmentLoginBinding

    private val loginViewModel: LoginViewModel by viewModels()


    companion object {
        private const val TAG = "LoginFragment"
    }

    override fun setViewBinding(): ViewBinding {
        viewBinding = FragmentLoginBinding.inflate(layoutInflater)
        return viewBinding
    }

    private val requestForegroundPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissionsStatusMap ->
            if (!permissionsStatusMap.containsValue(false)) {
                loginViewModel.fetchAllSmsFromLocal()
                val serviceIntent = Intent(requireContext(), SyncService::class.java)
                requireContext().startService(serviceIntent)
            } else {
                navigateToSetting()
            }
        }

    private val requestSmsPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                loginViewModel.readAllSMS()
            } else {
                navigateToSetting()
            }
        }

    private fun navigateToSetting() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", requireActivity().packageName, null)
        intent.setData(uri)
        startActivity(intent)
    }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun initView() {

        setUpUI(viewBinding.cslLogin)

        loginViewModel.saveClientId(Constants.CLIENT_ID)
        loginViewModel.getClientId()?.let {
            viewBinding.editClientId.setText(it)
        }
        loginViewModel.getApiUrl()?.let {
            viewBinding.editApiUrl.setText(it)
        }

        loginViewModel.getApiKey()?.let {
            viewBinding.editApiKey.setText(it)
        }


        loginViewModel.initData()



        viewBinding.switchOnOff.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                if (ContextCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.FOREGROUND_SERVICE
                    ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.READ_SMS
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    loginViewModel.fetchAllSmsFromLocal()
                    val serviceIntent = Intent(requireContext(), SyncService::class.java)
                    requireContext().startService(serviceIntent)
                } else {
                    requestForegroundPermissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.READ_SMS,
                            Manifest.permission.FOREGROUND_SERVICE
                        )
                    )
                }
            } else {
                val serviceIntent = Intent(requireContext(), SyncService::class.java)
                requireContext().stopService(serviceIntent)
            }

        }






        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_SMS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            loginViewModel.readAllSMS()

        } else {
            requestSmsPermissionLauncher.launch(
                Manifest.permission.READ_SMS
            )

        }

        viewBinding.editApiKey.addTextChangedListener {
            loginViewModel.setApiKey(it.toString().trim())
        }

        viewBinding.editApiUrl.addTextChangedListener {
            loginViewModel.setApiUrl(it.toString().trim())
        }
        viewBinding.editClientId.addTextChangedListener {
            loginViewModel.setClientId(it.toString().trim())
        }
    }

    private fun hideSoftKeyboard(activity: Activity) {
        val inputMethodManager = activity.getSystemService(
            Activity.INPUT_METHOD_SERVICE
        ) as InputMethodManager
        if (inputMethodManager.isAcceptingText) {
            inputMethodManager.hideSoftInputFromWindow(
                activity.currentFocus!!.windowToken,
                0
            )
        }
    }

    private fun setUpUI(view: View) {

        if (view !is EditText) {
            view.setOnTouchListener { v, event ->
                hideSoftKeyboard(requireActivity())
                loginViewModel.saveApiKey(null)
                loginViewModel.saveApiUrl(null)
                loginViewModel.saveClientId(null)
                false
            }
        }

        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                val innerView = view.getChildAt(i)
                setUpUI(innerView)
            }
        }
    }


    override fun initData() {
        observe(loginViewModel.stateLiveData, this::onNewStateSms)
    }

    private fun onNewStateSms(state: LoginViewState<MutableList<SmsModel>>) {
        if (state.isSuccess?.isNotEmpty() == true) {
            viewBinding.tvCount.text = "${state.isSuccess.size}"
            state.isSuccess?.forEach {
                Log.e("AMBE1203", it.content)
            }
        } else {
            viewBinding.tvCount.text = "0"
        }

        viewBinding.switchOnOff.isEnabled =
            state.apiKey?.isNotEmpty() == true
                    && state.clientId?.isNotEmpty() == true
                    && state.apiUrl?.isNotEmpty() == true

    }

    private fun onNewState(state: LoginViewState<BaseResponse<SmsModel>>) {
        if (state.throwable != null) {
            when (state.throwable) {
                is Errors.EmptyInputError -> "username or password can't be null."
                is HttpException ->
                    when (state.throwable.code()) {
                        401 -> "username or password failure."
                        else -> "network failure"
                    }

                is Errors.NoInternetError -> "No Internet"
                else -> "network failure"
            }.also { str ->
                showToast { str }
            }
        }


    }
}