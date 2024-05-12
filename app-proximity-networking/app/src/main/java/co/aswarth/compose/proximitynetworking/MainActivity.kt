package co.aswarth.compose.proximitynetworking

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.aswarth.compose.proximitynetworking.ui.theme.ProximityNetworkingTheme
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

data class MatchData(
    val c_accept: Boolean = false,
    val c_id: String = "",
    val d_accept: Boolean = false,
    val d_id: String = "",
)

data class Client(
    val name: String,
    val url: String,
    val project_idea: String,
    val technicalExpertise: String
)

data class Developer(
    val name: String,
    val url: String,
    val experience: String,
    val technicalExpertise: String
)

val matchData = MatchData()

class MainActivity : ComponentActivity() {
    @SuppressLint("MutableCollectionMutableState")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ProximityNetworkingTheme {
                /*
                0 - client or dev
                1 - login with phone number
                2 - main screen
                 */

                val curState = remember { mutableIntStateOf(2) }

                val userType = remember { mutableStateOf("Developer") }

                val phoneNumber = remember { mutableStateOf("1231231232") }
                val password = remember { mutableStateOf("abcd") }

                val projDesc = remember { mutableStateOf("") }
                val techRequirements = remember { mutableStateOf("") }
                val matches = remember { mutableStateListOf(matchData) }


                val clients = remember { mutableStateOf(hashMapOf<String, Client>()) }
                val developers = remember { mutableStateOf(hashMapOf<String, Developer>()) }

                val database = Firebase.database



                Surface(
                    modifier = Modifier
                        .fillMaxSize(),
                    color = MaterialTheme.colorScheme.primaryContainer,
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        when (curState.intValue) {
                            0 -> {
                                ChoiceScreen(curState, userType)
                            }

                            1 -> {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Spacer(modifier = Modifier.weight(1f))
                                    // Login with phone number
                                    OutlinedTextField(
                                        value = phoneNumber.value,
                                        onValueChange = {
                                            phoneNumber.value = it
                                        },
                                        label = { Text("Phone Number") },
                                        leadingIcon = {
                                            Text("+91")
                                        },
                                        singleLine = true,
                                        trailingIcon = {
                                            if (phoneNumber.value.isNotEmpty()) {
                                                Icon(
                                                    imageVector = Icons.Filled.Clear,
                                                    contentDescription = "Next",
                                                    modifier = Modifier
                                                        .padding(8.dp)
                                                        .clickable {
                                                            phoneNumber.value = ""
                                                        }
                                                )

                                            }
                                        }

                                    )

                                    Spacer(modifier = Modifier.weight(0.08f))
                                    OutlinedTextField(
                                        value = password.value,
                                        onValueChange = {
                                            password.value = it
                                        },
                                        label = { Text("Password") },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Filled.Lock,
                                                contentDescription = "Password"
                                            )
                                        },
                                        singleLine = true,
                                        visualTransformation = PasswordVisualTransformation(),
                                        trailingIcon = {
                                            if (password.value.isNotEmpty()) {
                                                Icon(
                                                    imageVector = Icons.Filled.Clear,
                                                    contentDescription = "Next",
                                                    modifier = Modifier
                                                        .padding(8.dp)
                                                        .clickable {
                                                            password.value = ""
                                                        }
                                                )

                                            }
                                        }

                                    )
                                    Spacer(modifier = Modifier.weight(0.2f))
                                    Button(
                                        onClick = {
                                            // read password from database
                                            val userId = phoneNumber.value
                                            val myRef =
                                                database.getReference("${userType.value}/$userId/password")

                                            myRef.addValueEventListener(object :
                                                ValueEventListener {
                                                override fun onDataChange(snapshot: DataSnapshot) {
                                                    if (snapshot.value == null) {
                                                        return
                                                    }
                                                    val value = snapshot.value as String
                                                    if (value == password.value) {
                                                        curState.intValue = 2
                                                    } else {
                                                        Log.d("MainActivityy", "Incorrect password")
                                                    }
                                                }

                                                override fun onCancelled(error: DatabaseError) {
                                                    Log.d("debugsms", "error$error")
                                                }
                                            })

                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 16.dp),
                                        shape = MaterialTheme.shapes.extraLarge,
                                    ) {
                                        Text(
                                            text = "Login",
                                            modifier = Modifier.padding(2.dp),
                                            fontSize = 42.sp,
                                        )
                                    }

                                    Spacer(modifier = Modifier.weight(1f))
                                }

                            }

                            else -> {
                                // Main screen
                                MainScreen(
                                    userType,
                                    projDesc,
                                    techRequirements,
                                    phoneNumber,
                                    database,
                                    matches,
                                    clients,
                                    developers
                                )
                            }
                        }
                    }

                }
            }

        }
    }

    private fun listenToFirebase(
        database: FirebaseDatabase,
        userType: MutableState<String>,
        phoneNumber: MutableState<String>,
        projDesc: MutableState<String>,
        techRequirements: MutableState<String>,
        matches: SnapshotStateList<MatchData>,
        clients: MutableState<HashMap<String, Client>>,
        developers: MutableState<HashMap<String, Developer>>
    ) {
        val path = "${userType.value}/${phoneNumber.value}/"
        val myRef = database.getReference(path)

        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.value == null) {
                    return
                }
                val value = snapshot.value as HashMap<*, *>
                Log.d("debugsms", "value $value")
                if (userType.value == "Client") {
                    projDesc.value = value["project_idea"].toString()
                } else {
                    projDesc.value = value["experience"].toString()
                }
                techRequirements.value = value["technicalExpertise"].toString()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("debugsms", "error$error")
            }
        })

        val myRef2 = database.getReference("matches")

        myRef2.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.value == null) {
                    return
                }
                val value = snapshot.value as HashMap<*, *>
                Log.d("debugsms", "value $value")
                matches.clear()
                for (i in value) {
                    Log.d("debugsms", "i $i")
                    val map = i.value as HashMap<*, *>
                    val c_accept = map["c_accept"] as Boolean
                    val c_id = map["c_id"] as String
                    val d_accept = map["d_accept"] as Boolean
                    val d_id = map["d_id"] as String
                    matches.add(MatchData(c_accept, c_id, d_accept, d_id))
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("debugsms", "error$error")
            }
        })


        val myRef3 = database.getReference("Client")

        myRef3.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.value == null) {
                    return
                }
                val value = snapshot.value as HashMap<*, *>
                Log.d("myRef3", "value $value")
//                clients.value.clear()
                for ((phoneNumberr, clientInfoo) in value) {
                    val clientInfo = clientInfoo as HashMap<*, *>
                    val client = Client(
                        name = clientInfo["name"] as String,
                        url = clientInfo["url"] as String,
                        project_idea = clientInfo["project_idea"] as String,
                        technicalExpertise = clientInfo["technicalExpertise"] as String,
                    )
                    clients.value[phoneNumberr.toString()] = client
                }
                Log.d("myRef3", "client $clients")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("debugsms", "error$error")
            }
        })

        val myRef4 = database.getReference("Developer")

        myRef4.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.value == null) {
                    return
                }
                val value = snapshot.value as HashMap<*, *>
                Log.d("myRef4", "value $value")
                for ((phoneNumberr, devInfoo) in value) {
                    val devInfo = devInfoo as HashMap<*, *>
                    val developer = Developer(
                        name = devInfo["name"] as String,
                        url = devInfo["url"] as String,
                        experience = devInfo["experience"] as String,
                        technicalExpertise = devInfo["technicalExpertise"] as String,
                    )
                    developers.value[phoneNumberr.toString()] = developer
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("debugsms", "error$error")
            }
        })


    }

    @SuppressLint("MutableCollectionMutableState")
    @Composable
    private fun MainScreen(
        userType: MutableState<String>,
        projDesc: MutableState<String>,
        techRequirements: MutableState<String>,
        phoneNumber: MutableState<String>,
        database: FirebaseDatabase,
        matches: SnapshotStateList<MatchData>,
        clients: MutableState<HashMap<String, Client>>,
        developers: MutableState<HashMap<String, Developer>>
    ) {


        if (userType.value == "Client") {
            ClientScreen(
                database,
                userType,
                projDesc,
                techRequirements,
                phoneNumber,
                matches,
                clients,
                developers
            )
        } else {
            DeveloperScreen(
                database,
                userType,
                projDesc,
                techRequirements,
                phoneNumber,
                matches,
                clients,
                developers
            )
        }


    }

    @Composable
    private fun ClientScreen(
        database: FirebaseDatabase,
        userType: MutableState<String>,
        projDesc: MutableState<String>,
        techRequirements: MutableState<String>,
        phoneNumber: MutableState<String>,
        matches: SnapshotStateList<MatchData>,
        clients: MutableState<HashMap<String, Client>>,
        developers: MutableState<HashMap<String, Developer>>
    ) {
        listenToFirebase(
            database,
            userType,
            phoneNumber,
            projDesc,
            techRequirements,
            matches,
            clients,
            developers
        )
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (userType.value == "Client") "Welcome ${clients.value[phoneNumber.value]?.name ?: userType.value}" else "Welcome ${developers.value[phoneNumber.value]?.name ?: userType.value}",
                fontSize = 32.sp,
            )
            OutlinedTextField(
                value = projDesc.value,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .height(120.dp)
                    .padding(bottom = 8.dp),
                onValueChange = {
                    projDesc.value = it
                    val myRef =
                        Firebase.database.getReference("${userType.value}/${phoneNumber.value}")
                    myRef.child("project_idea").setValue(projDesc.value)
                },
                supportingText = { Text("Project Idea") },
                label = { Text("Describe your project idea") },
                singleLine = false,
                maxLines = 5,
            )

            Divider(color = Color.Black, thickness = 1.dp)

            OutlinedTextField(
                value = techRequirements.value,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .height(120.dp)
                    .padding(bottom = 8.dp),
                onValueChange = {
                    techRequirements.value = it
                    val myRef =
                        Firebase.database.getReference("${userType.value}/${phoneNumber.value}")
                    myRef.child("technicalExpertise").setValue(it)
                },
                supportingText = { Text("Technical Requirements") },
                label = { Text("Describe your technical requirements") },
                singleLine = false,
                maxLines = 5,
            )

            LazyColumn {
                itemsIndexed(matches) { index, match ->
                    if (match.c_id == phoneNumber.value) {
                        Column(
                            modifier = Modifier
                                .padding(8.dp)
                                .fillMaxWidth()
                                .clip(MaterialTheme.shapes.medium)
                                .background(MaterialTheme.colorScheme.inverseOnSurface)
                                .padding(8.dp),

                            ) {

                            Text(
                                text = "Experience",
                                fontSize = 20.sp,
                                modifier = Modifier.padding(8.dp)
                            )
//                            Divider(color = Color.Black, thickness = 1.dp)
                            Text(text = developers.value[match.d_id]?.experience ?: "")

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "Technical Expertise",
                                fontSize = 20.sp,
                                modifier = Modifier.padding(8.dp)
                            )
//                            Divider(color = Color.Black, thickness = 1.dp)
                            Text(text = developers.value[match.d_id]?.technicalExpertise ?: "")

                            if (match.d_accept && match.c_accept) {
                                Spacer(modifier = Modifier.height(16.dp))

                                Text(
                                    text = "Linkedin URL",
                                    fontSize = 20.sp,
                                    modifier = Modifier.padding(8.dp)
                                )
                                Text(text = developers.value[match.d_id]?.url ?: "")
                            } else {
                                Row(
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .fillMaxWidth()
                                        .padding(bottom = 8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            val myRef =
                                                Firebase.database.getReference("matches/$index")
                                            myRef.child("c_accept").setValue(true)
                                        },
                                        modifier = Modifier
                                            .padding(8.dp)
                                            .fillMaxWidth(0.55f),
                                        shape = MaterialTheme.shapes.medium
                                    ) {
                                        Text(text = "Accept", modifier = Modifier.padding(8.dp))
                                    }
                                    OutlinedButton(
                                        onClick = {
                                            val myRef =
                                                Firebase.database.getReference("matches/$index")
                                            myRef.child("c_accept").setValue(false)
                                        },
                                        modifier = Modifier.padding(8.dp)
                                            .fillMaxWidth(1f),
                                        shape = MaterialTheme.shapes.medium,
                                    ) {
                                        Text(
                                            text = "Reject",
                                            modifier = Modifier
                                                .padding(horizontal = 4.dp, vertical = 8.dp),
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Log.d("debugsms", "${clients.value}")
                }
            }
        }

    }


    @Composable
    private fun DeveloperScreen(
        database: FirebaseDatabase,
        userType: MutableState<String>,
        projDesc: MutableState<String>,
        techRequirements: MutableState<String>,
        phoneNumber: MutableState<String>,
        matches: SnapshotStateList<MatchData>,
        clients: MutableState<HashMap<String, Client>>,
        developers: MutableState<HashMap<String, Developer>>
    ) {
        listenToFirebase(
            database,
            userType,
            phoneNumber,
            projDesc,
            techRequirements,
            matches,
            clients,
            developers
        )
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (userType.value == "Developer") "Welcome ${developers.value[phoneNumber.value]?.name ?: userType.value}" else "Welcome ${clients.value[phoneNumber.value]?.name ?: userType.value}",
                fontSize = 32.sp,
            )
            OutlinedTextField(
                value = projDesc.value,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .height(120.dp)
                    .padding(bottom = 8.dp),
                onValueChange = {
                    projDesc.value = it
                    val myRef =
                        Firebase.database.getReference("${userType.value}/${phoneNumber.value}")
                    myRef.child("experience").setValue(projDesc.value)
                },
                supportingText = { Text("Experience") },
                label = { Text("Describe your experience") },
                singleLine = false,
                maxLines = 5,
            )

            Divider(color = Color.Black, thickness = 1.dp)

            OutlinedTextField(
                value = techRequirements.value,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .height(120.dp)
                    .padding(bottom = 8.dp),
                onValueChange = {
                    techRequirements.value = it
                    val myRef =
                        Firebase.database.getReference("${userType.value}/${phoneNumber.value}")
                    myRef.child("technicalExpertise").setValue(it)
                },
                supportingText = { Text("Technical Expertise") },
                label = { Text("Describe your technical expertise") },
                singleLine = false,
                maxLines = 5,
            )

            LazyColumn {
                itemsIndexed(matches) { index, match ->
                    if (match.d_id == phoneNumber.value) {
                        Column(
                            modifier = Modifier
                                .padding(8.dp)
                                .fillMaxWidth()
                                .clip(MaterialTheme.shapes.medium)
                                .background(MaterialTheme.colorScheme.inverseOnSurface)
                                .padding(8.dp),

                            ) {

                            Text(
                                text = "Project Idea",
                                fontSize = 20.sp,
                                modifier = Modifier.padding(8.dp)
                            )
                            Text(text = clients.value[match.c_id]?.project_idea ?: "")

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "Technical Requirements",
                                fontSize = 20.sp,
                                modifier = Modifier.padding(8.dp)
                            )
                            Text(text = clients.value[match.c_id]?.technicalExpertise ?: "")

                            if (match.c_accept && match.d_accept) {
                                Spacer(modifier = Modifier.height(16.dp))

                                Text(
                                    text = "Linkedin URL",
                                    fontSize = 20.sp,
                                    modifier = Modifier.padding(8.dp)
                                )
                                Text(text = clients.value[match.c_id]?.url ?: "")
                            } else {
                                Row(
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .fillMaxWidth()
                                        .padding(bottom = 8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            val myRef =
                                                Firebase.database.getReference("matches/$index")
                                            myRef.child("d_accept").setValue(true)
                                        },
                                        modifier = Modifier
                                            .padding(8.dp)
                                            .fillMaxWidth(0.55f),
                                        shape = MaterialTheme.shapes.medium
                                    ) {
                                        Text(text = "Accept", modifier = Modifier.padding(8.dp))
                                    }
                                    OutlinedButton(
                                        onClick = {
                                            val myRef =
                                                Firebase.database.getReference("matches/$index")
                                            myRef.child("d_accept").setValue(false)
                                        },
                                        modifier = Modifier.padding(8.dp)
                                            .fillMaxWidth(1f),
                                        shape = MaterialTheme.shapes.medium,
                                    ) {
                                        Text(
                                            text = "Reject",
                                            modifier = Modifier
                                                .padding(horizontal = 4.dp, vertical = 8.dp),
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Log.d("debugsms", "${developers.value}")
                }
            }
        }
    }


    @Composable
    private fun ChoiceScreen(curState: MutableState<Int>, userType: MutableState<String>) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Proximity Networking",
                modifier = Modifier.padding(16.dp),
                fontSize = 60.sp,
                lineHeight = 72.sp,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.weight(5f))
            Text(
                text = "I am a",
                fontSize = 32.sp,
            )
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                shape = MaterialTheme.shapes.extraLarge,
                onClick = {
                    userType.value = "Client"
                    curState.value = 1
                }) {
                Text(
                    text = "Client",
                    modifier = Modifier.padding(2.dp),
                    fontSize = 64.sp,
                )
            }
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                shape = MaterialTheme.shapes.extraLarge,
                onClick = {
                    userType.value = "Developer"
                    curState.value = 1
                }) {
                Text(
                    text = "Developer",
                    modifier = Modifier.padding(2.dp),
                    fontSize = 64.sp,
                )
            }
            Spacer(modifier = Modifier.weight(1f))
        }


    }
}

