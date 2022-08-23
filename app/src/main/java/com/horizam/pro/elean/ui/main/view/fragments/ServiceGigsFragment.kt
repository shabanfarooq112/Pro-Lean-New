package com.horizam.pro.elean.ui.main.view.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.gson.Gson
import com.horizam.pro.elean.Constants
import com.horizam.pro.elean.R
import com.horizam.pro.elean.data.api.ApiHelper
import com.horizam.pro.elean.data.api.RetrofitBuilder
import com.horizam.pro.elean.data.model.MessageGig
import com.horizam.pro.elean.data.model.SpinnerPriceModel
import com.horizam.pro.elean.data.model.requests.FavouriteRequest
import com.horizam.pro.elean.data.model.requests.SearchGigsRequest
import com.horizam.pro.elean.data.model.response.GeneralResponse
import com.horizam.pro.elean.data.model.response.ServiceDetail
import com.horizam.pro.elean.databinding.FragmentServiceGigsBinding
import com.horizam.pro.elean.ui.base.ViewModelFactory
import com.horizam.pro.elean.ui.main.adapter.GigsAdapter
import com.horizam.pro.elean.ui.main.adapter.MyLoadStateAdapter
import com.horizam.pro.elean.ui.main.adapter.PriceAdapter
import com.horizam.pro.elean.ui.main.callbacks.*
import com.horizam.pro.elean.ui.main.view.activities.AuthenticationActivity
import com.horizam.pro.elean.ui.main.viewmodel.ServiceGigsViewModel
import com.horizam.pro.elean.utils.BaseUtils.Companion.hideKeyboard
import com.horizam.pro.elean.utils.PrefManager
import com.horizam.pro.elean.utils.Resource
import com.horizam.pro.elean.utils.Status


class ServiceGigsFragment : Fragment(), OnItemClickListener, FavouriteHandler,
    ContactSellerHandler, AdapterView.OnItemSelectedListener, SwipeRefreshLayout.OnRefreshListener,
    LogoutHandler {

    private lateinit var binding: FragmentServiceGigsBinding
    private lateinit var adapter: GigsAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewModel: ServiceGigsViewModel
    private lateinit var genericHandler: GenericHandler
    private lateinit var prefManager: PrefManager
    private lateinit var priceArrayList: List<String>
    private lateinit var priceValueArrayList: List<String>
    private lateinit var priceAdapter: ArrayAdapter<SpinnerPriceModel>
    private var filter = ""
    private var filterValue = ""
    private val args: ServiceGigsFragmentArgs by navArgs()
    private var from: Int = 0
    private var delay: Long = 0
    private var delayCheck = 0
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    override fun onAttach(context: Context) {
        super.onAttach(context)
        genericHandler = context as GenericHandler
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupViewModel()
        setupFavoritesObservers()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentServiceGigsBinding.inflate(layoutInflater, container, false)
        initViews()
//        setupViewModel()
        setupObservers()
        setRecyclerview()
        setSearchFieldsListener()
        setOnClickListeners()
        executeApi()
        return binding.root
    }

    private fun setSearchFieldsListener() {
        binding.autoCompleteTextView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchManagement()
            }

            override fun afterTextChanged(s: Editable?) {
            }

        })
    }

    private fun searchManagement() {
        if (delayCheck == 0) {
            delay = 500
            delayCheck = 1
            if (delay > 0) {
                @Suppress("DEPRECATION")
                Handler().postDelayed({
                    delay = 0
                    delayCheck = 0
                    exeSearch()
                }, delay)
            }
        }
    }

    private fun executeApi() {
//        if (from == Constants.NORMAL_FLOW) {
        if(from==prefManager.sellerMode)
        {
        if (viewModel.sellers.value == null) {
            viewModel.getServicesBySubCategories(args.id)
            }
//
        }
    }

    override fun onRefresh() {
        if (swipeRefreshLayout.isRefreshing) {
            swipeRefreshLayout.isRefreshing = false
        }
        executeApi()
    }

    private fun initViews() {
        swipeRefreshLayout = binding.swipeRefresh
        swipeRefreshLayout.setOnRefreshListener(this)
        from = args.from
        prefManager = PrefManager(requireContext())
        adapter = GigsAdapter(this, this, this, this,this,this)
        recyclerView = binding.rvServiceGigs
        setPriceSpinner()
    }

    private fun setPriceSpinner() {
        priceArrayList = arrayListOf(
            "${Constants.CURRENCY}5+",
            "${Constants.CURRENCY}5 - ${Constants.CURRENCY}20",
            "${Constants.CURRENCY}21 - ${Constants.CURRENCY}50",
            "${Constants.CURRENCY}51 - ${Constants.CURRENCY}100",
            "${Constants.CURRENCY}101 - ${Constants.CURRENCY}500",
            "${Constants.CURRENCY}500+"
        )
        priceValueArrayList = arrayListOf("5", "5,20", "21,50", "51,100", "101,500", "500")
        val spinnerList = priceArrayList.map { price ->
            SpinnerPriceModel(
                filterValue = priceValueArrayList[priceArrayList.indexOf(price)],
                value = price
            )
        }
        priceAdapter = PriceAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item, spinnerList
        ).also {
            it.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
            binding.priceSpinner.adapter = it
        }
        binding.priceSpinner.onItemSelectedListener = this
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        when (parent!!.id) {
            binding.priceSpinner.id -> {
                val spinnerPriceModel = parent.selectedItem as SpinnerPriceModel
                when {
                    spinnerPriceModel.value.contains("+") -> {
                        filter = "price>"
                        filterValue = spinnerPriceModel.filterValue
                        if (from == 1) {
                            searchManagement()
                        }
                    }
                    else -> {
                        filter = "price"
                        filterValue = spinnerPriceModel.filterValue
                        if (from == 1) {
                            searchManagement()
                        }
                    }
                }
            }
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {

    }

    private fun setRecyclerview() {
        recyclerView.let {
            it.setHasFixedSize(true)
            it.layoutManager = LinearLayoutManager(requireContext())
            it.adapter = adapter.withLoadStateHeaderAndFooter(
                header = MyLoadStateAdapter { adapter.retry() },
                footer = MyLoadStateAdapter { adapter.retry() }
            )
        }
        setAdapterLoadState(adapter)
    }

    private fun setAdapterLoadState(adapter: GigsAdapter) {
        adapter.addLoadStateListener { loadState ->
            binding.apply {
                genericHandler.showProgressBar(loadState.source.refresh is LoadState.Loading)
                rvServiceGigs.isVisible = loadState.source.refresh is LoadState.NotLoading
                btnRetry.isVisible = loadState.source.refresh is LoadState.Error
                textViewError.isVisible = loadState.source.refresh is LoadState.Error
                // no results
                if (loadState.source.refresh is LoadState.NotLoading &&
                    loadState.append.endOfPaginationReached &&
                    adapter.itemCount < 1
                ) {
                    rvServiceGigs.isVisible = false
                    tvPlaceholder.isVisible = true
                } else {
                    tvPlaceholder.isVisible = false
                }
            }
        }
    }

    private fun setOnClickListeners() {
        binding.apply {
            ivToolbar.setOnClickListener {
                findNavController().popBackStack()
            }
            btnRetry.setOnClickListener {
                adapter.retry()
            }
            btnSearch.setOnClickListener {
                from = 1
                hideKeyboard()
                exeSearch()
            }
        }
        binding.autoCompleteTextView.onFocusChangeListener = focusChangeListener
    }

    private fun exeSearch() {
        val query = binding.autoCompleteTextView.text.toString().trim()
        val distance = binding.slider.value.toString()
        val request = SearchGigsRequest(
            query = query,
            distance = distance,
            filter = filter,
            filterValue = filterValue,
        )
        viewModel.searchGigs(request)
    }

    private fun setupViewModel() {
        viewModel = ViewModelProviders.of(
            this,
            ViewModelFactory(ApiHelper(RetrofitBuilder.apiService))
        ).get(ServiceGigsViewModel::class.java)
    }

    private fun setupObservers() {
        viewModel.sellers.observe(viewLifecycleOwner) {
            adapter.submitData(viewLifecycleOwner.lifecycle, it)
        }
        viewModel.searchSellers.observe(viewLifecycleOwner) {
            adapter.submitData(viewLifecycleOwner.lifecycle, it)
        }
    }

    private fun setupFavoritesObservers() {
        viewModel.makeFavourite.observe(this, makeFavouriteObserver)
    }

    private val makeFavouriteObserver = Observer<Resource<GeneralResponse>> {
        it?.let { resource ->
            when (resource.status) {
                Status.SUCCESS -> {
                    genericHandler.showProgressBar(false)
                    if (from == Constants.NORMAL_FLOW) {
                        viewModel.getServicesBySubCategories(args.id)
                    } else {
                        exeSearch()
                    }
                }
                Status.ERROR -> {
                    genericHandler.showProgressBar(false)
                    genericHandler.showErrorMessage(it.message.toString())
                }
                Status.LOADING -> {
                    genericHandler.showProgressBar(true)
                }
            }
        }
    }

    override fun <T> onItemClick(item: T) {
        if (item is ServiceDetail) {
            val gson = Gson()
            val serviceData = gson.toJson(item)
            val action =
                ServiceGigsFragmentDirections.actionServiceGigsFragmentToGigDetailsFragment(
                    serviceData
                )
            findNavController().navigate(action)
        }
    }

    override fun <T> addRemoveWishList(item: T) {
        if (item is ServiceDetail) {
            viewModel.addToWishlistCall(FavouriteRequest(item.id))
        }
    }

    override fun <T> contactSeller(item: T) {
        if (item is ServiceDetail) {
            try {
                if (prefManager.userId != item.service_user.id && item.service_user.id != "") {
                    val messageGig = MessageGig(
                        gigId = item.id,
                        gigImage = item.service_media[0].media,
                        gigTitle = item.s_description,
                        gigUsername = item.service_user.username
                    )
                    ServiceGigsFragmentDirections.actionServiceGigsFragmentToMessagesFragment(
                        userName = item.service_user.name,
                        photo = item.service_user.image,
                        id = item.service_user.id,
                        refersGig = true,
                        messageGig = messageGig
                    ).also {
                        findNavController().navigate(it)
                    }
                }
            } catch (e: Exception) {
                genericHandler.showErrorMessage(e.message.toString())
            }
        }
    }

    private val focusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
        if (!hasFocus) {
            hideKeyboard()
        }
    }

    override fun checkLogout() {
        var intent = Intent(activity, AuthenticationActivity::class.java)
        startActivity(intent)
    }

}