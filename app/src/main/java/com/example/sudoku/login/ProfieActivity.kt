package com.example.sudoku.login

import android.os.AsyncTask.Status
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.sudoku.R
import com.facebook.AccessToken
import com.facebook.GraphRequest
import com.facebook.HttpMethod
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.signin.*
import kotlinx.android.synthetic.main.activity_profie.*
import org.json.JSONException


class ProfieActivity : AppCompatActivity() {
    lateinit var mGoogleSignInClient: GoogleSignInClient
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profie)
        //Facebook
        result()
        //Google
        val task: GoogleSignInAccount? = GoogleSignIn.getLastSignedInAccount(this)
        if (task != null) {
            var pesonName: String? = task.displayName;
            var personEmail: String? = task.email;
            var personID: String = task.id.toString();

            tvName.setText(pesonName)
            tvEmail.setText(personEmail)
            tvID.setText(personID)

        }
        //Logout
        val btnLogout = findViewById<Button>(R.id.btnSignOut)
        btnLogout.setOnClickListener(View.OnClickListener {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()

            val googleSignInClient = GoogleSignIn.getClient(this, gso)
            googleSignInClient.signOut()
            // Logout
            if (AccessToken.getCurrentAccessToken() != null) {
                GraphRequest(
                    AccessToken.getCurrentAccessToken(),
                    "/me/permissions/",
                    null,
                    HttpMethod.DELETE,
                    GraphRequest.Callback {
                        AccessToken.setCurrentAccessToken(null)
                        LoginManager.getInstance().logOut()
                        finish()
                    }
                ).executeAsync()
            }
        })

    }

    private fun result() {
        val request = GraphRequest.newMeRequest(
            AccessToken.getCurrentAccessToken()
        ) { `object`, response ->

            try {
                var email: String = `object`.getString("email")
                var name: String = `object`.getString("name")
                var gender: String = `object`.getString("id")
                //var imageview: String =`object`.getString("profile_pic")

                tvName.setText(name)
                tvEmail.setText(email)
                tvID.setText(gender)
                //imageView.setImageURI(imageview)
            } catch (e: JSONException) {
                e.printStackTrace()
            }

        }
        var bundle = Bundle()
        bundle.putString("fields", "email,name,id");
        request.setParameters(bundle)
        request.executeAsync()
    }

}
