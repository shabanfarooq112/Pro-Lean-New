package com.horizam.pro.elean.ui.main.viewmodel

import androidx.lifecycle.*
import com.horizam.pro.elean.Constants
import com.horizam.pro.elean.data.model.requests.CreateServiceRequest
import com.horizam.pro.elean.data.model.requests.LoginRequest
import com.horizam.pro.elean.data.model.requests.UpdateProfileRequest
import com.horizam.pro.elean.data.model.response.GeneralResponse
import com.horizam.pro.elean.data.repository.MainRepository
import com.horizam.pro.elean.utils.BaseUtils
import com.horizam.pro.elean.utils.Resource
import kotlinx.coroutines.Dispatchers


class NotificationsViewModel(private val mainRepository: MainRepository) : ViewModel() {

    private val notificationsRequest = MutableLiveData(Constants.DEFAULT_MUTABLE_LIVEDATA_VALUE)
    private val notificationsRequestRead = MutableLiveData(String)

    val notifications = notificationsRequest.switchMap {
        liveData(Dispatchers.IO) {
            emit(Resource.loading(data = null))
            try {
                emit(Resource.success(data = mainRepository.getNotifications()))
            } catch (exception: Exception) {
                val errorMessage = BaseUtils.getError(exception)
                emit(Resource.error(data = null, message = errorMessage))
            }
        }
    }
    val notificationsRead = notificationsRequestRead.switchMap {
        liveData(Dispatchers.IO) {
            emit(Resource.loading(data = null))
            try {
                emit(Resource.success(data = mainRepository.getNotificationsRead()))
            } catch (exception: Exception) {
                val errorMessage = BaseUtils.getError(exception)
                emit(Resource.error(data = null, message = errorMessage))
            }
        }
    }

    fun getNotificationsCall(){
        notificationsRequest.value = Constants.DEFAULT_MUTABLE_LIVEDATA_VALUE
    }
    fun getNotificationsReadCall(){
        notificationsRequestRead.value = String
    }

}