package ca.josue_lubaki.credentialsignin

import android.app.Activity
import android.app.Application
import android.util.Log
import androidx.credentials.*
import androidx.credentials.exceptions.CreateCredentialCancellationException
import androidx.credentials.exceptions.CreateCredentialException
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * created by Josue Lubaki
 * date : 2023-07-04
 * version : 1.0.0
 */

class CredentialViewModel(application: Application) : AndroidViewModel(application){
    private val credentialManager by lazy {
        CredentialManager.create(application)
    }

    private val _signInPasswordCredential = MutableStateFlow<PasswordCredential?>(null)
    val signInPasswordCredential : StateFlow<PasswordCredential?> = _signInPasswordCredential.asStateFlow()

    fun signInOrSignUp(activity: Activity, username : String, password : String){
        viewModelScope.launch {
            val signInSuccess = true
            // Run your app logic here to sign in the user

            if(signInSuccess){
                // set the credential to the flow for the UI to consume it
                _signInPasswordCredential.value = PasswordCredential(username, password)

                // save the credential to the credential manager
                saveCredential(activity, username, password)
            }
        }
    }

    fun signInWithSavedCredential(activity: Activity){
        viewModelScope.launch {
            runCatching {
                val passwordCredential = getCredential(activity) ?: return@launch

                val signInSuccess = true
                // Run your app logic here to sign in the user

                if(signInSuccess){
                    // set the credential to the flow for the UI to consume it
                    _signInPasswordCredential.value = passwordCredential
                }
            }.onFailure {
                when(it){
                    is NoCredentialException -> {
                        // The user has no saved credentials, so they need to sign in manually
                        _signInPasswordCredential.value = null
                    }
                    else -> {
                        Log.v("CredentialTest", "Credential retrieval error", it)
                    }
                }
            }
        }
    }

    private suspend fun getCredential(activity: Activity): PasswordCredential? {
        runCatching {
            // Tell the credential library that we're interested in a password credential
            val credentialRequest = GetCredentialRequest(
                credentialOptions = listOf(GetPasswordOption())
            )

            // show the user a dialog allowing them to choose a credential
            val credentialResponse = credentialManager.getCredential(
                activity = activity,
                request = credentialRequest
            )

//            return@getCredential credentialResponse.credential as? PasswordCredential
            return@getCredential _signInPasswordCredential.value
        }
        .onFailure {
            when(it){
                is GetCredentialCancellationException -> {
                    Log.e("CredentialTest", "Error getting credential", it)
                    throw it
                }
                is NoCredentialException -> {
                    Log.e("CredentialTest", "Error getting credential", it)
                    throw it
                }
                is GetCredentialException -> {
                    Log.e("CredentialTest", "Error getting credential", it)
                    throw it
                }
                else -> {
                    Log.e("CredentialTest", "Error getting credential", it)
                    throw it
                }
            }
        }

        return null
    }

    private suspend fun saveCredential(activity: Activity, username: String, password: String) {
        runCatching {
            // save the credential to the credential manager
//            credentialManager.createCredential(
//                activity = activity,
//                request = CreatePasswordRequest(
//                    id = username,
//                    password = password
//                )
//            )
        }.onFailure {
            when (it) {
                is CreateCredentialCancellationException -> {
                    Log.e("CredentialTest", "Error creating credential", it)
                    throw it
                }
                is CreateCredentialException -> {
                    Log.e("CredentialTest", "Error creating credential", it)
                    throw it
                }
                else -> {
                    Log.e("CredentialTest", "Error creating credential", it)
                    throw it
                }
            }
        }
    }

    fun signOut(){
        viewModelScope.launch {
            try {
                _signInPasswordCredential.value = null
            } catch (e: Exception) {
                Log.v("CredentialTest", "Credential deletion error", e)
            }
        }
    }
}
