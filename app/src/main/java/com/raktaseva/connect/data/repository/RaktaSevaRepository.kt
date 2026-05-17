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
        Donor("1", "Siddu", "+91 9743255267", "AB+", 1.2, true, 126, 13.0103, 77.6582),
        Donor("2", "Manjula", "+91 9743255267", "B+", 2.5, true, 180, 12.9903, 77.6382),
        Donor("3", "Vivekananda", "+91 8431601791", "A-", 3.8, true, 115, 13.0203, 77.6682),
        Donor("4", "Basava", "+91 9036728361", "A+", 5.2, true, 145, 12.9803, 77.6282),
        Donor("5", "Surya", "+91 8127978033", "O+", 4.5, true, 220, 13.0303, 77.6782),
        Donor("6", "Raghu", "+91 9888766554", "O-", 6.1, true, 98, 12.9703, 77.6182),
        Donor("7", "Pavan", "+91 9777655443", "B-", 2.0, true, 95, 13.0053, 77.6532),
        Donor("8", "Venu", "+91 9666544332", "AB-", 3.5, true, 120, 12.9953, 77.6432)
    )

    private val accepted = donors.take(2)

    private val _profile = MutableStateFlow(
        UserProfile(
            name = "Sujay H S",
            phone = "+91 8296033433",
            email = "sujayhs.22is@saividya.ac.in",
            bloodGroup = "O+",
            location = "Rajajinagar, Bangalore",
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
                patientName = "Sujay's Family Member",
                bloodGroup = "B+",
                unitsRequired = 2,
                urgency = Urgency.CRITICAL,
                hospitalName = "Manipal Hospital",
                hospitalAddress = "Rajajinagar, Bangalore",
                contactNumber = "+91 8296033433",
                notes = "Needed urgently for surgery",
                acceptedDonors = accepted
            )
        )
    )
    val requests: StateFlow<List<BloodRequest>> = _requests

    fun nearbyDonors(bloodGroup: String, maxDistanceKm: Float): List<Donor> {
        return donors.filter {
            it.bloodGroup == bloodGroup &&
                    it.distanceKm <= maxDistanceKm &&
                    it.isAvailable &&
                    it.lastDonationDaysAgo >= 90 // Success Criteria: Hide donors who donated in last 90 days
        }
    }

    fun toggleAvailability() {
        _profile.update { it.copy(isAvailable = !it.isAvailable) }
    }

    fun createRequest(request: BloodRequest) {
        _requests.update { listOf(request.copy(acceptedDonors = emptyList())) + it }
    }
}
