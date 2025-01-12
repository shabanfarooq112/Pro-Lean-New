package com.horizam.pro.elean.ui.main.view.fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.glide.slider.library.SliderLayout
import com.glide.slider.library.animations.DescriptionAnimation
import com.glide.slider.library.slidertypes.BaseSliderView
import com.glide.slider.library.slidertypes.DefaultSliderView
import com.glide.slider.library.tricks.ViewPagerEx
import com.horizam.pro.elean.App
import com.horizam.pro.elean.Constants
import com.horizam.pro.elean.R
import com.horizam.pro.elean.data.api.ApiHelper
import com.horizam.pro.elean.data.api.RetrofitBuilder
import com.horizam.pro.elean.data.model.response.ServiceDetail
import com.horizam.pro.elean.data.model.response.ServiceResponse
import com.horizam.pro.elean.databinding.FragmentFeaturedGigDetailsBinding
import com.horizam.pro.elean.ui.base.ViewModelFactory
import com.horizam.pro.elean.ui.main.adapter.ReviewsAdapter
import com.horizam.pro.elean.ui.main.callbacks.GenericHandler
import com.horizam.pro.elean.ui.main.callbacks.OnItemClickListener
import com.horizam.pro.elean.ui.main.viewmodel.FeaturedGigDetailsViewModel
import com.horizam.pro.elean.utils.PrefManager
import com.horizam.pro.elean.utils.Status
import java.lang.Exception


class FeaturedGigsDetailsFragment : Fragment(), OnItemClickListener,
    BaseSliderView.OnSliderClickListener,
    ViewPagerEx.OnPageChangeListener {

    private lateinit var binding: FragmentFeaturedGigDetailsBinding
    private lateinit var viewModel: FeaturedGigDetailsViewModel
    private lateinit var adapter: ReviewsAdapter
    private lateinit var genericHandler: GenericHandler
    private lateinit var glideSliderLayout: SliderLayout
    private lateinit var requestOptions: RequestOptions
    private val args: FeaturedGigsDetailsFragmentArgs by navArgs()
    private lateinit var prefManager: PrefManager
    private lateinit var serviceDetail: ServiceDetail
    private lateinit var recyclerView: RecyclerView
    private var userId: String = ""
    val bundle = Bundle()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        genericHandler = context as GenericHandler
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFeaturedGigDetailsBinding.inflate(layoutInflater, container, false)
        setToolbarData()
        initViews()
        setupViewModel()
        setupObservers()
        setOnClickListeners()
        executeApi()
        return binding.root
    }

    private fun executeApi() {
        if (viewModel.featuredGigDetails.value?.data == null) {
            genericHandler.showProgressBar(true)
            viewModel.featuredGigDetailsCall(args.uid)
        }
    }

    private fun initViews() {
        prefManager = PrefManager(requireContext())
        adapter = ReviewsAdapter(this)
        recyclerView = binding.rvReviews
        requestOptions = RequestOptions().centerCrop()
        setSliderProperties()
    }

    private fun setSliderProperties() {
        glideSliderLayout = binding.imgSlider.apply {
            setPresetTransformer(SliderLayout.Transformer.Accordion)
            setPresetIndicator(SliderLayout.PresetIndicators.Center_Bottom)
            setCustomAnimation(DescriptionAnimation())
            setDuration(4000)
            addOnPageChangeListener(this@FeaturedGigsDetailsFragment)
            stopCyclingWhenTouch(false)
        }
    }

    private fun setOnClickListeners() {
        binding.apply {
            toolbar.ivToolbar.setOnClickListener {
                findNavController().popBackStack()
            }
            btnRetry.setOnClickListener {
                executeApi()
            }
            ivUser.setOnClickListener {
//                if (userId == "") {
//                    return@setOnClickListener
//                }
//                Intent(requireActivity(), UserAboutActivity::class.java).also {
//                    it.putExtra("id", userId)
//                    startActivity(it)
//                }
            }
            btnContactSeller.setOnClickListener {
                try {
                    if (prefManager.userId != userId && userId != "") {
                      bundle.putString("seller_username", serviceDetail.service_user.username)
                        FeaturedGigsDetailsFragmentDirections.actionFeaturedGigsDetailsFragmentToMessagesFragment(
                            userName = "",
                            photo = "",
                            id = userId
                        ).also {
                            findNavController().navigate(it)
                        }
                    }
                } catch (e: Exception) {
                    genericHandler.showErrorMessage(e.message.toString())
                }
            }
            btnPurchase.setOnClickListener {
                val serviceId: String = args.uid
                if (serviceId.isNotEmpty()) {
                    val customOrderBottomSheet = CustomOrderBottomSheet()
                    bundle.putString("service_name", serviceDetail.s_description)
                    bundle.putString("seller_name", serviceDetail.service_user.name)
                    bundle.putString("seller_username",serviceDetail.service_user.username)
                    bundle.putString("price", serviceDetail.price.toString())
                    bundle.putString(Constants.SERVICE_ID, serviceId)
                    bundle.putStringArrayList(Constants.DAYS_LIST, arrayListOf("1 day"))
                    customOrderBottomSheet.arguments = bundle
                    customOrderBottomSheet.show(
                        requireActivity().supportFragmentManager,
                        CustomOrderBottomSheet.TAG
                    )
                }
            }
        }

    }

    private fun setToolbarData() {
        binding.toolbar.ivToolbar.setImageResource(R.drawable.ic_back)
        binding.toolbar.tvToolbar.text = getString(R.string.str_featured_gigs_details)
        binding.toolbar.ivToolbar.isVisible=true
    }

    private fun setupViewModel() {
        viewModel = ViewModelProviders.of(
            this,
            ViewModelFactory(ApiHelper(RetrofitBuilder.apiService))
        ).get(FeaturedGigDetailsViewModel::class.java)
    }

    private fun setupObservers() {
        viewModel.featuredGigDetails.observe(viewLifecycleOwner, {
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

    private fun changeViewVisibility(textView: Boolean, button: Boolean, layout: Boolean) {
        binding.textViewError.isVisible = textView
        binding.btnRetry.isVisible = button
        binding.mainLayout.isVisible = layout
    }

    private fun handleResponse(response: ServiceResponse) {
        try {
            setUIData(response.service)
        } catch (e: Exception) {
            genericHandler.showErrorMessage(e.message.toString())
        }
    }

    private fun setUIData(serviceDetail: ServiceDetail) {
        binding.apply {
            tvUserName.text = serviceDetail.service_user.username
            tvCategoryPrice.text = Constants.CURRENCY.plus(" ").plus(serviceDetail.price.toString())
            tvServiceDetailTitle.text = serviceDetail.s_description
            if(prefManager.setLanguage=="0"||prefManager.setLanguage=="")
            {
                tvCategoryName.text = serviceDetail.category.title
                tvSubcategoryName.text = serviceDetail.sub_category.title
            }
            else {
                tvCategoryName.text = serviceDetail.category.fiTitle
                tvSubcategoryName.text = serviceDetail.sub_category.fiTitle

            }
            tvServiceDetailDescription.text = serviceDetail.description
            tvNoOfRevision.text = serviceDetail.revision.toString()
            ratingBar.rating = serviceDetail.service_rating.toFloat()
            noOfRating.text = "(${serviceDetail.total_reviews})"
            userId = serviceDetail.service_user.id
            if (serviceDetail.service_media.size > 0) {
                Glide.with(this@FeaturedGigsDetailsFragment)
                    .load("${Constants.BASE_URL}${serviceDetail.service_media[0].media}")
                    .placeholder(R.drawable.img_profile)
                    .error(R.drawable.img_profile)
                    .into(ivUser)
                setImageSlider(serviceDetail)
            }
        }
//        if (serviceDetail.serviceReviewsList.isEmpty()) {
//            recyclerView.isVisible = false
//            binding.tvPlaceholder.isVisible = true
//        } else {
//            adapter.submitList(serviceDetail.serviceReviewsList)
//            recyclerView.isVisible = true
//            binding.tvPlaceholder.isVisible = false
//        }
    }

    private fun setImageSlider(service: ServiceDetail) {
        service.service_media.let { imagesList ->
            if (imagesList.isNotEmpty()) {
                imagesList.forEach { image ->
                    val defaultSliderView = DefaultSliderView(requireContext())
                    defaultSliderView
                        .image(Constants.BASE_URL.plus(image.media))
                        .setRequestOption(requestOptions)
                        .setProgressBarVisible(true)
                        .setOnSliderClickListener(this)
                    glideSliderLayout.addSlider(defaultSliderView)
                }
            }
        }
    }

    override fun onSliderClick(slider: BaseSliderView?) {
        // Toast.makeText(requireContext(), slider!!.getBundle().getString("extra") + "", Toast.LENGTH_SHORT).show()
    }

    override fun onStop() {
        glideSliderLayout.stopAutoCycle()
        super.onStop()
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

    }

    override fun onPageSelected(position: Int) {

    }

    override fun onPageScrollStateChanged(state: Int) {

    }

    override fun <T> onItemClick(item: T) {

    }
}