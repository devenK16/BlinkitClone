package com.example.grocery_delivery.auth

import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.grocery_delivery.R
import com.example.grocery_delivery.Utils
import com.example.grocery_delivery.databinding.FragmentSignInBinding

/**
 * A simple [Fragment] subclass.
 * Use the [SignInFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SignInFragment : Fragment() {
    private lateinit var binding: FragmentSignInBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSignInBinding.inflate(layoutInflater)
        SetStatusBarColor()
        getUserNumber()
        onContinueButtonClick()
        return binding.root
    }

    private fun onContinueButtonClick() {
        binding.continuebtn.setOnClickListener {
            val number=binding.etUserNumber.text.toString()

            if (number.isEmpty() || number.length != 10 ){
                Utils.showToast(requireContext()  ,"please enter valid phone number")
            }
            else{
                val bundle= Bundle()
                bundle.putString("number", number)
                findNavController().navigate(R.id.action_signInFragment_to_OTPFragment,bundle)
            }
        }
    }
    private fun getUserNumber() {
        binding.etUserNumber.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(number: CharSequence?, start: Int, before: Int, count: Int) {
                if (number?.length==10){
                    binding.continuebtn.setBackgroundColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.green
                        )
                    )
                }else{
                    binding.continuebtn.setBackgroundColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.grayish_blue
                        )
                    )
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }
    private fun SetStatusBarColor(){
        activity?.window?.apply{
            val statusBarColors = ContextCompat.getColor(requireContext(), R.color.yellow)
            statusBarColor = statusBarColors
            if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
        }
    }
}