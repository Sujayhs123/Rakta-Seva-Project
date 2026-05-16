package com.raktaseva.connect.data.model

enum class BloodGroup(val label: String) {
    A_POSITIVE("A+"),
    A_NEGATIVE("A-"),
    B_POSITIVE("B+"),
    B_NEGATIVE("B-"),
    AB_POSITIVE("AB+"),
    AB_NEGATIVE("AB-"),
    O_POSITIVE("O+"),
    O_NEGATIVE("O-");

    companion object {
        val labels = entries.map { it.label }
    }
}

enum class Urgency(val label: String) {
    CRITICAL("Critical"),
    URGENT("Urgent"),
    WITHIN_24_HOURS("Within 24 hrs")
}

data class Donor(
    val id: String,
    val name: String,
    val phone: String,
    val bloodGroup: String,
    val distanceKm: Double,
    val isAvailable: Boolean,
    val lastDonationDaysAgo: Int,
    val lat: Double = 18.5204,
    val lng: Double = 73.8567
)

data class BloodRequest(
    val id: String,
    val patientName: String,
    val bloodGroup: String,
    val unitsRequired: Int,
    val urgency: Urgency,
    val hospitalName: String,
    val hospitalAddress: String,
    val contactNumber: String,
    val notes: String,
    val status: String = "Active",
    val notifiedDonors: Int = 24,
    val viewedDonors: Int = 5,
    val acceptedDonors: List<Donor> = emptyList()
)

data class UserProfile(
    val name: String,
    val phone: String,
    val email: String,
    val bloodGroup: String,
    val location: String,
    val isAvailable: Boolean,
    val totalDonations: Int,
    val livesSaved: Int,
    val daysSinceDonation: Int
)
