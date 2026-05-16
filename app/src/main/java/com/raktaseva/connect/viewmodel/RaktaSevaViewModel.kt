package com.raktaseva.connect.viewmodel

import androidx.lifecycle.ViewModel
import com.raktaseva.connect.data.model.BloodRequest
import com.raktaseva.connect.data.repository.RaktaSevaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class RaktaSevaViewModel : ViewModel() {
    private val repository = RaktaSevaRepository()

    val profile = repository.profile
    val requests = repository.requests

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn = _isLoggedIn.asStateFlow()

    fun login() {
        _isLoggedIn.value = true
    }

    fun logout() {
        _isLoggedIn.value = false
    }

    fun toggleAvailability() = repository.toggleAvailability()

    fun nearbyDonors(bloodGroup: String, distanceKm: Float) = repository.nearbyDonors(bloodGroup, distanceKm)

    fun broadcastRequest(request: BloodRequest) = repository.createRequest(request)
}
