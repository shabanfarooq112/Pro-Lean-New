package com.horizam.pro.elean.ui.main.view.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.horizam.pro.elean.App
import com.horizam.pro.elean.R
import com.horizam.pro.elean.databinding.FragmentBuyerActionsBinding
import com.horizam.pro.elean.databinding.FragmentLoginBinding
import com.horizam.pro.elean.databinding.FragmentSignUpBinding


class BuyerActionsFragment : Fragment() {

    private lateinit var binding: FragmentBuyerActionsBinding
    private lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBuyerActionsBinding.inflate(layoutInflater,container,false)
        setToolbarData()
        initViews()
        setClickListeners()
        return binding.root
    }

    private fun initViews() {
        navController = this.findNavController()
    }

    private fun setClickListeners() {
        binding.toolbar.ivToolbar.setOnClickListener {
            navController.popBackStack()
        }
        binding.cardPostJob.setOnClickListener {
            navController.navigate(R.id.postJobFragment)
        }
        binding.cardPostedJobs.setOnClickListener {
            navController.navigate(R.id.postedJobsFragment)
        }
        binding.cardManageOrders.setOnClickListener {
//            startActivity(Intent(requireActivity(),ManageOrdersActivity::class.java))
        }
    }

    private fun setToolbarData() {
        binding.toolbar.ivToolbar.setImageResource(R.drawable.ic_back)
        binding.toolbar.ivToolbar.isVisible=true
        binding.toolbar.tvToolbar.text =getString(R.string.str_buyer_actions)
    }
}