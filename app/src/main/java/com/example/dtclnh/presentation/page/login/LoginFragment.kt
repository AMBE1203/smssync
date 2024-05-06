package com.example.dtclnh.presentation.page.login

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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
import androidx.lifecycle.LiveData
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.viewbinding.ViewBinding
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.example.dtclnh.core.Constants
import com.example.dtclnh.core.Constants.WORK_MANAGER_ID
import com.example.dtclnh.core.Constants.WORK_MANAGER_TAG
import com.example.dtclnh.core.Errors
import com.example.dtclnh.databinding.FragmentLoginBinding
import com.example.dtclnh.domain.model.BaseResponse
import com.example.dtclnh.domain.model.SmsModel
import com.example.dtclnh.domain.model.SyncStatus
import com.example.dtclnh.presentation.base.ext.observe
import com.example.dtclnh.presentation.base.view.BaseFragment
import com.example.dtclnh.presentation.broadcast.SyncService
import com.example.dtclnh.util.showToast
import com.google.common.util.concurrent.ListenableFuture
import dagger.hilt.android.AndroidEntryPoint
import retrofit2.HttpException
import java.util.concurrent.ExecutionException


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
        intent.data = uri
        startActivity(intent)
    }

    private fun isServiceRunning(context: Context, serviceClass: Class<*>): Boolean {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val services = activityManager.getRunningServices(Int.MAX_VALUE)
        for (service in services) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }


    @SuppressLint("SuspiciousIndentation")
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
                try {
                    WorkManager.getInstance(requireActivity().applicationContext)
                        .cancelUniqueWork(WORK_MANAGER_ID)
                    WorkManager.getInstance(requireActivity().applicationContext)
                        .cancelAllWork()
                    val serviceIntent = Intent(requireContext(), SyncService::class.java)
                    requireContext().stopService(serviceIntent)
                } catch (_: Exception) {

                }

            }

        }


        viewBinding.switchOnOff.isChecked =
            isServiceRunning(requireContext(), SyncService::class.java)

        WorkManager.getInstance(requireActivity().applicationContext)
            .getWorkInfosByTagLiveData(Constants.WORK_MANAGER_TAG).observe(this) {
                if (it.isNotEmpty()) {
                    val x = it[0]
                    when (x.state) {
                        WorkInfo.State.SUCCEEDED -> {
                            viewBinding.tvStatus.setText("aaaa")

                        }
                        WorkInfo.State.FAILED -> {
                            viewBinding.tvStatus.setText("bbbb")

                        }
                        else -> {}
                    }
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

        WorkManager.getInstance(requireActivity().applicationContext)
            .getWorkInfosByTagLiveData(WORK_MANAGER_TAG)
            .observe(this) { workInfos ->
                if (workInfos != null) {
                    workInfos.forEach { workInfo ->
                        val successData = workInfo.outputData.getBoolean("work_success_flag", false)
                        if (successData) {

                            Log.e("AMBE1203 ", " vao day 11")
                            // Xử lý khi công việc thành công
                            // ...
                        }
                    }


                }
            }

        val filter = IntentFilter("action_work_success")
        LocalBroadcastManager.getInstance(requireContext().applicationContext)
            .registerReceiver(receiver, filter)

        val filterRunning = IntentFilter("action_work_running")
        LocalBroadcastManager.getInstance(requireContext().applicationContext)
            .registerReceiver(receiver, filterRunning)


    }


    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "action_work_success") {
                // Xử lý khi công việc thành công
                // ...
                viewBinding.tvStatus.setText("action_work_success")

            } else if (intent?.action == "action_work_running") {
                viewBinding.tvStatus.setText("action_work_running")
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(receiver)
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

    private fun onNewSyncStatus(status: SyncStatus) {
        when (status) {
            SyncStatus.SYNCING -> viewBinding.tvStatus.setText("a")
            SyncStatus.SUCCESS -> viewBinding.tvStatus.setText("b")
            SyncStatus.ERROR -> viewBinding.tvStatus.setText("c")
        }

    }

    private fun onNewStateSms(state: LoginViewState<MutableList<SmsModel>>) {
        if (state.isSuccess?.isNotEmpty() == true) {
            viewBinding.tvCount.text = "${state.isSuccess.size}"
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