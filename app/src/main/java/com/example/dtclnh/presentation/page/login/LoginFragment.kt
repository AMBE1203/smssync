package com.example.dtclnh.presentation.page.login

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.content.*
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.preference.PreferenceManager
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
import com.example.dtclnh.presentation.base.ext.observe
import com.example.dtclnh.presentation.base.view.BaseFragment
import com.example.dtclnh.presentation.broadcast.SyncService
import com.example.dtclnh.presentation.view.BottomSheetDismissListener
import com.example.dtclnh.presentation.view.BottomSheetFragment
import com.example.dtclnh.util.showToast
import dagger.hilt.android.AndroidEntryPoint
import retrofit2.HttpException
import java.util.regex.Pattern
import javax.inject.Inject


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
                loginViewModel.countNumberSmsForBackUp()
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
        loginViewModel.saveClientId(Constants.CLIENT_ID)



        loginViewModel.initData()

//        viewBinding.swOnOff.isEnabled =
//            loginViewModel.getClientId()?.isNotEmpty() == true
//                    && loginViewModel.getApiUrl()?.isNotEmpty() == true
//                    && loginViewModel.getApiKey()?.isNotEmpty() == true


        viewBinding.swOnOff.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                if (ContextCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.FOREGROUND_SERVICE
                    ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.READ_SMS
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    if (isLink(loginViewModel.getApiUrl() ?: "") && loginViewModel.getApiKey()
                            ?.isNotEmpty() == true && loginViewModel.getClientId()
                            ?.isNotEmpty() == true
                    ) {
                        loginViewModel.countNumberSmsForBackUp()
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

        viewBinding.swOnOff.isChecked =
            isServiceRunning(requireContext(), SyncService::class.java)

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


    private fun isLink(inputString: String): Boolean {
        val pattern = Pattern.compile("^https?://\\S+")
        val matcher = pattern.matcher(inputString)
        return matcher.matches()
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == ACTION_WORK_SUCCESS) {
                loginViewModel.countNumberSmsForBackUp()
                viewBinding.mProgressBar.visibility = View.GONE
                viewBinding.tvStatus.text = getString(R.string.sync_success)

            } else if (intent?.action == ACTION_WORK_RUNNING) {
                viewBinding.mProgressBar.visibility = View.VISIBLE
                viewBinding.tvStatus.text = getString(R.string.sync_running)
            } else if (intent?.action == ACTION_WORK_FAIL) {
                viewBinding.mProgressBar.visibility = View.GONE
                val error = intent.extras?.getString("error")
                error?.let {
                    Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
                }
                viewBinding.tvStatus.text = getString(R.string.sync_fail)
            }
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
        if (state.numberSmsNotBackUp != null) {
            viewBinding.tvTotal.text = "${state.numberSmsNotBackUp}"
        } else {
            viewBinding.tvTotal.text = "0"
        }

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