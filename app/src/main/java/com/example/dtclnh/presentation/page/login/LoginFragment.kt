package com.example.dtclnh.presentation.page.login

import android.view.View
import androidx.fragment.app.viewModels
import androidx.viewbinding.ViewBinding
import com.example.dtclnh.core.Errors
import com.example.dtclnh.databinding.FragmentLoginBinding
import com.example.dtclnh.domain.model.BaseResponse
import com.example.dtclnh.domain.model.UserModel
import com.example.dtclnh.presentation.base.ext.observe
import com.example.dtclnh.presentation.base.view.BaseFragment
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

    override fun setViewBinding(): ViewBinding? {
        viewBinding = FragmentLoginBinding.inflate(layoutInflater)
        return viewBinding
    }


    override fun initView() {
    }

    override fun initData() {
        viewBinding.mBtnSignIn.setSafeOnClickListener {
            loginViewModel.login(
                viewBinding.tvUsername.text.toString(),
                viewBinding.tvPassword.text.toString()
            )
        }

        observe(loginViewModel.loginLiveData, this::onNewState)

    }

    private fun onNewState(state: LoginViewState<BaseResponse<UserModel>>) {
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


        viewBinding.mProgressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE
        viewBinding.txtErrorEmail.visibility =
            if (state.showErrorValidEmail) View.VISIBLE else View.GONE

        if (state.result != null) {

            if (state.result.code != 1) {
                showToast { state.result.message }
            } else {
                showToast { state.result.data?.userName.toString() }

            }

        }

    }
}