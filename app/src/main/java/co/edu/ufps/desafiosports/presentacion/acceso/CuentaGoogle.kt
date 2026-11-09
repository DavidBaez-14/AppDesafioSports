package co.edu.ufps.desafiosports.presentacion.acceso

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import co.edu.ufps.desafiosports.R
import co.edu.ufps.desafiosports.domain.model.Nonce
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException

/**
 * Abre el selector de cuentas de Google con Credential Manager y devuelve el ID token (MOV-0006).
 *
 * Usa GetSignInWithGoogleOption: es el flujo de un botón explícito, que siempre deja elegir cuenta.
 * [context] debe ser el de la Activity (el selector se dibuja sobre ella).
 */
suspend fun pedirCuentaGoogle(context: Context, webClientId: String): ResultadoGoogle {
    val nonce = Nonce.nuevo()
    val opcion = GetSignInWithGoogleOption.Builder(webClientId)
        .setNonce(Nonce.sha256(nonce))
        .build()
    val pedido = GetCredentialRequest.Builder().addCredentialOption(opcion).build()

    return try {
        val credencial = CredentialManager.create(context).getCredential(context, pedido).credential
        if (credencial is CustomCredential && credencial.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            ResultadoGoogle.Token(GoogleIdTokenCredential.createFrom(credencial.data).idToken, nonce)
        } else {
            ResultadoGoogle.Fallo(context.getString(R.string.google_error))
        }
    } catch (e: GetCredentialCancellationException) {
        ResultadoGoogle.Cancelado
    } catch (e: NoCredentialException) {
        ResultadoGoogle.Fallo(context.getString(R.string.google_sin_cuenta))
    } catch (e: GetCredentialException) {
        ResultadoGoogle.Fallo(context.getString(R.string.google_error))
    } catch (e: GoogleIdTokenParsingException) {
        ResultadoGoogle.Fallo(context.getString(R.string.google_error))
    }
}
