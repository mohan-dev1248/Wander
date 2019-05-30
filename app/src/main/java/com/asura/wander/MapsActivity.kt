package com.asura.wander

import android.Manifest
import android.content.pm.PackageManager
import android.content.res.Resources
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.*

import com.google.android.gms.maps.model.*
import java.util.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private val REQUEST_LOCATION_PERMISSION: Int = 0
    private lateinit var mMap: GoogleMap
    private val tag = "MapsActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = SupportMapFragment.newInstance()
        supportFragmentManager.beginTransaction().add(R.id.map_container,mapFragment).commit()
        mapFragment.getMapAsync(this)
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

        try{
            val success = mMap.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    this, R.raw.map_style
                )
            )
            if(!success){
                Log.e(tag,"Styling Map Failed")
            }
        }catch (e : Resources.NotFoundException){
            Log.e(tag,"Can't find the style")
        }
        // Add a marker in Sydney and move the camera
        val geekSkool = LatLng(12.961561, 77.644157)
        mMap.addMarker(
            MarkerOptions().
                position(geekSkool).
                title("GeekSkool").
                icon(BitmapDescriptorFactory.defaultMarker(
                    BitmapDescriptorFactory.HUE_AZURE))
        )
        val zoomLevel = 15F
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(geekSkool, zoomLevel))

//        val groundOverlay:GroundOverlayOptions = GroundOverlayOptions().
//            image(BitmapDescriptorFactory.fromResource(R.drawable.android)).
//            position(geekSkool,100F)
//        mMap.addGroundOverlay(groundOverlay)

        setMapLongClick(mMap)
        setPoiClick(mMap)
        enableMyLocation()
        setInfoWindowClickToParanoma()
    }

    private fun setMapLongClick(map: GoogleMap){
        map.setOnMapLongClickListener {
            val snippet = String.format(
                Locale.getDefault(),
                "Lat: %1$.5f, Long: %2$.5f",
                it.latitude,
                it.longitude
            )
            map.addMarker(MarkerOptions()
                .position(it)
                .title(getString(R.string.dropped_pin))
                .snippet(snippet))
        }
    }

    private fun setPoiClick(map: GoogleMap){
        map.setOnPoiClickListener {
            val snippet = String.format(
                Locale.getDefault(),
                "Lat: %1$.5f, Long: %2$.5f",
                it.latLng.latitude,
                it.latLng.longitude
            )
            val poiMarker = map.addMarker(
                MarkerOptions()
                    .title(it.name)
                    .position(it.latLng)
                    .snippet(snippet)
            )
            poiMarker.tag = "poi"
            poiMarker.showInfoWindow()
        }
    }

    private fun enableMyLocation(){
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            mMap.isMyLocationEnabled = true
        } else {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION)
        }
    }

    private fun setInfoWindowClickToParanoma(){
        mMap.setOnInfoWindowClickListener {
            if(it.tag == "poi"){
                val  options = StreetViewPanoramaOptions().position(it.position)
                val fragment = SupportStreetViewPanoramaFragment.newInstance(options)
                supportFragmentManager.beginTransaction().
                    replace(R.id.map_container,fragment).addToBackStack(null).commit()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.map_options,menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            R.id.normalMap -> mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
            R.id.hybridMap -> mMap.mapType = GoogleMap.MAP_TYPE_HYBRID
            R.id.satelliteMap -> mMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
            R.id.terrainMap -> mMap.mapType = GoogleMap.MAP_TYPE_TERRAIN
            else -> {

            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when(requestCode){
            REQUEST_LOCATION_PERMISSION -> {
                if (grantResults.isNotEmpty()
                    && grantResults[0]
                    == PackageManager.PERMISSION_GRANTED) {
                    mMap.isMyLocationEnabled = true
                }
            }
        }
    }
}
