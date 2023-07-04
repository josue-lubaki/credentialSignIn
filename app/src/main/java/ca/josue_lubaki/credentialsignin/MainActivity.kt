package ca.josue_lubaki.credentialsignin

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.credentials.PasswordCredential
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ca.josue_lubaki.credentialsignin.ui.theme.CredentialSignInTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CredentialSignInTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CredentialScreen()
                }
            }
        }
    }
}

@Composable
fun CredentialScreen(viewModel : CredentialViewModel = viewModel()) {
    val context = LocalContext.current.getActivity()
    val credential by viewModel.signInPasswordCredential.collectAsStateWithLifecycle()

    credential?.let {
        SignInScreen(
            credential = it,
            logOut = viewModel::signOut
        )
    } ?: run {
        SignInForm(
            signInOrSignUp = { username, password ->
                // perform you app's sign in or sign up logic here.
                // viewModel.signInOrSignUp(context, username, password)

                // there if successful, save the credential to the credential manager
                context?.let { viewModel.signInOrSignUp(it, username, password) }
            },
            signInWithSavedCredential = {
//                context ?: return@SignInForm
                context?.let { viewModel.signInWithSavedCredential(it) }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignInForm(
    signInOrSignUp: (username : String, password : String) -> Unit,
    signInWithSavedCredential: () -> Unit
) {
    var username by remember { mutableStateOf("josuelubaki@gmail.com") }
    var password by remember { mutableStateOf("1234") }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        TextField(
            value = username,
            onValueChange = { username = it },
            label = { Text(text = "Username") },
            singleLine = true,
            placeholder = { Text(text = "Enter your username") },
        )

        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text(text = "Password") },
            singleLine = true,
            placeholder = { Text(text = "Enter your password") },
        )

        Button(onClick = { signInOrSignUp(username, password) }) {
            Text(text = "Sign In or Sign Up")
        }

        Text(text = "-- OR --")
        Button(onClick = { signInWithSavedCredential() }) {
            Text(text = "Sign In with Saved Credential")
        }
    }
}

@Composable
fun SignInScreen(credential: PasswordCredential, logOut: () -> Unit) {
    Column {
        Text(text = "You have successfully signed in")
        Text(text = "Welcome ${credential.id}")
        Text(text = "Password: ${credential.password}")
        Button(onClick = logOut) {
            Text(text = "Sign Out")
        }
    }
}

private fun Context.getActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.getActivity()
    else -> null
}
