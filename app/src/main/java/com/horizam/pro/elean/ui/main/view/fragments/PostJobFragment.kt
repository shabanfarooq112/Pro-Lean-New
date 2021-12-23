package com.horizam.pro.elean.ui.main.view.fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.horizam.pro.elean.App
import com.horizam.pro.elean.Constants
import com.horizam.pro.elean.R
import com.horizam.pro.elean.data.api.ApiHelper
import com.horizam.pro.elean.data.api.RetrofitBuilder
import com.horizam.pro.elean.data.model.SpinnerModel
import com.horizam.pro.elean.data.model.requests.PostJobRequest
import com.horizam.pro.elean.data.model.response.*
import com.horizam.pro.elean.databinding.FragmentPostJobBinding
import com.horizam.pro.elean.ui.base.ViewModelFactory
import com.horizam.pro.elean.ui.main.adapter.SpinnerAdapter
import com.horizam.pro.elean.ui.main.callbacks.GenericHandler
import com.horizam.pro.elean.ui.main.viewmodel.GigDetailsViewModel
import com.horizam.pro.elean.ui.main.viewmodel.PostJobViewModel
import com.horizam.pro.elean.utils.BaseUtils.Companion.hideKeyboard
import com.horizam.pro.elean.utils.Resource
import com.horizam.pro.elean.utils.Status
import java.lang.Exception


class PostJobFragment : Fragment(), AdapterView.OnItemSelectedListener {

    private lateinit var binding: FragmentPostJobBinding
    private lateinit var viewModel: PostJobViewModel
    private lateinit var genericHandler: GenericHandler
    private lateinit var categoriesArrayList: List<SpinnerModel>
    private lateinit var subcategoriesArrayList: List<SpinnerModel>
    private lateinit var daysArrayList: List<String>
    private lateinit var categoriesAdapter: ArrayAdapter<SpinnerModel>
    private lateinit var subcategoriesAdapter: ArrayAdapter<SpinnerModel>
    private lateinit var daysAdapter: ArrayAdapter<String>
    private var categoryId: String = ""
    private var subcategoryId: String = ""
    private var deliveryTime = ""

    override fun onAttach(context: Context) {
        super.onAttach(context)
        genericHandler = context as GenericHandler
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPostJobBinding.inflate(layoutInflater, container, false)
        setToolbarData()
        initViews()
        setupViewModel()
        setupObservers()
        setClickListeners()
        return binding.root
    }

    private fun initViews() {
        categoriesArrayList = ArrayList()
        subcategoriesArrayList = ArrayList()
        daysArrayList = ArrayList()
        binding.spinnerCategory.onItemSelectedListener = this
        binding.spinnerSubCategory.onItemSelectedListener = this
        binding.spinnerDeliveryTime.onItemSelectedListener = this
    }

    private fun setClickListeners() {
        binding.apply {
            toolbar.ivToolbar.setOnClickListener {
                findNavController().popBackStack()
            }
            btnPublish.setOnClickListener {
                hideKeyboard()
                validateData()
            }
        }
    }

    private fun validateData() {
        binding.apply {
            when {
                etDescription.editableText.trim().isEmpty() -> {
                    genericHandler.showMessage(getString(R.string.str_enter_valid_description))
                    return
                }
                etDescription.editableText.trim().length < 20 -> {
                    genericHandler.showMessage(getString(R.string.str_enter_valid_description_length))
                    return
                }
                categoryId == "" -> {
                    genericHandler.showMessage(getString(R.string.str_enter_valid_category))
                    return
                }
                subcategoryId == "" -> {
                    genericHandler.showMessage(getString(R.string.str_enter_valid_subcategory))
                    return
                }
                deliveryTime.isEmpty() -> {
                    genericHandler.showMessage(getString(R.string.str_enter_valid_delivery_time))
                    return
                }
                etPrice.editableText.trim().isEmpty() -> {
                    genericHandler.showMessage(getString(R.string.str_enter_valid_price))
                    return
                }
                etPrice.text.toString().toDouble() < Constants.MINIMUM_ORDER_PRICE -> {
                    genericHandler.showMessage("Minimum ${Constants.MINIMUM_ORDER_PRICE}${Constants.CURRENCY} must be entered")
                    return
                }
                else -> {
                    executeApi()
                }
            }
        }
    }

    private fun executeApi() {
        genericHandler.showProgressBar(true)
//        val postJobRequest = PostJobRequest(
//            category_id = categoryId,
//            sub_category_id = subcategoryId,
//            description = binding.etDescription.text.toString().trim(),
//            delivery_time = deliveryTime,
//            budget = binding.etPrice.text.toString().toDouble()
//        )
//        viewModel.postJobCall(postJobRequest)
    }

    private fun setToolbarData() {
        binding.toolbar.ivToolbar.setImageResource(R.drawable.ic_back)
        binding.toolbar.tvToolbar.text = App.getAppContext()!!.getString(R.string.str_post_a_job)
    }

    private fun setupViewModel() {
        viewModel = ViewModelProviders.of(
            this,
            ViewModelFactory(ApiHelper(RetrofitBuilder.apiService))
        ).get(PostJobViewModel::class.java)
    }

    private fun setupObservers() {
        viewModel.categoriesDays.observe(viewLifecycleOwner, {
            it?.let { resource ->
                when (resource.status) {
                    Status.SUCCESS -> {
                        genericHandler.showProgressBar(false)
                        resource.data?.let { response ->
                            handleResponse(response)
                        }
                    }
                    Status.ERROR -> {
                        genericHandler.showProgressBar(false)
                        genericHandler.showMessage(it.message.toString())
                    }
                    Status.LOADING -> {
                        genericHandler.showProgressBar(true)
                    }
                }
            }
        })
        viewModel.spinnerSubcategories.observe(viewLifecycleOwner, subcategoriesObserver)
        viewModel.postJob.observe(viewLifecycleOwner, postJobObserver)
    }

    private fun <T> handleResponse(response: T) {
        try {
            when (response) {
                is CategoriesDaysResponse -> {
                    setUIData(response)
                }
                is SubcategoriesDataResponse -> {
                    setSpinnerSubcategories(response)
                }
                is PostJobResponse -> {
                    genericHandler.showMessage(response.message)
                    findNavController().popBackStack()
                    findNavController().navigate(R.id.postedJobsFragment)
                }
            }
        } catch (e: Exception) {
            genericHandler.showMessage(e.message.toString())
        }
    }

    private fun setSpinnerSubcategories(response: SubcategoriesDataResponse) {
        subcategoriesArrayList = response.subcategoriesList.map { spinnerSubcategories ->
            SpinnerModel(id = spinnerSubcategories.id, value = spinnerSubcategories.title)
        }
        subcategoriesAdapter = SpinnerAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item, subcategoriesArrayList
        ).also {
            it.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
            binding.spinnerSubCategory.adapter = it
        }
    }

    private fun setUIData(response: CategoriesDaysResponse) {
//        categoriesArrayList = response.categories.map { spinnerCategories ->
//            SpinnerModel(id = spinnerCategories.id, value = spinnerCategories.title)
//        }
//        daysArrayList = response.days
//        categoriesAdapter = SpinnerAdapter(
//            requireContext(),
//            android.R.layout.simple_spinner_item, categoriesArrayList
//        ).also {
//            it.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
//            binding.spinnerCategory.adapter = it
//        }
//        daysAdapter = ArrayAdapter(
//            requireContext(),
//            android.R.layout.simple_spinner_item, daysArrayList
//        ).also {
//            it.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
//            binding.spinnerDeliveryTime.adapter = it
//        }
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        when (parent!!.id) {
            binding.spinnerCategory.id -> {
                val spinnerModel = parent.selectedItem as SpinnerModel
                categoryId = spinnerModel.id
//                viewModel.spinnerSubcategoriesCall(spinnerModel.id)
            }
            binding.spinnerSubCategory.id -> {
                val spinnerModel = parent.selectedItem as SpinnerModel
                subcategoryId = spinnerModel.id
            }
            binding.spinnerDeliveryTime.id -> {
                deliveryTime = parent.selectedItem.toString()
            }
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {

    }

    private val subcategoriesObserver = Observer<Resource<SubcategoriesDataResponse>> {
        it?.let { resource ->
            when (resource.status) {
                Status.SUCCESS -> {
                    genericHandler.showProgressBar(false)
                    resource.data?.let { response ->
                        handleResponse(response)
                    }
                }
                Status.ERROR -> {
                    genericHandler.showProgressBar(false)
                    genericHandler.showMessage(it.message.toString())
                }
                Status.LOADING -> {
                    genericHandler.showProgressBar(true)
                }
            }
        }
    }

    private val postJobObserver = Observer<Resource<PostJobResponse>> {
        it?.let { resource ->
            when (resource.status) {
                Status.SUCCESS -> {
                    genericHandler.showProgressBar(false)
                    resource.data?.let { response ->
                        handleResponse(response)
                    }
                }
                Status.ERROR -> {
                    genericHandler.showProgressBar(false)
                    genericHandler.showMessage(it.message.toString())
                }
                Status.LOADING -> {
                    genericHandler.showProgressBar(true)
                }
            }
        }
    }

}