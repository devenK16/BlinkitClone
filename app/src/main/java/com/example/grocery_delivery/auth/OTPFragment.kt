package com.example.grocery_delivery.auth

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.grocery_delivery.R
import com.example.grocery_delivery.Utils
import com.example.grocery_delivery.activity.UsersMainActivity
import com.example.grocery_delivery.databinding.FragmentOTPBinding
import com.example.grocery_delivery.models.Users
import com.example.grocery_delivery.viewmodels.AuthViewModel
import kotlinx.coroutines.launch

class OTPFragment : Fragment() {
    private val viewmodel: AuthViewModel by viewModels()
    private lateinit var binding: FragmentOTPBinding
    private lateinit var userNumber: String
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentOTPBinding.inflate(layoutInflater)
        getUserNumber()
        customizeEnteringOTP()
        sendOtp()
        onBackButtonClicked()
        setStatusBarColor()
        onLoginButtonClicked()
        return binding.root
    }

    private fun onLoginButtonClicked() {
        binding.btnLogin.setOnClickListener {
            Utils.showDialog(requireContext(), "Signing You...")
            val editTexts = arrayOf(
                binding.otp1,
                binding.otp2,
                binding.otp3,
                binding.otp4,
                binding.otp5,
                binding.otp6
            )
            val otp = editTexts.joinToString("") {
                it.text.toString()
            }

            if (otp.length < editTexts.size) {
                Utils.showToast(requireContext(), "Please enter correct otp")
            } else {
                editTexts.forEach {
                    it.text?.clear(); it.clearFocus()

                }
                verifyOtp(otp)
            }
        }
    }

    private fun verifyOtp(otp: String) {
        val user =
            Users(uid = Utils.getCurrentUid(), userPhoneNumber = userNumber, userAddress = null)
        viewmodel.apply {
            signInWithPhoneAuthCredential(otp, userNumber , user)

            lifecycleScope.launch {
                viewmodel.isSignedInSuccessfully.collect {
                    if (it) {
                        Utils.hideDialog()
                        Utils.showToast(requireContext(), "Logged In")
                        val intent = Intent( requireContext() , UsersMainActivity::class.java)
                        startActivity(intent)
                        requireActivity().finish()
                    }
                }
            }
        }
    }

    private fun sendOtp() {
        Utils.showDialog(requireContext(), "Sending OTP")

        viewmodel.apply {
            sendOtp(userNumber, requireActivity())
            lifecycleScope.launch {
                otpSent.collect { otpSent ->
                    if (otpSent) {
                        Utils.hideDialog()
                        Utils.showToast(requireContext(), "Otp Sent..")
                    }
                }
            }
        }
    }

    private fun onBackButtonClicked() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigate(R.id.action_OTPFragment_to_signInFragment)
        }
    }

    private fun customizeEnteringOTP() {
        val editTexts = arrayOf(
            binding.otp1,
            binding.otp2,
            binding.otp3,
            binding.otp4,
            binding.otp5,
            binding.otp6
        )
        for (i in editTexts.indices) {
            editTexts[i].addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                }

                override fun afterTextChanged(s: Editable?) {
                    if (s?.length == 1) {
                        if (i < editTexts.size - 1) {
                            editTexts[i + 1].requestFocus()
                        }
                    } else if (s?.length == 0) {
                        if (i > 0) {
                            editTexts[i - 1].requestFocus()
                        }
                    }
                }

            })
        }
    }

    private fun getUserNumber() {
        val bundle = arguments
        userNumber = bundle?.getString("number").toString()
        binding.tvnumber.text = userNumber
    }

    private fun setStatusBarColor() {
        activity?.window?.apply {
            val statusBarColors = ContextCompat.getColor(requireContext(), R.color.yellow)
            statusBarColor = statusBarColors
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
        }
    }

}