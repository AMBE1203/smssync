package com.example.dtclnh.presentation.view

import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import com.example.dtclnh.R
import com.example.dtclnh.core.Constants
import com.example.dtclnh.databinding.BottomSheetSettingBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class BottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: BottomSheetSettingBinding? = null

    private val binding get() = _binding!!

    private var mClientId: String? = null
    private var mApiKey: String? = null
    private var mApiUrl: String? = null

    @Inject
    lateinit var sharedPreferences: SharedPreferences


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = BottomSheetSettingBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val clientId = sharedPreferences.getString(Constants.CLIENT_ID_KEY, "")
        clientId?.let {
            mClientId = it
            binding.editClientId.setText(it)
        }

        val apiKey = sharedPreferences.getString(Constants.API_KEY_KEY, "")
        apiKey?.let {
            mApiKey = it
            binding.editApiKey.setText(it)
        }

        val apiUrl = sharedPreferences.getString(Constants.API_URL_KEY, "")
        apiUrl?.let {
            mApiUrl = it
            binding.editApiUrl.setText(it)
        }

        binding.editApiKey.addTextChangedListener {
            mApiKey = it.toString()
        }

        binding.editApiUrl.addTextChangedListener {
            mApiUrl = it.toString()
        }
        binding.editClientId.addTextChangedListener {
            mClientId = it.toString()
        }

        binding.btnSave.setOnClickListener {

            if (mClientId?.isNotEmpty() == true
                && mApiKey?.isNotEmpty() == true && mApiUrl?.isNotEmpty() == true
            ) {
                bottomSheetDismissListener?.onBottomSheetDismissed(
                    clientItd = mClientId ?: "",
                    apiKey = mApiKey ?: "",
                    apiUrl = mApiUrl ?: ""
                )
                dismiss()
            } else {
                var error = R.string.api_url_not_empty
                if (mApiKey?.isNotEmpty() == false) {
                    error = R.string.api_key_incorrect
                } else if (mClientId?.isNotEmpty() == false) {
                    error = R.string.api_client_incorrect
                }
                Toast.makeText(
                    requireContext(),
                    getString(error),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private var bottomSheetDismissListener: BottomSheetDismissListener? = null

    fun setBottomSheetDismissListener(listener: BottomSheetDismissListener) {
        bottomSheetDismissListener = listener
    }

}

interface BottomSheetDismissListener {
    fun onBottomSheetDismissed(clientItd: String, apiKey: String, apiUrl: String)
}


