package com.example.doctorq

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.navigation.fragment.findNavController
import com.example.doctorq.databinding.FragmentLoginBinding

class LoginFragment : Fragment() {
   private lateinit var binding:FragmentLoginBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.onBackPressedDispatcher?.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().navigateUp()
            }
        })

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding=FragmentLoginBinding.inflate(layoutInflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var isClicked: Boolean = false
        binding.apply {
            tvRegister.setOnClickListener(){
                findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToRegisterFragment())
            }
            toggleButton()

            btncheck.setOnClickListener(){
                findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToUserHomeFragment())
            }
        }

    }


    fun toggleButton() {
        //Toast.makeText(requireContext(), "clicked", Toast.LENGTH_SHORT).show()
        binding.apply {
            etPassword.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (s != null) {
                        if (s.isNotEmpty()) {
                            //Log.d("text", "onTextChanged: ${s}")
                            btnToggle.visibility = View.VISIBLE
                        } else {
                            //Log.d("nulltext", "onTextChanged: ${s}")
                            btnToggle.visibility = View.GONE
                        }
                    }
                }

                override fun afterTextChanged(s: Editable?) {

                }

            })
        }
    }

}