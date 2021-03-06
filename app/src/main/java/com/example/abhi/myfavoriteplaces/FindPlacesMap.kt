package com.example.abhi.myfavoriteplaces

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.support.annotation.IdRes
import android.support.annotation.RequiresApi
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.*
import com.google.android.gms.location.places.Places
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.io.IOException

/**
 * Map activity allows user to save locations searched for - use of Google Maps Api, Places SDK
 */
class FindPlacesMap : AppCompatActivity(), OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener{

    //firebase
    private lateinit var userId : String
    private lateinit var mDatabaseReference: DatabaseReference

    //auth
    var fbAuth = FirebaseAuth.getInstance()

    //saved marker list
    private lateinit var markerList: MutableList<Place>

    private lateinit var mMap: GoogleMap
    private lateinit var searchIcon : ImageView
    private lateinit var saveLocationButton : Button

    //Fab buttons
    private lateinit var logoutButton: FloatingActionButton
    private lateinit var profileButton: FloatingActionButton

    private lateinit var savedLastLocation : Location
    private lateinit var googleApiClient: GoogleApiClient
    private var latLngBounds = LatLngBounds(LatLng(-40.0, -168.0), LatLng(71.0, 136.0))

    //autofill
    private lateinit var searchText: AutoCompleteTextView
    private lateinit var saveButton: Button
    private lateinit var placeAutocompleteAdapter : PlaceAutocompleteAdapter

    //location
    private var latitude: Double=0.toDouble()
    private var longitude: Double=0.toDouble()
    private var mMarker: Marker?= null

    //Location
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    lateinit var locationRequest: LocationRequest
    lateinit var locationCallback: LocationCallback

    companion object {
        private const val MY_PERMISSION_CODE: Int = 1000
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_find_places_map)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        userId = intent.getStringExtra("USERID")
        mDatabaseReference = FirebaseDatabase.getInstance().getReference("Users")
        markerList = mutableListOf()

        searchText = this.bind(R.id.input_search)
        saveButton = this.bind(R.id.saveMarkertBtn)
        searchIcon = this.bind(R.id.ic_magnify)
        saveLocationButton = this.bind(R.id.saveMarkertBtn)
        logoutButton = this.bind(R.id.logout_btn)
        profileButton = this.bind(R.id.profile_btn)

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkLocationPermission()) {
                buildLocationRequest()
                buildLocationCallBack()

                fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
                fusedLocationProviderClient
                        .requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper())
            }
        } else {
            buildLocationRequest()
            buildLocationCallBack()

            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
            fusedLocationProviderClient
                    .requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper())
        }
        retrievePlaces()

        showProfile()

        logout()
    }

    /**
     * Log out of fb auth
     */
    private fun logout() {
        logoutButton.setOnClickListener {view ->
            fbAuth.signOut()

            val intent = Intent(this, LoginSignUp:: class.java)
            startActivity(intent) //go back to login
            finish()
        }
    }

    /**
     * Go to user profile
     */
    private fun showProfile() {
        profileButton.setOnClickListener {
            val intent = Intent(this, Profile::class.java)
            Log.e("EMAIL", fbAuth.currentUser!!.email)
            intent.putExtra("EMAIL", fbAuth.currentUser!!.email)
            startActivity(intent)
        }
    }

    /**
     * Find and mark user's current location
     */
    private fun buildLocationCallBack() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult?) {
                savedLastLocation = p0!!.locations.get(p0!!.locations.size-1)

                if (mMarker != null) {
                    mMarker!!.remove()
                }
                latitude = savedLastLocation.latitude
                longitude = savedLastLocation.longitude

                val latLng = LatLng(latitude, longitude)
                val markerOptions = MarkerOptions().position(latLng)
                        .title("Current Location").icon(BitmapDescriptorFactory.defaultMarker())

                mMarker = mMap!!.addMarker(markerOptions)

                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))
                mMap!!.animateCamera(CameraUpdateFactory.zoomTo(16f))
            }
        }
    }

    private fun buildLocationRequest() {
        locationRequest = LocationRequest()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 5000
        locationRequest.fastestInterval = 3000
        locationRequest.smallestDisplacement = 10f
    }

    private fun checkLocationPermission(): Boolean{
        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(this, arrayOf(
                        android.Manifest.permission.ACCESS_FINE_LOCATION
                ) ,MY_PERMISSION_CODE)
            }
            else {
                ActivityCompat.requestPermissions(this, arrayOf(
                        android.Manifest.permission.ACCESS_FINE_LOCATION
                ) ,MY_PERMISSION_CODE)
            }
            return false
        } else {
            return true
        }
    }

    /**
     * Request permission to use location services
     */
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            MY_PERMISSION_CODE -> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission
                            (this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                        if (checkLocationPermission()) {
                            buildLocationRequest()
                            buildLocationCallBack()

                            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
                            fusedLocationProviderClient
                                    .requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper())

                            mMap!!.isMyLocationEnabled = true
                        }
                    }
                } else {
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onStop() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        super.onStop()
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission
                    (this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mMap!!.isMyLocationEnabled = true
            }
        } else {
            mMap!!.isMyLocationEnabled = true
        }

        mMap.uiSettings.isZoomControlsEnabled = true

        searchLocation()
    }

    /**
     * User types a places and app autofills suggestions as the user types
     */
    private fun searchLocation() {
        googleApiClient = GoogleApiClient.Builder(this).addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API).enableAutoManage(this, this).build()

        placeAutocompleteAdapter = PlaceAutocompleteAdapter(this, googleApiClient,
                latLngBounds, null) //use of google places sdk class to autofill search items

        searchText.setAdapter(placeAutocompleteAdapter)

        searchIcon.setOnClickListener {
            geoLocate() //find and go to location
            saveButton.visibility = View.VISIBLE //have the ability to save searched locations
            searchText.text.clear()
        }
    }

    /**
     * Search for user specified location
     */
    private fun geoLocate() {
        val searchEntry = searchText.text.toString()
        var addressList: MutableList<Address> = mutableListOf()
        val options = MarkerOptions()

        if (searchEntry != "") {
            val geocoder = Geocoder(this)
            try { //max 5 items will show on search
                addressList = geocoder.getFromLocationName(searchEntry, 5)
            } catch (e: IOException) {
                e.printStackTrace()
            }

            for (i in addressList!!.indices) { //add marker to location
                val address = addressList[i]
                val latLng = LatLng(address.latitude, address.longitude)
                options.position(latLng)
                options.title(address.featureName)
                mMap!!.addMarker(options)
                mMap!!.animateCamera(CameraUpdateFactory.newLatLng(latLng))

                saveLocation(address, latLng)
            }
        }
    }

    /**
     * Saves location, on click of add to favorites button, to firebase
     */
    private fun saveLocation(address: Address, latLng: LatLng) {
        saveLocationButton.setOnClickListener {
            Log.e("thisuserid", "" + userId)
            val placeId = mDatabaseReference.child(userId).child("places").push().key
            val newPlace = Place(address.featureName, latLng.latitude, latLng.longitude)
            mDatabaseReference.child(userId).child("places").child(placeId).setValue(newPlace).addOnCompleteListener {
                Toast.makeText(this, "Saved", Toast.LENGTH_LONG).show()
            }
        }
    }

    /**
     * Retrieve saved places and add markers to those saved places
     */
    private fun retrievePlaces() {
        mDatabaseReference.child(userId).child("places").addValueEventListener(object: ValueEventListener {
            override fun onCancelled(p0: DatabaseError?) {
               //empty
            }

            override fun onDataChange(p0: DataSnapshot?) {
               if (p0!!.exists()) {
                   markerList.clear()

                   for(i in p0.children) {
                       val place : Place? = i.getValue(Place::class.java)
                       markerList.add(place!!)
                   }

                   for (i in markerList) {
                       val options = MarkerOptions()
                       val latLng = LatLng(i.latitude, i.longitude)
                       options.position(latLng)

                       options.title(i.locationName)

                       mMap!!.addMarker(options)
                       mMap!!.animateCamera(CameraUpdateFactory.newLatLng(latLng))
                   }
               }
            }

        })
    }

    /**
     * Extension function, method calls findviewbyid and performs cast
     */
    fun <T : View> Activity.bind(@IdRes res : Int) : T {
        @Suppress("UNCHECKED_CAST")
        return findViewById(res) as T
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
