package com.foodhealth.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth

//Login screen : the user types an email and password and Firebase checks them
//the two lamba parameters are "callbacks": this screen does not know where to go
//next, so its calls them and MainActivity (the NavHost) does the navigating
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onGoToSignUp: () -> Unit
){
    var email by remember { mutableStateOf("")}
    var password by remember { mutableStateOf("")}
    var showPassword by remember {mutableStateOf(false) } //show/hide toggle
    var loading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") } //error text shown to the use
    var messageIsError by remember { mutableStateOf(true) }  // red for errors, green otherwise

    //gives access to firebase authentication (set up in the firebase console)
    val auth = FirebaseAuth.getInstance()

    //columns stack everything vertically,centred on the screen
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ){
        // fillMaxSize comes before verticalScroll, so the content is centred when it
        //fits and scrolls when the keyboard takes up space
        AuthLogo(tagline = "Know whats on your plate.", showName = false)
        Spacer(Modifier.height(32.dp))


        //email box: each keystroke updates the email state
        OutlinedTextField(
            value = email,
            onValueChange = { email = it},
            label = { Text ("Email") },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = authFieldColors(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))

        //password box: shows dots instead of letters
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            singleLine= true,
            shape = RoundedCornerShape(16.dp),
            colors = authFieldColors(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation =
                if (showPassword) VisualTransformation.None else PasswordVisualTransformation
                    (),
            trailingIcon = {
                TextButton(onClick = { showPassword = !showPassword }) {
                    Text(if (showPassword) "Hide" else "Show")
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
        //message under the fields, only shown when there is something to show
        if (message.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Text(
                message,
                color = if (messageIsError) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.primary
            )
        }
        Spacer(Modifier.height(20.dp))

        //main button (forest green, white text from the theme)
        Button(
            onClick = {
                //checks input first
                if (email.isBlank() || password.isBlank()){
                    message = "Please fill in both fields"
                } else {
                    //ask firebase to sign in. it works in the bkg so
                    // give it 2 listeners: one for success, one for failure
                    message = ""
                    loading = true
                    auth.signInWithEmailAndPassword(email.trim(), password)
                        .addOnSuccessListener { onLoginSuccess() } //go to home
                        .addOnFailureListener { message = "Login failed: ${it.message}" }
                }
            },
            enabled = !loading ,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ){
            Text (
                if (loading) "Please wait.." else "Log in",
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.height(8.dp))

        // link to the sign up screen
        TextButton(onClick = onGoToSignUp, modifier = Modifier.fillMaxWidth()) {
            Text(
                "No account? Sign up",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
        }

        TextButton(
            onClick = {
                if (email.isBlank()) {
                    messageIsError = true
                    message = "Type your email above first"
                } else {
                    auth.sendPasswordResetEmail(email.trim())
                        .addOnSuccessListener {
                            messageIsError = false
                            message = "Reset link sent. Check your email."
                        }
                        .addOnFailureListener {
                            messageIsError = true
                            message = "Could not send reset email: ${it.message}"
                        }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Forgot password?", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}


