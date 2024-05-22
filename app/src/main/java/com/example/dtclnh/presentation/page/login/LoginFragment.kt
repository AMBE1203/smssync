package com.example.dtclnh.presentation.page.login

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.content.*
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.CountDownTimer
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.viewbinding.ViewBinding
import androidx.work.WorkManager
import com.example.dtclnh.R
import com.example.dtclnh.core.Constants
import com.example.dtclnh.core.Constants.ACTION_WORK_FAIL
import com.example.dtclnh.core.Constants.ACTION_WORK_RUNNING
import com.example.dtclnh.core.Constants.ACTION_WORK_SUCCESS
import com.example.dtclnh.core.Constants.WORK_MANAGER_ID
import com.example.dtclnh.core.Errors
import com.example.dtclnh.databinding.FragmentLoginBinding
import com.example.dtclnh.domain.model.BaseResponse
import com.example.dtclnh.domain.model.SmsModel
import com.example.dtclnh.presentation.base.ext.convertSecondsToMMSS
import com.example.dtclnh.presentation.base.ext.isLink
import com.example.dtclnh.presentation.base.ext.observe
import com.example.dtclnh.presentation.base.view.BaseFragment
import com.example.dtclnh.presentation.broadcast.SyncService
import com.example.dtclnh.presentation.view.BottomSheetDismissListener
import com.example.dtclnh.presentation.view.BottomSheetFragment
import com.example.dtclnh.util.showToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.util.concurrent.TimeUnit


@AndroidEntryPoint
class LoginFragment : BaseFragment(), BottomSheetDismissListener {

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
                lifecycleScope.launch {
                    loginViewModel.countNumberSmsForBackUp()

                }
                val serviceIntent = Intent(requireContext(), SyncService::class.java)
                requireContext().startService(serviceIntent)
            } else {
                navigateToSetting()
            }
        }

    private val requestSmsPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissionsStatusMap ->
            val a = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                permissionsStatusMap[Manifest.permission.FOREGROUND_SERVICE]
            } else {
                true
            }
            val b = permissionsStatusMap[Manifest.permission.READ_SMS]
            val c = permissionsStatusMap[Manifest.permission.RECEIVE_SMS]
            val d = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissionsStatusMap[Manifest.permission.POST_NOTIFICATIONS]
            } else {
                true
            }

            if (a == true && b == true && c == true && d == true) {

                lifecycleScope.launch {
                    async(Dispatchers.IO) {
                        loginViewModel.readAllSMS()
                    }.await()
                    loginViewModel.countNumberSmsForBackUp()
                }

            } else {
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
    override fun initView() {

        loginViewModel.getClientId().let {
            if (it?.isEmpty() == true) {
                loginViewModel.saveClientId(Constants.CLIENT_ID)
            }
        }

        loginViewModel.getApiKey().let {
            if (it?.isEmpty() == true) {
                loginViewModel.saveApiKey(Constants.API_KEY)
            }
        }

        loginViewModel.getApiUrl().let {
            if (it?.isEmpty() == true) {
                loginViewModel.saveApiUrl(Constants.API_URL)
            }
        }



        loginViewModel.initData()



        viewBinding.swOnOff.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                if (ContextCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.FOREGROUND_SERVICE
                    ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.READ_SMS
                    ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.RECEIVE_SMS
                    ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.RECEIVE_SMS
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    if (isLink(loginViewModel.getApiUrl() ?: "") && loginViewModel.getApiKey()
                            ?.isNotEmpty() == true && loginViewModel.getClientId()
                            ?.isNotEmpty() == true
                    ) {

                        try {
                            WorkManager.getInstance(requireActivity().applicationContext)
                                .cancelUniqueWork(WORK_MANAGER_ID)
                            WorkManager.getInstance(requireActivity().applicationContext)
                                .cancelAllWork()

                            if (isServiceRunning(requireContext(), SyncService::class.java)) {

                                val serviceIntent =
                                    Intent(requireContext(), SyncService::class.java)
                                requireContext().stopService(serviceIntent)
                            }

                        } catch (_: Exception) {

                        }


                        lifecycleScope.launch {
                            loginViewModel.countNumberSmsForBackUp()
                        }
                        val serviceIntent = Intent(requireContext(), SyncService::class.java)
                        requireContext().startService(serviceIntent)
                    } else {

                        var error = R.string.api_url_incorrect
                        if (loginViewModel.getApiKey()?.isNotEmpty() == false) {
                            error = R.string.api_key_incorrect
                        } else if (loginViewModel.getClientId()?.isNotEmpty() == false) {
                            error = R.string.api_client_incorrect
                        }
                        Toast.makeText(
                            requireContext(),
                            getString(error),
                            Toast.LENGTH_LONG
                        ).show()
                        viewBinding.swOnOff.isChecked = false
                    }
                } else {
                    var per = arrayOf(
                        Manifest.permission.READ_SMS,
                        Manifest.permission.RECEIVE_SMS,

                        )
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        per += Manifest.permission.FOREGROUND_SERVICE
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        per += Manifest.permission.POST_NOTIFICATIONS;
                    }

                    requestForegroundPermissionLauncher.launch(per)
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

        viewBinding.swOnOff.isChecked =
            isServiceRunning(requireContext(), SyncService::class.java)

        val a = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.FOREGROUND_SERVICE
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
        val b = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.READ_SMS
        ) == PackageManager.PERMISSION_GRANTED
        val c = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.RECEIVE_SMS
        ) == PackageManager.PERMISSION_GRANTED
        val d = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }

        if (a && b && c && d) {
            lifecycleScope.launch {
                async(Dispatchers.IO) {
                    loginViewModel.readAllSMS()
                }.await()
                loginViewModel.countNumberSmsForBackUp()
            }


        } else {

            var per = arrayOf(
                Manifest.permission.READ_SMS,
                Manifest.permission.RECEIVE_SMS,

                )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                per += Manifest.permission.FOREGROUND_SERVICE
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                per += Manifest.permission.POST_NOTIFICATIONS;
            }

            requestSmsPermissionLauncher.launch(per)


        }


        val filter = IntentFilter(ACTION_WORK_SUCCESS)
        LocalBroadcastManager.getInstance(requireContext().applicationContext)
            .registerReceiver(receiver, filter)

        val filterRunning = IntentFilter(ACTION_WORK_RUNNING)
        LocalBroadcastManager.getInstance(requireContext().applicationContext)
            .registerReceiver(receiver, filterRunning)
        val filterFail = IntentFilter(ACTION_WORK_FAIL)
        LocalBroadcastManager.getInstance(requireContext().applicationContext)
            .registerReceiver(receiver, filterFail)

        viewBinding.cardSetting.setOnClickListener {
            showBottomSheet()

        }


    }

    private fun showBottomSheet() {
        val bottomSheetFragment = BottomSheetFragment()
        bottomSheetFragment.setBottomSheetDismissListener(this)

        bottomSheetFragment.show(requireActivity().supportFragmentManager, bottomSheetFragment.tag)

    }




    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == ACTION_WORK_SUCCESS) {
                lifecycleScope.launch {
                    loginViewModel.countNumberSmsForBackUp()
                }
//                viewBinding.tvErr.text = ""
                viewBinding.mProgressBar.visibility = View.GONE
                viewBinding.tvTotal.text = getString(R.string.sync_success)

            } else if (intent?.action == ACTION_WORK_RUNNING) {
                viewBinding.mProgressBar.visibility = View.VISIBLE
//                viewBinding.tvErr.text = ""
                viewBinding.tvTotal.text = getString(R.string.sync_running)
                countDownTimer.cancel()
                countDownTimer.start()
            } else if (intent?.action == ACTION_WORK_FAIL) {
                viewBinding.mProgressBar.visibility = View.GONE
                val error = intent.extras?.getString("error")
                error?.let {
                    viewBinding.tvErr.text = it
                }
                viewBinding.tvTotal.text = getString(R.string.sync_fail)
            }
        }
    }

    val countDownTimer = object : CountDownTimer(TimeUnit.MINUTES.toMillis(15), 1000) {
        override fun onTick(millisUntilFinished: Long) {
            val remainingTime = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished)
            val timeInHHMM = convertSecondsToMMSS(remainingTime)
            viewBinding.tvTime.text = timeInHHMM
        }

        override fun onFinish() {
            viewBinding.tvTime.text = getString(R.string.next_time)
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(receiver)
    }


    override fun initData() {
        observe(loginViewModel.stateLiveData, this::onNewStateSms)
    }


    private fun onNewStateSms(state: LoginViewState<MutableList<SmsModel>>) {
        viewBinding.tvFail.text = "${state.numberSmsNotBackUp ?: 0}"


        viewBinding.tvSuccess.text = "${state.numberSmsBackUpSuccess ?: 0}"


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

    override fun onBottomSheetDismissed(clientItd: String, apiKey: String, apiUrl: String) {
        loginViewModel.saveApiKey(apiKey)
        loginViewModel.saveApiUrl(apiUrl)
        loginViewModel.saveClientId(clientItd)
    }
}