package com.horizam.pro.elean.ui.main.view.fragments

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.horizam.pro.elean.App
import com.horizam.pro.elean.Constants
import com.horizam.pro.elean.R
import com.horizam.pro.elean.data.api.ApiHelper
import com.horizam.pro.elean.data.api.RetrofitBuilder
import com.horizam.pro.elean.data.model.requests.UpdateProfileRequest
import com.horizam.pro.elean.data.model.response.ProfileInfo
import com.horizam.pro.elean.databinding.DialogChoosePictureBinding
import com.horizam.pro.elean.databinding.FragmentEditProfileBinding
import com.horizam.pro.elean.ui.base.ViewModelFactory
import com.horizam.pro.elean.ui.main.callbacks.GenericHandler
import com.horizam.pro.elean.ui.main.callbacks.UpdateProfileHandler
import com.horizam.pro.elean.ui.main.viewmodel.ProfileViewModel
import com.horizam.pro.elean.utils.*
import com.horizam.pro.elean.utils.BaseUtils.Companion.hideKeyboard
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.internal.notifyAll
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class EditProfileFragment : Fragment() {

    private lateinit var profileImage: String
    private lateinit var binding: FragmentEditProfileBinding
    private lateinit var viewModel: ProfileViewModel
    private lateinit var genericHandler: GenericHandler
    private lateinit var dialogChooseImage: Dialog
    private lateinit var prefManager: PrefManager
    private lateinit var updateProfileHandler: UpdateProfileHandler
    private lateinit var bindingChooseImageDialog: DialogChoosePictureBinding

    override fun onAttach(context: Context) {
        super.onAttach(context)
        genericHandler = context as GenericHandler
        updateProfileHandler = context as UpdateProfileHandler
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEditProfileBinding.inflate(layoutInflater, container, false)
        setToolbarData()
        initViews()
        setupViewModel()
        setupObservers()
        setClickListeners()
        return binding.root
    }

    private fun initViews() {
        profileImage = ""
        prefManager = PrefManager(requireContext())
        initChooseImageDialog()
    }

    private fun initChooseImageDialog() {
        dialogChooseImage = Dialog(requireContext())
        bindingChooseImageDialog = DialogChoosePictureBinding.inflate(layoutInflater)
        dialogChooseImage.setContentView(bindingChooseImageDialog.root)
    }

    private fun setClickListeners() {
        binding.apply {
            toolbar.ivToolbar.setOnClickListener {
                findNavController().popBackStack()
            }
            btnRetry.setOnClickListener {
                executeApi()
            }
            submitBtn.setOnClickListener {
                hideKeyboard()
                validateData()
            }
            cardEditPicture.setOnClickListener {
                dialogChooseImage.show()
                bindingChooseImageDialog.btnCamera.setOnClickListener {
                    dialogChooseImage.dismiss()
                    onClickRequestCameraPermission()
                }
                bindingChooseImageDialog.btnGallery.setOnClickListener {
                    dialogChooseImage.dismiss()
                    onClickRequestPermission()
                }
                bindingChooseImageDialog.btnNo.setOnClickListener { dialogChooseImage.dismiss() }
            }
        }
    }

    private fun onClickRequestCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                dispatchTakePictureIntent()
            }

            ActivityCompat.shouldShowRequestPermissionRationale(
                requireActivity(),
                Manifest.permission.CAMERA
            ) -> {
                showSnackbar(
                    requireView(),
                    getString(R.string.permission_required),
                    Snackbar.LENGTH_INDEFINITE,
                    getString(R.string.str_ok)
                ) {
                    requestCameraPermissionLauncher.launch(
                        Manifest.permission.CAMERA
                    )
                }
            }
            else -> {
                requestCameraPermissionLauncher.launch(
                    Manifest.permission.CAMERA
                )
            }
        }
    }

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            takePictureIntent.resolveActivity(requireContext().packageManager)?.also {
                // Create the File where the photo should go
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    genericHandler.showErrorMessage(ex.message.toString())
                    null
                }
                // Continue only if the File was successfully created
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        requireContext(),
                        "com.horizam.pro.elean.fileprovider",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    resultCameraLauncher.launch(photoURI)
                }
            }
        }
    }

    private fun executeApi() {
        genericHandler.showProgressBar(true)
        viewModel.profileDataCall()
    }

    private fun setToolbarData() {
        binding.toolbar.ivToolbar.setImageResource(R.drawable.ic_back)
        binding.toolbar.ivToolbar.isVisible=true
        binding.toolbar.tvToolbar.text = getString(R.string.edit_profile)
    }

    private fun setupViewModel() {
        viewModel = ViewModelProviders.of(
            this,
            ViewModelFactory(ApiHelper(RetrofitBuilder.apiService))
        ).get(ProfileViewModel::class.java)
    }

    private fun setupObservers() {
        viewModel.profileData.observe(viewLifecycleOwner) {
            it?.let { resource ->
                when (resource.status) {
                    Status.SUCCESS -> {
                        genericHandler.showProgressBar(false)
                        resource.data?.let { response ->
                            handleResponse(response, 0)
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
        }
        viewModel.updateProfile.observe(viewLifecycleOwner, updateProfileObserver)
    }

    private val updateProfileObserver = Observer<Resource<ProfileInfo>> {
        it?.let { resource ->
            when (resource.status) {
                Status.SUCCESS -> {
                    genericHandler.showProgressBar(false)
                    resource.data?.let { response ->
                        handleResponse(response, 1)
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

    private fun changeViewVisibility(textView: Boolean, button: Boolean, layout: Boolean) {
        binding.textViewError.isVisible = textView
        binding.btnRetry.isVisible = button
        binding.mainLayout.isVisible = layout
    }

    private fun <T> handleResponse(response: T, check: Int) {
        try {
            if (response is ProfileInfo) {
                setUiData(response)
                if (check == 1) {
                    genericHandler.showSuccessMessage(getString(R.string.str_profile_sucessfully_upated))
                    prefManager.userImage = response.image.toString()
                    updateProfileHandler.updateProfile()
                    this.findNavController().popBackStack()
                }
            }
        } catch (e: Exception) {
            genericHandler.showErrorMessage(e.message.toString())
        }
    }

    private fun onClickRequestPermission() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED -> {
                val imageIntent = Intent().apply {
                    type = "image/*"
                    putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false)
                    action = Intent.ACTION_GET_CONTENT
                }
                resultLauncher.launch(imageIntent)
            }

            ActivityCompat.shouldShowRequestPermissionRationale(
                requireActivity(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) -> {
                showSnackbar(
                    requireView(),
                    getString(R.string.permission_required),
                    Snackbar.LENGTH_INDEFINITE,
                    getString(R.string.str_ok)
                ) {
                    requestPermissionLauncher.launch(
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    )
                }
            }
            else -> {
                requestPermissionLauncher.launch(
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
            }
        }
    }

    private fun setUiData(profileInfo: ProfileInfo) {

        binding.apply {
            Glide.with(this@EditProfileFragment)
                .load(Constants.BASE_URL.plus(profileInfo.image))
                .error(R.drawable.img_profile)
                .placeholder(R.drawable.img_loading)
                .into(binding.ivProfile)
            etAddress==null
            etAddress.setText(profileInfo.address)
            etPhone==null
            etFullName.setText(profileInfo.name)
            etPhone.setText(profileInfo.phone)
            etDescription.setText(profileInfo.description)
            //etCarrierNumber.setText(profile.phone)
        }
    }

    fun showSnackbar(
        view: View, msg: String, length: Int, actionMessage: CharSequence?,
        action: (View) -> Unit
    ) {
        val snackbar = Snackbar.make(view, msg, length)
        if (actionMessage != null) {
            snackbar.setAction(actionMessage) {
                action(requireActivity().findViewById(android.R.id.content))
            }.show()
        } else {
            snackbar.show()
        }
    }

    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                if (result.data != null) {
                    try {
                        handlePickerResult(result.data!!)
                    } catch (e: Exception) {
                        genericHandler.showErrorMessage(e.message.toString())
                    }
                } else {
                    genericHandler.showErrorMessage(getString(R.string.str_invalid_data))
                }
            }
        }

    private var resultCameraLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { result ->
            if (result) {
                try {
                    Glide.with(this)
                        .load(profileImage)
                        .error(R.drawable.img_profile)
                        .placeholder(R.drawable.img_loading)
                        .into(binding.ivProfile)
                } catch (ex: Exception) {
                    genericHandler.showErrorMessage(ex.message.toString())
                }
            } else {
                genericHandler.showErrorMessage(getString(R.string.str_invalid_data))
            }
        }


    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String =
            SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? =
            requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            profileImage = absolutePath
        }
    }


    private fun handlePickerResult(data: Intent) {
        if (data.data != null) {
            // if single image is selected
            val imageUri: Uri = data.data!!
            addImagesToList(imageUri)
        }
    }

    private fun addImagesToList(imageUri: Uri) {
        imageUri.let {
            val uriPathHelper = URIPathHelper()
            val imagePath = uriPathHelper.getPath(requireContext(), it)
            if (!imagePath.isNullOrEmpty()) {
                profileImage = imagePath
                Glide.with(this)
                    .load(profileImage)
                    .error(R.drawable.img_profile)
                    .placeholder(R.drawable.img_loading)
                    .into(binding.ivProfile)
            } else {
                genericHandler.showErrorMessage(getString(R.string.str_valid_image))
            }
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                Log.i("Permission: ", "Granted")
                val imageIntent = Intent().apply {
                    type = "image/*"
                    putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false)
                    action = Intent.ACTION_GET_CONTENT
                }
                resultLauncher.launch(imageIntent)
            } else {
                Log.i("Permission: ", "Denied")
                genericHandler.showErrorMessage(
                    getString(R.string.permission_required)
                        .plus(getString(R.string.str_enabled))
                )
            }
        }

    private val requestCameraPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                Log.i("Permission: ", "Granted")
                dispatchTakePictureIntent()
            } else {
                Log.i("Permission: ", "Denied")
                genericHandler.showErrorMessage(
                    getString(R.string.permission_required)
                        .plus(getString(R.string.str_enabled))
                )
            }
        }

    private fun validateData() {
//        removeAllEditTextError()
//        binding.apply {
//            when {
//                etFullName.editableText.trim().isEmpty() -> {
//                    genericHandler.showErrorMessage(getString(R.string.str_enter_valid_username))
//                    return
//                }
//               !Validator.isValidPhone(etPhone.editableText.toString().trim())->{
//                   genericHandler.showErrorMessage(getString(R.string.str_enter_valid_phone))
//
//                   return
//               }
//                !Validator.isValidAddress(etAddress.editableText.toString().trim()) -> {
//                    genericHandler.showErrorMessage(getString(R.string.str_enter_valid_address))
//                    return
//              }
//                else -> {
                    createMultipartData()
                }
//        }
//    }
    private fun removeAllEditTextError() {
        binding.etFullName.error = null
        binding.etPhone.error = null
        binding.etAddress.error = null
    binding.etDescription.error=null
    }

    private fun createMultipartData() {
        lifecycleScope.launch {
            var image: MultipartBody.Part? = null
            if (profileImage.isNotEmpty()) {
                image =
                    BaseUtils.compressAndCreateImageData(
                        profileImage,
                        "image",
                        requireContext()
                    )
            }
            val map: HashMap<String, RequestBody> = HashMap()
            map["name"] =
                BaseUtils.createRequestBodyFromString(binding.etFullName.text.toString().trim())
            map["phone"] =
                BaseUtils.createRequestBodyFromString(binding.etPhone.text.toString().trim())
            map["address"] =
                BaseUtils.createRequestBodyFromString(binding.etAddress.text.toString().trim())
            map["description"]=
                BaseUtils.createRequestBodyFromString(binding.etDescription.text.toString().trim())
            exeApi(image, map)
        }
    }    private fun exeApi(
        image: MultipartBody.Part?,
        map: java.util.HashMap<String, RequestBody>
    ) {
        genericHandler.showProgressBar(true)
        viewModel.updateProfileCall(UpdateProfileRequest(partMap = map, image = image))
    }

}

