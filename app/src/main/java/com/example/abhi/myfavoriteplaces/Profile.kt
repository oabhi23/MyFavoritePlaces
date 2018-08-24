package com.example.abhi.myfavoriteplaces

import android.app.Activity
import android.os.Bundle
import android.support.annotation.IdRes
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.TextView

/**
 * User profile activity
 */

class Profile : AppCompatActivity() {

    private lateinit var emailUser: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val email = intent.getStringExtra("EMAIL")

        emailUser = this.bind(R.id.email_user)
        emailUser.text = email
    }

    /**
     * Extension function, method calls findviewbyid and performs cast
     */
    fun <T : View> Activity.bind(@IdRes res : Int) : T {
        @Suppress("UNCHECKED_CAST")
        return findViewById(res) as T
    }

}