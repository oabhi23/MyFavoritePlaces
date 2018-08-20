package com.example.abhi.myfavoriteplaces

import android.app.Activity
import android.nfc.Tag
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.annotation.IdRes
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class LoginSignUp : AppCompatActivity() {

    private lateinit var loginButton : Button
    private lateinit var signupButton : Button

    private var mDatabaseReference: DatabaseReference? = null
    private var mDatabase: FirebaseDatabase? = null
    private var mAuth: FirebaseAuth? = null

    private lateinit var usernameEditText: EditText
    private lateinit var passwordEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_signup)

        loginButton = bind(R.id.loginBtn)
        signupButton = bind(R.id.signupBtn)

        usernameEditText = bind(R.id.username)
        passwordEditText = bind(R.id.password)

        mDatabase = FirebaseDatabase.getInstance()
        mDatabaseReference = mDatabase!!.reference!!.child("Users")
        mAuth = FirebaseAuth.getInstance()

        loginButton.setOnClickListener {
            val userName = usernameEditText?.text.toString()
            val password = passwordEditText?.text.toString()
            login(userName, password)
        }

        signupButton.setOnClickListener {
            val userName = usernameEditText?.text.toString()
            val password = passwordEditText?.text.toString()

            signUp(userName, password)
        }
    }

    private fun login(userName: String, password: String) {
        if (!TextUtils.isEmpty(userName) && !TextUtils.isEmpty(password)) {
            mAuth!!.signInWithEmailAndPassword(userName!!, password!!)
                    .addOnCompleteListener(this) { task ->

                        if (task.isSuccessful) {
                            Toast.makeText(this@LoginSignUp, "Login successful.",
                                    Toast.LENGTH_SHORT).show()

                            usernameEditText.text.clear()
                            passwordEditText.text.clear()
                        } else {
                            Toast.makeText(this@LoginSignUp, "Incorrect username or password.",
                                    Toast.LENGTH_SHORT).show()
                        }

                    }
        } else {
            Toast.makeText(this, "Please enter a username and password",
                    Toast.LENGTH_LONG).show()
        }
    }

    private fun signUp(userName: String, password: String) {
        if (!TextUtils.isEmpty(userName) && !TextUtils.isEmpty(password)) {
            mAuth!!.createUserWithEmailAndPassword(userName!!, password!!)
                    .addOnCompleteListener(this) { task ->

                        if(task.isSuccessful) {
                            Toast.makeText(this@LoginSignUp,
                                    "Authentication successful. Please log in.",
                                    Toast.LENGTH_SHORT).show()

                            usernameEditText.text.clear()
                            passwordEditText.text.clear()
                        } else {
                            Toast.makeText(this@LoginSignUp, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show()
                        }
            }
        } else {
            Toast.makeText(this, "Please enter a username and password",
                    Toast.LENGTH_LONG).show()
        }
    }

    fun <T : View> Activity.bind(@IdRes res : Int) : T {
        @Suppress("UNCHECKED_CAST")
        return findViewById(res) as T
    }
}