package com.raktaseva.connect.data.repository

import com.raktaseva.connect.data.model.BloodRequest
import com.raktaseva.connect.data.model.Donor
import com.raktaseva.connect.data.model.Urgency
import com.raktaseva.connect.data.model.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class RaktaSevaRepository {
    private val donors = listOf(
        Donor("1", "Suresh Kumar", "+91 98765 43210", "B+", 1.2, true, 126, 13.0103, 77.6582),
        Donor("2", "Ananya Reddy", "+91 98765 99990", "B+", 2.5, true, 180, 12.9903, 77.6382),
        Donor("3", "Vikram Singh", "+91 97654 32109", "O+", 3.8, true, 95, 13.0203, 77.6682),
        Donor("4", "Kavita Rao", "+91 99887 77665", "AB+", 5.2, false, 28, 12.9803, 77.6282)
    )

    private val accepted = donors.take(2)

    private val _profile = MutableStateFlow(
        UserProfile(
            name = "Tejaswini R",
            phone = "+91 98765 43210",
            email = "tejaswini@example.com",
            bloodGroup = "B+",
            location = "Banaswadi, Bangalore",
            isAvailable = true,
            totalDonations = 12,
            livesSaved = 36,
            daysSinceDonation = 120
        )
    )
    val profile: StateFlow<UserProfile> = _profile

    private val _requests = MutableStateFlow(
        listOf(
            BloodRequest(
                id = "REQ-1001",
                patientName = "Tejaswini's Family Member",
                bloodGroup = "B+",
                unitsRequired = 2,
                urgency = Urgency.CRITICAL,
                hospitalName = "Manipal Hospital",
                hospitalAddress = "Banaswadi, Bangalore",
                contactNumber = "+91 98765 43210",
                notes = "Needed urgently for surgery",
                acceptedDonors = accepted
            )
        )
    )
    val requests: StateFlow<List<BloodRequest>> = _requests

    fun nearbyDonors(bloodGroup: String, maxDistanceKm: Float): List<Donor> {
        return donors.filter { it.bloodGroup == bloodGroup && it.distanceKm <= maxDistanceKm }
    }

    fun toggleAvailability() {
        _profile.update { it.copy(isAvailable = !it.isAvailable) }
    }

    fun createRequest(request: BloodRequest) {
        _requests.update { listOf(request.copy(acceptedDonors = emptyList())) + it }
    }
}
