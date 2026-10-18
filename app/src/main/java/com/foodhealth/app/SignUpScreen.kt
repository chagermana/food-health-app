package com.foodhealth.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth

//sign up screen : creates a new account in firebase authentication
//onSignUpSuccess and onBackToLogin are callbacks.
//MainActivity decides what happens next(the NavHost does the navigating)
@Composable
fun SignUpScreen(onSignUpSuccess: () -> Unit, onBackToLogin: () -> Unit){
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember {mutableStateOf(false) } //show/hide toggle
    var confirm by remember { mutableStateOf("") } //second password box
    var message by remember { mutableStateOf("") } //error text shown to the user

    //gives us access to firebase authentication
    val auth = FirebaseAuth.getInstance()

    //stacks everything vertically, centred on the screen
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Create account", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp)) // empty gap between items

        //email box
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        //password box(shown as dots)
        OutlinedTextField(
            value = password,
            onValueChange = { password = it},
            label = { Text("Password ( 6+ characters)")},
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
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
        Spacer(Modifier.height(8.dp))
        //confirm password box: the user types the password again to avoid typos
        OutlinedTextField(
            value = confirm,
            onValueChange = { confirm = it},
            label = { Text("Confirm password") },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
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
        Button(
            onClick = {
                //check the inout before calling firebase in thsi order
                if (email.isBlank()) {
                    message = "Please enter your email"
                } else if (password.length < 6) {
                    //firebase rejects passwords shorter than 6 characters
                    message = "Password must be at least 6 characters"
                } else if (password != confirm) {
                    message = "Passwords do not match"
                } else {
                    //all checks passed: ask firebase to create the account
                    //it works in the background, so we give it 2 listeners
                    auth.createUserWithEmailAndPassword(email.trim(), password)
                        .addOnSuccessListener { onSignUpSuccess() } //got to home
                        .addOnFailureListener { message = "Sign up failed: ${it.message}" }
                }
            },
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
        ) {
            Text("Create account")
        }
        //only shows the error text when there is something to show
        if (message.isNotEmpty()){
            Text(message, color = MaterialTheme.colorScheme.error)
        }
        // line back to the login
        TextButton(onClick = onBackToLogin) {
            Text("Already have an account? Log in")
        }
       // <-- and this line
    }

}

