@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.raktaseva.connect

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.List
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.raktaseva.connect.data.model.BloodGroup
import com.raktaseva.connect.data.model.BloodRequest
import com.raktaseva.connect.data.model.Donor
import com.raktaseva.connect.data.model.Urgency
import com.raktaseva.connect.data.model.UserProfile
import com.raktaseva.connect.ui.theme.AppBackground
import com.raktaseva.connect.ui.theme.BloodRed
import com.raktaseva.connect.ui.theme.EmergencyOrange
import com.raktaseva.connect.ui.theme.RaktaSevaTheme
import com.raktaseva.connect.ui.theme.SuccessGreen
import com.raktaseva.connect.viewmodel.RaktaSevaViewModel
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    private val appViewModel: RaktaSevaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RaktaSevaTheme {
                RaktaSevaApp(appViewModel)
            }
        }
    }
}

private object Routes {
    const val Splash = "splash"
    const val Onboarding = "onboarding"
    const val Auth = "auth"
    const val Home = "home"
    const val Request = "request"
    const val Donors = "donors"
    const val Profile = "profile"
    const val Status = "status"
    const val Incoming = "incoming"
    const val MyRequests = "my_requests"
}

@Composable
fun RaktaSevaApp(appViewModel: RaktaSevaViewModel = viewModel()) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Routes.Splash) {
        composable(Routes.Splash) { SplashScreen(navController, appViewModel) }
        composable(Routes.Onboarding) { OnboardingScreen(navController) }
        composable(Routes.Auth) { LoginRegisterScreen(navController, appViewModel) }
        composable(Routes.Home) { HomeScreen(navController, appViewModel) }
        composable(Routes.Request) { EmergencyRequestScreen(navController, appViewModel) }
        composable(Routes.Donors) { NearbyDonorsScreen(navController, appViewModel) }
        composable(Routes.Profile) { DonorProfileScreen(navController, appViewModel) }
        composable(Routes.Status) { RequestStatusScreen(navController, appViewModel) }
        composable(Routes.Incoming) { IncomingRequestScreen(navController, appViewModel) }
        composable(Routes.MyRequests) { MyRequestsScreen(navController, appViewModel) }
    }
}

@Composable
private fun SplashScreen(navController: NavHostController, viewModel: RaktaSevaViewModel) {
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    LaunchedEffect(Unit) {
        delay(2500)
        navController.navigate(if (isLoggedIn) Routes.Home else Routes.Onboarding) {
            popUpTo(Routes.Splash) { inclusive = true }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BloodRed),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            BloodDropLogo(size = 112, color = Color.White)
            Spacer(Modifier.height(24.dp))
            Text("Rakta-Seva Connect", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text("Saving Lives, One Drop at a Time.", color = Color.White.copy(alpha = 0.86f), fontSize = 15.sp)
            Spacer(Modifier.height(48.dp))
            Text("Connecting donors nearby...", color = Color.White.copy(alpha = 0.76f), fontSize = 12.sp)
        }
    }
}

@Composable
internal fun OnboardingScreen(navController: NavHostController) {
    val pages = listOf(
        "Become a registered blood donor in 60 seconds.",
        "Post emergency blood requests instantly.",
        "Get matched with donors within 10 km. Your details stay private until you accept."
    )
    var page by rememberSaveable { mutableIntStateOf(0) }

    Surface(Modifier.fillMaxSize(), color = AppBackground) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = { navController.navigateToAuth() }) { Text("Skip") }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                BloodDropLogo(size = 120, color = BloodRed)
                Spacer(Modifier.height(24.dp))
                Text("Fast, private blood help", fontSize = 26.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                Spacer(Modifier.height(12.dp))
                Text(pages[page], style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
            }
            Column {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    pages.indices.forEach { index ->
                        Box(
                            modifier = Modifier
                                .padding(4.dp)
                                .size(if (index == page) 12.dp else 8.dp)
                                .clip(CircleShape)
                                .background(if (index == page) BloodRed else Color.LightGray)
                        )
                    }
                }
                Spacer(Modifier.height(18.dp))
                Button(
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    onClick = {
                        if (page == pages.lastIndex) navController.navigateToAuth() else page += 1
                    }
                ) {
                    Text(if (page == pages.lastIndex) "Get Started" else "Next")
                }
            }
        }
    }
}

@Composable
private fun LoginRegisterScreen(navController: NavHostController, viewModel: RaktaSevaViewModel) {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var phone by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var bloodGroup by rememberSaveable { mutableStateOf("B+") }
    var dob by rememberSaveable { mutableStateOf("") }
    var message by rememberSaveable { mutableStateOf("") }

    Surface(color = AppBackground, modifier = Modifier.fillMaxSize()) {
        Column(Modifier.verticalScroll(rememberScrollState())) {
            Box(
                modifier = Modifier.fillMaxWidth().background(BloodRed).padding(28.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    BloodDropLogo(size = 72, color = Color.White)
                    Spacer(Modifier.height(12.dp))
                    Text("Rakta-Seva Connect", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                }
            }
            TabRow(selectedTabIndex = tab) {
                Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("Login") })
                Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("Register") })
            }
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Phone number") },
                    prefix = { Text("+91 ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Password") },
                    visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true
                )
                if (tab == 1) {
                    OutlinedTextField(name, { name = it }, Modifier.fillMaxWidth(), label = { Text("Full name") }, singleLine = true)
                    OutlinedTextField(email, { email = it }, Modifier.fillMaxWidth(), label = { Text("Email optional") }, singleLine = true)
                    BloodGroupSelector(bloodGroup) { bloodGroup = it }
                    OutlinedTextField(dob, { dob = it }, Modifier.fillMaxWidth(), label = { Text("Date of birth") }, placeholder = { Text("DD/MM/YYYY") }, singleLine = true)
                    AssistChip(onClick = { message = "Location permission will be requested when Firebase setup is connected." }, label = { Text("Allow Location Access") })
                }
                if (message.isNotBlank()) Text(message, color = BloodRed, fontSize = 13.sp)
                Button(
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    onClick = {
                        val isValid = phone.length >= 10 && password.length >= 6 && (tab == 0 || name.isNotBlank() && dob.isNotBlank())
                        if (isValid) {
                            viewModel.login()
                            navController.navigate(Routes.Home) { popUpTo(Routes.Auth) { inclusive = true } }
                        } else {
                            message = if (password.length < 6) "Password must be at least 6 characters." else "Please complete all fields."
                        }
                    }
                ) {
                    Text(if (tab == 0) "Verify and Login" else "Register")
                }
            }
        }
    }
}

@Composable
private fun HomeScreen(navController: NavHostController, viewModel: RaktaSevaViewModel) {
    val profile by viewModel.profile.collectAsState()
    val requests by viewModel.requests.collectAsState()
    AppScaffold(navController, "RaktaSeva Connect") {
        Column(
            modifier = Modifier.padding(it).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            GreetingCard(profile)
            AvailabilityCard(profile.isAvailable) { viewModel.toggleAvailability() }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                QuickAction("Request Blood", "Broadcast now", Icons.Default.List, Modifier.weight(1f), BloodRed) { navController.navigate(Routes.Request) }
                QuickAction("Find Donors", "Within 10 km", Icons.Default.Search, Modifier.weight(1f), EmergencyOrange) { navController.navigate(Routes.Donors) }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                QuickAction("My Requests", "Track status", Icons.Default.Home, Modifier.weight(1f), SuccessGreen) { navController.navigate(Routes.MyRequests) }
                QuickAction("My Profile", "Manage donor info", Icons.Default.Person, Modifier.weight(1f), Color(0xFF1976D2)) { navController.navigate(Routes.Profile) }
            }
            SectionTitle("Active Emergency Alerts")
            requests.forEach { request ->
                RequestSummaryCard(request, onClick = { navController.navigate(Routes.Incoming) })
            }
        }
    }
}

@Composable
private fun EmergencyRequestScreen(navController: NavHostController, viewModel: RaktaSevaViewModel) {
    var patient by rememberSaveable { mutableStateOf("") }
    var group by rememberSaveable { mutableStateOf("B+") }
    var units by rememberSaveable { mutableStateOf("2") }
    var urgency by rememberSaveable { mutableStateOf(Urgency.CRITICAL) }
    var hospital by rememberSaveable { mutableStateOf("") }
    var address by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var notes by rememberSaveable { mutableStateOf("") }
    var error by rememberSaveable { mutableStateOf("") }

    AppScaffold(navController, "Emergency Request", showBottomBar = false) { padding ->
        Column(
            Modifier.padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("Emergency Blood Request", color = BloodRed, fontSize = 26.sp, fontWeight = FontWeight.Bold)
            OutlinedTextField(patient, { patient = it }, Modifier.fillMaxWidth(), label = { Text("Patient name") })
            BloodGroupSelector(group) { group = it }
            OutlinedTextField(units, { units = it.filter(Char::isDigit).take(2) }, Modifier.fillMaxWidth(), label = { Text("Units required") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                Urgency.entries.forEach { item ->
                    FilterChip(
                        selected = urgency == item,
                        onClick = { urgency = item },
                        label = { Text(item.label) }
                    )
                }
            }
            OutlinedTextField(hospital, { hospital = it }, Modifier.fillMaxWidth(), label = { Text("Hospital name") })
            OutlinedTextField(address, { address = it }, Modifier.fillMaxWidth(), label = { Text("Hospital address") })
            OutlinedTextField(phone, { phone = it }, Modifier.fillMaxWidth(), label = { Text("Requester contact number") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone))
            OutlinedTextField(notes, { notes = it }, Modifier.fillMaxWidth(), label = { Text("Additional notes optional") }, minLines = 3)
            if (error.isNotBlank()) Text(error, color = BloodRed)
            Button(
                modifier = Modifier.fillMaxWidth().height(58.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BloodRed),
                onClick = {
                    if (patient.isBlank() || hospital.isBlank() || address.isBlank() || phone.length < 10) {
                        error = "Please complete patient, hospital, address and contact details."
                    } else {
                        viewModel.broadcastRequest(
                            BloodRequest(
                                id = "REQ-${System.currentTimeMillis()}",
                                patientName = patient,
                                bloodGroup = group,
                                unitsRequired = units.toIntOrNull()?.coerceIn(1, 10) ?: 1,
                                urgency = urgency,
                                hospitalName = hospital,
                                hospitalAddress = address,
                                contactNumber = phone,
                                notes = notes
                            )
                        )
                        navController.navigate(Routes.Status)
                    }
                }
            ) {
                Text("BROADCAST REQUEST", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun NearbyDonorsScreen(navController: NavHostController, viewModel: RaktaSevaViewModel) {
    var group by rememberSaveable { mutableStateOf("B+") }
    var distance by rememberSaveable { mutableStateOf(10f) }
    var showMap by rememberSaveable { mutableStateOf(false) }
    val donors = viewModel.nearbyDonors(group, distance)

    AppScaffold(navController, "Nearby Donors") { padding ->
        Column(Modifier.padding(padding).padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            FilterCard(group, { group = it }, distance, { distance = it }, showMap, { showMap = it })
            if (showMap) DonorsMapView(donors, distance)
            SectionTitle("Available Donors Near You")
            if (donors.isEmpty()) EmptyState("No donors found nearby", "Try increasing distance or changing blood group.")
            donors.forEach { DonorCard(it, revealPhone = false) }
        }
    }
}

@Composable
private fun IncomingRequestScreen(navController: NavHostController, viewModel: RaktaSevaViewModel) {
    val request = viewModel.requests.collectAsState().value.first()
    var accepted by rememberSaveable { mutableStateOf(false) }
    AppScaffold(navController, "Incoming Request", showBottomBar = false) { padding ->
        Column(Modifier.padding(padding).verticalScroll(rememberScrollState())) {
            Box(Modifier.fillMaxWidth().background(BloodRed).padding(24.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("EMERGENCY BLOOD NEEDED", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(8.dp))
                    Text("A patient near you needs your help", color = Color.White.copy(alpha = 0.86f))
                }
            }
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                RequestSummaryCard(request)
                DetailCard("Hospital Details", listOf(request.hospitalName, request.hospitalAddress, "2.3 km away from you"))
                DetailCard("Request Details", listOf("Patient: ${request.patientName}", "Notes: ${request.notes.ifBlank { "None" }}", "Contact appears only after acceptance."))
                if (accepted) {
                    DetailCard("Thank you for accepting", listOf("Requester phone: ${request.contactNumber}", "Use Call Now or Directions to coordinate quickly."))
                    Button(
                        modifier = Modifier.fillMaxWidth().height(54.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                        onClick = {}
                    ) { Text("Call Now") }
                    OutlinedButton(
                        modifier = Modifier.fillMaxWidth().height(54.dp),
                        onClick = {}
                    ) { Text("Get Directions") }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            modifier = Modifier.weight(1f).height(58.dp),
                            onClick = { navController.popBackStack() }
                        ) { Text("DECLINE") }
                        Button(
                            modifier = Modifier.weight(1f).height(58.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                            onClick = { accepted = true }
                        ) { Text("ACCEPT") }
                    }
                }
            }
        }
    }
}

@Composable
private fun RequestStatusScreen(navController: NavHostController, viewModel: RaktaSevaViewModel) {
    val request = viewModel.requests.collectAsState().value.first()
    AppScaffold(navController, "Request Status") { padding ->
        Column(Modifier.padding(padding).padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            RequestSummaryCard(request)
            CardItem {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    StatBlock(request.notifiedDonors.toString(), "Donors Notified")
                    StatBlock(request.viewedDonors.toString(), "Viewed")
                    StatBlock(request.acceptedDonors.size.toString(), "Accepted")
                }
                Spacer(Modifier.height(14.dp))
                LinearProgressIndicator(progress = 0.65f, modifier = Modifier.fillMaxWidth(), color = SuccessGreen)
            }
            SectionTitle("Accepted Donors")
            if (request.acceptedDonors.isEmpty()) EmptyState("Waiting for donors to accept", "Your request has been sent to nearby matching donors.")
            request.acceptedDonors.forEach { DonorCard(it, revealPhone = true) }
            Button(
                modifier = Modifier.fillMaxWidth().height(54.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                onClick = { navController.navigate(Routes.Home) }
            ) { Text("Mark as Fulfilled") }
            TextButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = { navController.navigate(Routes.Home) }
            ) { Text("Cancel Request", color = BloodRed) }
        }
    }
}

@Composable
private fun MyRequestsScreen(navController: NavHostController, viewModel: RaktaSevaViewModel) {
    val requests by viewModel.requests.collectAsState()
    var tab by rememberSaveable { mutableIntStateOf(0) }
    AppScaffold(navController, "My Requests") { padding ->
        Column(Modifier.padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            TabRow(tab) {
                Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("Active") })
                Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("Past") })
            }
            if (tab == 1) EmptyState("No past requests", "Fulfilled and expired requests will appear here.")
            else requests.forEach { RequestSummaryCard(it, onClick = { navController.navigate(Routes.Status) }) }
        }
    }
}

@Composable
private fun DonorProfileScreen(navController: NavHostController, viewModel: RaktaSevaViewModel) {
    val profile by viewModel.profile.collectAsState()
    AppScaffold(navController, "My Donor Profile") { padding ->
        Column(Modifier.padding(padding).padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            CardItem {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    BloodGroupBadge(profile.bloodGroup, 72)
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(profile.name, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        Text(if (profile.isAvailable) "Available Donor" else "Cooling down", color = if (profile.isAvailable) SuccessGreen else Color.Gray)
                    }
                }
            }
            AvailabilityCard(profile.isAvailable) { viewModel.toggleAvailability() }
            CardItem {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    StatBlock(profile.totalDonations.toString(), "Total Donations")
                    StatBlock(profile.livesSaved.toString(), "Lives Saved")
                    StatBlock(profile.daysSinceDonation.toString(), "Days Since")
                }
            }
            SectionTitle("Personal Information")
            CardItem {
                InfoRow("Phone", profile.phone)
                InfoRow("Email", profile.email)
                InfoRow("Blood Group", profile.bloodGroup)
                InfoRow("Address", profile.location)
            }
            SectionTitle("Donation History")
            CardItem {
                InfoRow("12 Jan 2025", "Apollo Hospital - Verified")
                InfoRow("08 Sep 2024", "Ruby Hall Clinic - Verified")
                InfoRow("22 Apr 2024", "Sassoon Hospital - Verified")
            }
            Button(
                modifier = Modifier.fillMaxWidth().height(54.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BloodRed),
                onClick = {
                    viewModel.logout()
                    navController.navigate(Routes.Auth) { popUpTo(Routes.Home) { inclusive = true } }
                }
            ) { Text("Logout") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppScaffold(
    navController: NavHostController,
    title: String,
    showBottomBar: Boolean = true,
    content: @Composable (androidx.compose.foundation.layout.PaddingValues) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BloodRed, titleContentColor = Color.White)
            )
        },
        bottomBar = {
            if (showBottomBar) {
                val entry by navController.currentBackStackEntryAsState()
                val route = entry?.destination?.route
                NavigationBar {
                    listOf(
                        "Home" to Routes.Home,
                        "Requests" to Routes.MyRequests,
                        "Donors" to Routes.Donors,
                        "Profile" to Routes.Profile
                    ).forEach { item ->
                        val icon = when (item.second) {
                            Routes.Home -> Icons.Default.Home
                            Routes.MyRequests -> Icons.Default.List
                            Routes.Donors -> Icons.Default.Search
                            Routes.Profile -> Icons.Default.Person
                            else -> Icons.Default.Home
                        }
                        NavigationBarItem(
                            selected = route == item.second,
                            onClick = {
                                navController.navigate(item.second) {
                                    launchSingleTop = true
                                    popUpTo(Routes.Home)
                                }
                            },
                            icon = { Icon(icon, contentDescription = item.first) },
                            label = { Text(item.first) }
                        )
                    }
                }
            }
        },
        content = content
    )
}

@Composable
internal fun GreetingCard(profile: UserProfile) {
    CardItem {
        Text("Hello, ${profile.name}", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(6.dp))
        Text(profile.location, color = Color.Gray)
    }
}

@Composable
internal fun AvailabilityCard(isAvailable: Boolean, onToggle: () -> Unit) {
    CardItem {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Column(Modifier.weight(1f)) {
                Text("I'm Available to Donate", fontWeight = FontWeight.Bold)
                Text(if (isAvailable) "Visible to nearby emergency requests" else "Hidden from donor matching", color = Color.Gray, fontSize = 13.sp)
            }
            Switch(checked = isAvailable, onCheckedChange = { onToggle() })
        }
    }
}

@Composable
internal fun QuickAction(title: String, subtitle: String, icon: ImageVector, modifier: Modifier, color: Color, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = modifier.height(118.dp),
        colors = CardDefaults.cardColors(containerColor = color),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.Center) {
            Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
            Spacer(Modifier.height(8.dp))
            Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(subtitle, color = Color.White.copy(alpha = 0.84f), fontSize = 13.sp)
        }
    }
}

@Composable
internal fun RequestSummaryCard(request: BloodRequest, onClick: () -> Unit = {}) {
    val isCritical = request.urgency == Urgency.CRITICAL
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = if (isCritical) Color(0xFFFFEBEE) else Color.White
        ),
        shape = RoundedCornerShape(8.dp),
        border = if (isCritical) androidx.compose.foundation.BorderStroke(1.dp, BloodRed) else null,
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            BloodGroupBadge(request.bloodGroup, 64)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "${request.bloodGroup} Required",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (isCritical) {
                        Surface(
                            color = BloodRed,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                "HIGH PRIORITY",
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }
                Text("${request.unitsRequired} units - ${request.urgency.label}", color = BloodRed, fontWeight = FontWeight.SemiBold)
                Text(request.hospitalName, color = Color.Gray)
            }
            AssistChip(onClick = {}, label = { Text(request.status) })
        }
    }
}

@Composable
internal fun DonorCard(donor: Donor, revealPhone: Boolean) {
    CardItem {
        Row(verticalAlignment = Alignment.CenterVertically) {
            BloodGroupBadge(donor.bloodGroup, 52)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(if (revealPhone) donor.name else donor.name.maskName(), fontWeight = FontWeight.Bold)
                Text("${donor.distanceKm} km away - ${if (donor.isAvailable) "Available" else "Unavailable"}", color = if (donor.isAvailable) SuccessGreen else Color.Gray)
                Text("Last donated ${donor.lastDonationDaysAgo / 30} months ago", color = Color.Gray, fontSize = 13.sp)
                if (revealPhone) Text(donor.phone, color = BloodRed, fontWeight = FontWeight.SemiBold)
                else Text("Phone hidden until donor accepts", color = Color.Gray, fontSize = 12.sp)
            }
            OutlinedButton(onClick = {}) { Text(if (revealPhone) "Call" else "Request") }
        }
    }
}

@Composable
internal fun FilterCard(
    group: String,
    onGroupChange: (String) -> Unit,
    distance: Float,
    onDistanceChange: (Float) -> Unit,
    showMap: Boolean,
    onToggleMap: (Boolean) -> Unit
) {
    CardItem {
        BloodGroupSelector(group, onGroupChange)
        Spacer(Modifier.height(8.dp))
        Text("Distance: ${distance.toInt()} km")
        Slider(value = distance, onValueChange = onDistanceChange, valueRange = 1f..10f, steps = 8)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = !showMap, onClick = { onToggleMap(false) }, label = { Text("List View") })
            FilterChip(selected = showMap, onClick = { onToggleMap(true) }, label = { Text("Map View") })
        }
    }
}

@Composable
internal fun BloodGroupSelector(selected: String, onSelected: (String) -> Unit) {
    Column {
        Text("Blood Group", fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            BloodGroup.labels.chunked(4).first().forEach { group ->
                FilterChip(selected = selected == group, onClick = { onSelected(group) }, label = { Text(group) })
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            BloodGroup.labels.chunked(4).last().forEach { group ->
                FilterChip(selected = selected == group, onClick = { onSelected(group) }, label = { Text(group) })
            }
        }
    }
}

@Composable
internal fun DonorsMapView(donors: List<Donor>, distance: Float) {
    val bangalore = LatLng(13.0003, 77.6482)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(bangalore, 12f)
    }

    Card(
        modifier = Modifier.fillMaxWidth().height(220.dp),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState
        ) {
            donors.forEach { donor ->
                Marker(
                    state = MarkerState(position = LatLng(donor.lat, donor.lng)),
                    title = donor.name,
                    snippet = "${donor.bloodGroup} - ${donor.distanceKm} km"
                )
            }
        }
    }
}

@Composable
internal fun CardItem(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(Modifier.padding(16.dp), content = content)
    }
}

@Composable
internal fun DetailCard(title: String, lines: List<String>) {
    CardItem {
        Text(title, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        lines.forEach { Text(it, color = Color.DarkGray) }
    }
}

@Composable
internal fun SectionTitle(text: String) {
    Text(text, fontSize = 20.sp, fontWeight = FontWeight.Bold)
}

@Composable
internal fun BloodGroupBadge(group: String, size: Int) {
    Box(
        Modifier.size(size.dp).clip(CircleShape).background(BloodRed),
        contentAlignment = Alignment.Center
    ) {
        Text(group, color = Color.White, fontWeight = FontWeight.Bold, fontSize = (size / 3).sp)
    }
}

@Composable
internal fun StatBlock(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.Bold, fontSize = 22.sp, color = BloodRed)
        Text(label, fontSize = 12.sp, textAlign = TextAlign.Center, color = Color.Gray)
    }
}

@Composable
internal fun InfoRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = Color.Gray)
        Text(value, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.End)
    }
    Divider()
}

@Composable
internal fun EmptyState(title: String, subtitle: String) {
    CardItem {
        Text(title, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(6.dp))
        Text(subtitle, color = Color.Gray, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
internal fun BloodDropLogo(size: Int, color: Color) {
    Canvas(modifier = Modifier.size(size.dp)) {
        val w = this.size.width
        val h = this.size.height
        val path = Path().apply {
            moveTo(w / 2f, 0f)
            cubicTo(w * 0.12f, h * 0.34f, 0f, h * 0.55f, 0f, h * 0.72f)
            cubicTo(0f, h * 0.92f, w * 0.18f, h, w / 2f, h)
            cubicTo(w * 0.82f, h, w, h * 0.92f, w, h * 0.72f)
            cubicTo(w, h * 0.55f, w * 0.88f, h * 0.34f, w / 2f, 0f)
            close()
        }
        drawPath(path, color)
    }
}

private fun String.maskName(): String {
    val parts = trim().split(" ")
    return if (parts.size >= 2) "${parts.first()} ${parts[1].first()}." else this
}

internal fun NavHostController.navigateToAuth() {
    navigate(Routes.Auth) {
        popUpTo(Routes.Onboarding) { inclusive = true }
    }
}

@Preview(showBackground = true)
@Composable
fun SplashPreview() {
    RaktaSevaTheme {
        Box(modifier = Modifier.fillMaxSize().background(BloodRed), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                BloodDropLogo(size = 112, color = Color.White)
                Spacer(Modifier.height(24.dp))
                Text("Rakta-Seva Connect", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text("Saving Lives, One Drop at a Time.", color = Color.White.copy(alpha = 0.86f), fontSize = 15.sp)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun OnboardingScreenPreview() {
    RaktaSevaTheme {
        OnboardingScreen(rememberNavController())
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HomePreview() {
    RaktaSevaTheme {
        HomeScreen(rememberNavController(), viewModel())
    }
}
