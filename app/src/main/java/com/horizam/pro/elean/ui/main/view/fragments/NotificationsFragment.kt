package com.horizam.pro.elean.ui.main.view.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.view.plusAssign
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.horizam.pro.elean.App
import com.horizam.pro.elean.Constants
import com.horizam.pro.elean.R
import com.horizam.pro.elean.data.api.ApiHelper
import com.horizam.pro.elean.data.api.RetrofitBuilder
import com.horizam.pro.elean.data.model.response.GeneralResponse
import com.horizam.pro.elean.data.model.response.Notification
import com.horizam.pro.elean.data.model.response.NotificationsResponse
import com.horizam.pro.elean.databinding.FragmentNotificationsBinding
import com.horizam.pro.elean.ui.base.ViewModelFactory
import com.horizam.pro.elean.ui.main.adapter.NotificationsAdapter
import com.horizam.pro.elean.ui.main.callbacks.GenericHandler
import com.horizam.pro.elean.ui.main.callbacks.NotificationsHandler
import com.horizam.pro.elean.ui.main.callbacks.OnItemClickListener
import com.horizam.pro.elean.ui.main.view.activities.HomeActivity
import com.horizam.pro.elean.ui.main.view.activities.SplashActivity
import com.horizam.pro.elean.ui.main.view.activities.UserGigDetailsActivity
import com.horizam.pro.elean.ui.main.viewmodel.NotificationsViewModel
import com.horizam.pro.elean.utils.Status
import kotlinx.android.synthetic.main.item_manage_service.view.*
import java.lang.Exception


class NotificationsFragment : Fragment(), NotificationsHandler, OnItemClickListener {

    private lateinit var binding: FragmentNotificationsBinding
    private lateinit var homeFragment: HomeFragment
    private lateinit var adapter: NotificationsAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewModel: NotificationsViewModel
    private lateinit var genericHandler: GenericHandler
    override fun onAttach(context: Context) {
        super.onAttach(context)
        genericHandler = context as GenericHandler

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNotificationsBinding.inflate(layoutInflater, container, false)



        setToolbarData()
        initViews()
        setupViewModel()
        setupObservers()
        ReadAdapter()
        setRecyclerView()
        setOnClickListeners()

        return binding.root
    }

    private fun initViews() {
        adapter = NotificationsAdapter(this)
        recyclerView = binding.rvNotifications
    }

    private fun setRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.addItemDecoration(
            DividerItemDecoration(
                recyclerView.context,
                (recyclerView.layoutManager as LinearLayoutManager).orientation
            )
        )
        recyclerView.adapter = adapter
    }

    private fun setOnClickListeners() {
        binding.apply {
            toolbar.ivToolbar.setOnClickListener {
                viewModel.getNotificationsCall()
                findNavController().popBackStack()
            }
            btnRetry.setOnClickListener {
                viewModel.getNotificationsCall()
                viewModel.getNotificationsReadCall()
            }
        }
    }
    private fun setToolbarData() {
        binding.toolbar.ivToolbar.setImageResource(R.drawable.ic_back)
        binding.toolbar.ivToolbar.visibility=View.VISIBLE
        binding.toolbar.tvToolbar.text =getString(R.string.str_notifications)}

    private fun setupViewModel() {
        viewModel = ViewModelProviders.of(
            this,
            ViewModelFactory(ApiHelper(RetrofitBuilder.apiService))
        ).get(NotificationsViewModel::class.java)
    }

    private fun setupObservers() {
        viewModel.notifications.observe(viewLifecycleOwner, {
            it?.let { resource ->
                when (resource.status) {
                    Status.SUCCESS -> {
                        genericHandler.showProgressBar(false)
                        resource.data?.let { response ->
                            handleResponse(response)
                            changeViewVisibility(textView = false, button = false, layout = true)
                        }
                    }
                    Status.ERROR -> {
                        genericHandler.showProgressBar(false)
                        genericHandler.showErrorMessage(it.message.toString())
                        changeViewVisibility(textView = true, button = true, layout = false)
                    }
                    Status.LOADING -> {
                        genericHandler.showProgressBar(true)
                        changeViewVisibility(textView = false, button = false, layout = false)
                    }
                }
            }
        })
    }
    private fun ReadAdapter()
    {
    viewModel.notificationsRead.observe(viewLifecycleOwner, {
        it?.let { resource ->
            when (resource.status) {
                Status.SUCCESS -> {
                    genericHandler.showProgressBar(false)
                    resource.data?.let { response ->
                        handleResponses(response)
                        changeViewVisibility(textView = false, button = false, layout = true)
                    }
                }
                Status.ERROR -> {
                    genericHandler.showProgressBar(false)
                    genericHandler.showErrorMessage(it.message.toString())
                    changeViewVisibility(textView = true, button = true, layout = false)
                }
                Status.LOADING -> {
                    genericHandler.showProgressBar(true)
                    changeViewVisibility(textView = false, button = false, layout = false)
                }
            }
        }
    })
}

    private fun changeViewVisibility(textView: Boolean, button: Boolean, layout: Boolean) {
        binding.textViewError.isVisible = textView
        binding.btnRetry.isVisible = button
        binding.rvNotifications.isVisible = layout
    }

    private fun handleResponse(response: NotificationsResponse) {
        try {
            setUIData(response.data)
//
        } catch (e: Exception) {
            genericHandler.showErrorMessage(e.message.toString())
        }
    }
    private fun handleResponses(response: GeneralResponse) {
        try {
            response.message
        } catch (e: Exception) {
            genericHandler.showErrorMessage(e.message.toString())
        }
    }

    private fun setUIData(list: List<Notification>) {
        adapter.submitList(list)
        binding.tvPlaceholder.isVisible = list.isEmpty()
    }

    override fun <T> onItemClick(item: T) {
        if (item is Notification) {
            val content_id = item.content_id
            val sender_id=item.sender_id
            val type=item.type
            if(type=="ORDER") {
                val bundle = Bundle()
                bundle.putString(Constants.TYPE, type)
                bundle.putString(Constants.CONTENT_ID, content_id)
                val int = Intent(context, HomeActivity::class.java)
                int.putExtras(bundle)
                startActivity(int)
            }
            else if(type=="MESSAGE")
            {
                val action =NotificationsFragmentDirections.actionNotificationsFragmentToMessagesFragment(sender_id)
                findNavController().navigate(action)
            }
            else if(type=="OFFER")
            {
                val action =NotificationsFragmentDirections.actionNotificationsFragmentToMessagesFragment(sender_id)
                findNavController().navigate(action)
            }
            else
            {

            }
        }
    }

}