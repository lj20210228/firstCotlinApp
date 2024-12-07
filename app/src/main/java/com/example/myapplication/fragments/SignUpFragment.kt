package com.example.myapplication.fragments
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentSignUpBinding
import com.google.firebase.auth.FirebaseAuth



class SignUpFragment : Fragment() {
    private lateinit var auth:FirebaseAuth
    private lateinit var navControl:NavController
    private lateinit var binding: FragmentSignUpBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
       binding= FragmentSignUpBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init(view)
        registerEvents()
    }
    private fun init(view: View)
    {
        navControl=Navigation.findNavController(view)
        auth=FirebaseAuth.getInstance()
    }
    private fun registerEvents()
    {
        binding.authTextView.setOnClickListener{
            navControl.navigate(R.id.action_signUpFragment_to_signInFragment)
        }
        binding.nextBtn.setOnClickListener{
            val email=binding.emailEt.text.toString().trim()
            val pass=binding.passEt.text.toString().trim()
            val verifyPass=binding.rePassEt.text.toString().trim()
            if(email.isNotEmpty()&&pass.isNotEmpty()&&verifyPass.isNotEmpty()){
                if(pass==verifyPass){
                    binding.progressBar.visibility=View.VISIBLE
                    auth.createUserWithEmailAndPassword(email,pass).
                            addOnCompleteListener { it ->
                                if (it.isSuccessful) {
                                    Toast.makeText(
                                        context,
                                        "Registered Successfully",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    navControl.navigate(R.id.action_signUpFragment_to_homeFragment)
                                } else {
                                    Toast.makeText(
                                        context,
                                        it.exception?.message,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                binding.progressBar.visibility = View.GONE
                            }
                }else{
                    Toast.makeText(context,"Empty Fields not allowed",Toast.LENGTH_SHORT).show()
                }
            }else{
                Toast.makeText(context,"Password doesn't match",Toast.LENGTH_SHORT).show()
            }
        }
    }



}