package com.horizam.pro.elean.ui.main.view.fragments.manageSales

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProviders
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.gson.Gson
import com.horizam.pro.elean.BuyerOrders
import com.horizam.pro.elean.Constants
import com.horizam.pro.elean.SellerOrders
import com.horizam.pro.elean.data.api.ApiHelper
import com.horizam.pro.elean.data.api.RetrofitBuilder
import com.horizam.pro.elean.data.model.response.Order
import com.horizam.pro.elean.data.model.response.OrdersResponse
import com.horizam.pro.elean.databinding.FragmentOrdersGenericBinding
import com.horizam.pro.elean.ui.base.ViewModelFactory
import com.horizam.pro.elean.ui.main.adapter.*
import com.horizam.pro.elean.ui.main.callbacks.GenericHandler
import com.horizam.pro.elean.ui.main.callbacks.OnItemClickListener
import com.horizam.pro.elean.ui.main.view.activities.OrderDetailsActivity
import com.horizam.pro.elean.ui.main.viewmodel.SellerOrdersViewModel
import com.horizam.pro.elean.utils.Status
import java.lang.Exception


class RevisionSalesFragment : Fragment(), OnItemClickListener, SwipeRefreshLayout.OnRefreshListener {

    private lateinit var binding: FragmentOrdersGenericBinding
    private lateinit var adapter: ActiveSalesAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewModel: SellerOrdersViewModel
    private lateinit var genericHandler: GenericHandler
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    override fun onAttach(context: Context) {
        super.onAttach(context)
        genericHandler = context as GenericHandler
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOrdersGenericBinding.inflate(layoutInflater, container, false)
        initViews()
        setupViewModel()
        setupObservers()
        setRecyclerView()
        setOnClickListeners()
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        exeApi()
    }

    private fun exeApi() {
        viewModel.getSellerOrdersCall(BuyerOrders.Revision)
    }

    private fun initViews() {
        adapter = ActiveSalesAdapter(this)
        recyclerView = binding.rvOrders
        swipeRefreshLayout = binding.swipeRefresh
        swipeRefreshLayout.setOnRefreshListener(this)
    }

    private fun setRecyclerView() {
//        recyclerView.layoutManager = LinearLayoutManager(requireContext())
//        recyclerView.adapter = adapter
//    }
        val linearLayoutManager = LinearLayoutManager(requireContext()).also { layoutManager ->
            layoutManager.reverseLayout = true
            layoutManager.stackFromEnd = false
        }
        recyclerView.let {
            it.setHasFixedSize(true)
            it.layoutManager = linearLayoutManager
            it.adapter = adapter.withLoadStateFooter(
                footer = MyLoadStateAdapter {
                    adapter.retry()
                }
            )
        }
        setAdapterLoadState(adapter)
        adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                if (positionStart == 0) {
                    linearLayoutManager.scrollToPosition(0)
                }
            }
        })
        setupObservers()
    }
    private fun setAdapterLoadState(adapter:ActiveSalesAdapter) {
        adapter.addLoadStateListener { loadState ->
            binding.apply {
//                genericHandler.showProgressBar(loadState.source.refresh is LoadState.Loading)
//                recyclerView.isVisible = loadState.source.refresh is LoadState.NotLoading
                btnRetry.isVisible = loadState.source.refresh is LoadState.Error
//                textViewError.isVisible = loadState.source.refresh is LoadState.Error
                // no results
                if (loadState.source.refresh is LoadState.NotLoading &&
                    loadState.append.endOfPaginationReached &&
                    adapter.itemCount < 1
                ) {
                    recyclerView.isVisible = false
                    tvPlaceholder.isVisible = true
                } else {
                    tvPlaceholder.isVisible = false
                }
            }
        }
    }


    private fun setOnClickListeners() {
        binding.apply {
            btnRetry.setOnClickListener {
                viewModel.getSellerOrdersCall(BuyerOrders.Revision)
            }
        }
    }

    private fun setupViewModel() {
        viewModel = ViewModelProviders.of(
            this,
            ViewModelFactory(ApiHelper(RetrofitBuilder.apiService))
        ).get(SellerOrdersViewModel::class.java)
    }

    private fun setupObservers() {
        viewModel.sellerOrders.observe(viewLifecycleOwner, {
            adapter.submitData(viewLifecycleOwner.lifecycle, it)
        })
    }
//            it?.let { resource ->
//                when (resource.status) {
//                    Status.SUCCESS -> {
//                        genericHandler.showProgressBar(false)
//                        resource.data?.let { response ->
//                            handleResponse(response)
//                            changeViewVisibility(textView = false, button = false, layout = true)
//                        }
//                    }
//                    Status.ERROR -> {
//                        genericHandler.showProgressBar(false)
//                        genericHandler.showErrorMessage(it.message.toString())
//                        changeViewVisibility(textView = true, button = true, layout = false)
//                    }
//                    Status.LOADING -> {
//                        genericHandler.showProgressBar(true)
//                        changeViewVisibility(textView = false, button = false, layout = false)
//                    }
//                }
//            }
//        })
//    }

//    private fun changeViewVisibility(textView: Boolean, button: Boolean, layout: Boolean) {
//        binding.textViewError.isVisible = textView
//        binding.btnRetry.isVisible = button
//        binding.rvOrders.isVisible = layout
//    }
//
//    private fun handleResponse(response: OrdersResponse) {
//        try {
//            setUIData(response.orderList)
//        } catch (e: Exception) {
//            genericHandler.showErrorMessage(e.message.toString())
//        }
//    }
//
//    private fun setUIData(list: List<Order>) {
//        adapter.submitList(list)
//        binding.tvPlaceholder.isVisible = list.isEmpty()
//    }

    override fun <T> onItemClick(item: T) {
        if (item is Order) {
            Intent(requireContext(), OrderDetailsActivity::class.java).also {
                val gson = Gson()
                it.putExtra(Constants.ORDER, gson.toJson(item))
                it.putExtra(Constants.ORDER_USER_ROLE, Constants.SELLER_USER)
                it.putExtra(Constants.ORDER_USER_ACTION, SellerOrders.Revision)
                resultLauncher.launch(it)
            }
        }
    }

    private val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            exeApi()
        }
    }

    override fun onRefresh() {
        if (swipeRefreshLayout.isRefreshing) {
            swipeRefreshLayout.isRefreshing = false
        }
        exeApi()
    }

}