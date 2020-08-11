package com.example.savemove

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.mapbox.android.core.location.LocationEngine
import com.mapbox.android.core.location.LocationEngineCallback
import com.mapbox.android.core.location.LocationEngineProvider
import com.mapbox.android.core.location.LocationEngineResult
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.api.directions.v5.DirectionsCriteria
import com.mapbox.api.directions.v5.models.DirectionsResponse
import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions
import com.mapbox.mapboxsdk.location.LocationComponentOptions
import com.mapbox.mapboxsdk.location.OnLocationClickListener
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.plugins.annotation.Symbol
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions
import com.mapbox.mapboxsdk.plugins.places.autocomplete.PlaceAutocomplete
import com.mapbox.mapboxsdk.plugins.places.autocomplete.model.PlaceOptions
import com.mapbox.mapboxsdk.style.expressions.Expression.*
import com.mapbox.mapboxsdk.style.layers.HeatmapLayer
import com.mapbox.mapboxsdk.style.layers.Property.ICON_ROTATION_ALIGNMENT_VIEWPORT
import com.mapbox.mapboxsdk.style.layers.Property.VISIBLE
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.*
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncher
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncherOptions
import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber
import java.io.InputStream
import java.lang.ref.WeakReference
import java.net.URI
import java.nio.charset.Charset

class MainActivity : AppCompatActivity(), OnMapReadyCallback, PermissionsListener,
    OnLocationClickListener, MapboxMap.OnMapClickListener {
    private lateinit var mapView: MapView
    private lateinit var mapboxMap: MapboxMap
    private var HEATMAP_LAYER_ID = "seguridad-heat"
    private var HEATMAP_SOURCE = "heatmap-data"
    private var HEATMAP_SOURCE_ID = "heatmap-source-id"
    private lateinit var symbolManager: SymbolManager
    private var symbol: Symbol? = null

    private lateinit var fabOpen: Animation
    private lateinit var fabClose: Animation
    private lateinit var fabClockWise: Animation
    private lateinit var fabAntiClockWise: Animation
    private lateinit var btnClose: Animation
    private lateinit var btnOpen: Animation

    private val SAVED_STATE_LOCATION = "saved_state_location"
    private var lastLocation: Location? = null
    private var marcadores = false

    var navigationMapRoute: NavigationMapRoute? = null
    var currentRoute: DirectionsRoute? = null
    private var originLocation: Location? = null

    private var permissionsManager: PermissionsManager = PermissionsManager(this)
    private lateinit var locationEngine: LocationEngine

    private val callback = LocationListeningCallback(this)

    private var REQUEST_CODE_AUTOCOMPLETE = 1
    private var dark = false
    private lateinit var theme: String
    private var layers = true

    private var isOpen = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token))
        setContentView(R.layout.activity_main)
                mapView = findViewById(R.id.mapView)
                mapView.onCreate(savedInstanceState)
                mapView.getMapAsync(this)
    }

    override fun onMapReady(mapboxMap: MapboxMap) {
        this.mapboxMap = mapboxMap
        locationEngine = LocationEngineProvider.getBestLocationEngine(this)
        mapboxMap.setStyle(Style.Builder().fromUri("mapbox://styles/danohealer/cjxbcwi6s4eob1cpw6lupzig3")) {
            val style: Style = mapboxMap.style!!
            enableLocationComponent(it)
            addButtons(style)
        }
    }

    private fun toggleStyle(dark: Boolean) {
        this.dark = if (dark) {
            theme = "mapbox://styles/danohealer/cjxbcwi6s4eob1cpw6lupzig3"
            false
        } else {
            theme = "mapbox://styles/danohealer/cjxrm9kn36lns1cqeups6qc3m"
            true
        }
        mapboxMap.setStyle(Style.Builder().fromUri(theme)) {
            val style: Style = mapboxMap.style!!
            enableLocationComponent(it)
            addButtons(style)
        }
    }

    private fun clearMarkers() {
        if (!symbolManager.annotations.isEmpty) {
            symbolManager.annotations.clear()
            symbolManager.delete(symbol)
            navigationMapRoute?.removeRoute()
        }
    }

    private fun addButtons(style: Style) {
        fabOpen = AnimationUtils.loadAnimation(this, R.anim.fab_open)
        fabClose = AnimationUtils.loadAnimation(this, R.anim.fab_close)
        fabClockWise = AnimationUtils.loadAnimation(this, R.anim.rotate_clockwise)
        fabAntiClockWise = AnimationUtils.loadAnimation(this, R.anim.rotate_anticlockwise)
        btnClose = AnimationUtils.loadAnimation(this, R.anim.fab_close)
        btnOpen = AnimationUtils.loadAnimation(this, R.anim.fab_open)
        style.addImage(("marker_icon"), BitmapFactory.decodeResource(resources, R.drawable.mapbox_marker_icon_default))
        symbolManager = SymbolManager(mapView, mapboxMap, style)
        symbolManager.iconAllowOverlap = true
        symbolManager.iconTranslate = arrayOf(-4f, 5f)
        symbolManager.iconRotationAlignment = ICON_ROTATION_ALIGNMENT_VIEWPORT

        floatingActionButton.setOnClickListener{
            val position = CameraPosition.Builder()
                .target(LatLng(mapboxMap.locationComponent.lastKnownLocation))
                .zoom(17.0)
                .build()
            mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(position), 300)
        }
        BtnNavigation.setOnClickListener{
            val navigationLauncherOptions = NavigationLauncherOptions.builder()
                .directionsRoute(currentRoute)
                .shouldSimulateRoute(true)
                .build()

            NavigationLauncher.startNavigation(this, navigationLauncherOptions) //4
        }
        fab_search.setOnClickListener{
            val intent: Intent = PlaceAutocomplete.IntentBuilder()
                .accessToken(Mapbox.getAccessToken()!!)
                .placeOptions(
                    PlaceOptions.builder()
                        .language("ES")
                        .proximity(Point.fromLngLat(mapboxMap.locationComponent.lastKnownLocation?.longitude!!, mapboxMap.locationComponent.lastKnownLocation?.latitude!!))
                        .backgroundColor(Color.parseColor("#EEEEEE"))
                        .limit(10)
                        .build(PlaceOptions.MODE_CARDS)
                )
                .build(this@MainActivity)
            startActivityForResult(intent, REQUEST_CODE_AUTOCOMPLETE)
        }
        BotonMas.setOnClickListener {
            toggleBtn()
            MapaDeCalor.setOnClickListener{
                if (layers) {
                    Firebase.storage.reference.child("seguridad.geojson").downloadUrl.addOnSuccessListener {
                        uri ->
                        val geoJson = URI(uri.toString())
                        style.addSource(
                        GeoJsonSource(
                            HEATMAP_SOURCE,
                            geoJson
                        )
                    )
                        addHeatmapLayer(style)
                        layers = false
                    }.addOnFailureListener{
                        exception -> Toast.makeText(this, exception.toString(), Toast.LENGTH_LONG).show()
                    }
                } else {
                    clearLayers(style)
                    layers = true
                }
            }

            Poligonos.setOnClickListener{
                clearMarkers()
                toggleBtn()
                clearLayers(style)
                layers = true
                if (BtnNavigation.isEnabled) {
                    BtnNavigation.startAnimation(btnClose)
                }
                BtnNavigation.isEnabled = false
                BtnNavigation.isFocusable = false
                toggleStyle(dark)
            }

            Rutas.setOnClickListener{
                if (!marcadores) {
                    mapboxMap.addOnMapClickListener(this)
                    Toast.makeText(this, "Toque el mapa para agregar un destino", Toast.LENGTH_SHORT).show()
                    marcadores = true
                } else {
                    mapboxMap.removeOnMapClickListener(this)
                    clearMarkers()
                    marcadores = false
                    if (BtnNavigation.isEnabled) {
                        BtnNavigation.startAnimation(btnClose)
                    }
                    BtnNavigation.isEnabled = false
                    BtnNavigation.isFocusable = false
                }
            }
        }
    }

    private fun toggleBtn() {
        if (isOpen) {
            MapaDeCalor.startAnimation(fabClose)
            TxtMapaDeCalor.startAnimation(fabClose)
            Poligonos.startAnimation(fabClose)
            TxtPoligonos.startAnimation(fabClose)
            Rutas.startAnimation(fabClose)
            TxtRuta.startAnimation(fabClose)
            BotonMas.startAnimation(fabClockWise)

            MapaDeCalor.isEnabled = false
            Poligonos.isEnabled = false
            Rutas.isEnabled = false
            isOpen = false
        } else {
            MapaDeCalor.startAnimation(fabOpen)
            TxtMapaDeCalor.startAnimation(fabOpen)
            Poligonos.startAnimation(fabOpen)
            TxtPoligonos.startAnimation(fabOpen)
            Rutas.startAnimation(fabOpen)
            TxtRuta.startAnimation(fabOpen)
            BotonMas.startAnimation(fabAntiClockWise)

            MapaDeCalor.isEnabled = true
            Poligonos.isEnabled = true
            Rutas.isEnabled = true
            isOpen = true
        }
    }

    private fun clearLayers(style: Style) {
        style.removeLayer(HEATMAP_LAYER_ID)
        style.removeSource(HEATMAP_SOURCE)
    }
    private fun addHeatmapLayer(@NonNull loadedMapStyle: Style) {
        val layer = HeatmapLayer(HEATMAP_LAYER_ID, HEATMAP_SOURCE)
        layer.maxZoom = 19F
        layer.sourceLayer = HEATMAP_SOURCE_ID
        layer.setProperties(
            visibility(VISIBLE),
            heatmapColor(
                interpolate(
                    linear(), heatmapDensity(),
                    literal(0.01), rgba(0, 0, 0, 0),
                    literal(0.1), rgba(0, 2, 114, .1),
                    literal(0.2), rgba(0, 6, 219, .15),
                    literal(0.3), rgba(0, 74, 255, .2),
                    literal(0.4), rgba(0, 202, 255, .25),
                    literal(0.5), rgba(73, 255, 154, .3),
                    literal(0.6), rgba(171, 255, 59, .35),
                    literal(0.7), rgba(255, 197, 3, .4),
                    literal(0.8), rgba(255, 82, 1, 0.6),
                    literal(0.9), rgba(196, 0, 1, 0.6),
                    literal(0.95), rgba(121, 0, 0, 0.6)
                )
            ),
            heatmapIntensity(2f),
            heatmapRadius(30f),
            heatmapOpacity(0.6f)
        )
        loadedMapStyle.addLayer(layer)
    }

    private fun getRoute(originPoint: Point, endPoint: Point) {
        val unitType = DirectionsCriteria.METRIC
        NavigationRoute.builder(this)
            .accessToken(Mapbox.getAccessToken()!!)
            .origin(originPoint)
            .destination(endPoint)
            .voiceUnits(unitType)
            .build()
            .getRoute(object : Callback<DirectionsResponse> {
                override fun onFailure(call: Call<DirectionsResponse>, t: Throwable) {
                    Timber.d(t.localizedMessage)
                }

                override fun onResponse(call: Call<DirectionsResponse>, response: Response<DirectionsResponse>
                ) {
                    if (navigationMapRoute != null) {
                        navigationMapRoute?.updateRouteVisibilityTo(false)
                    } else {
                        navigationMapRoute = NavigationMapRoute(null, mapView, mapboxMap)
                    }

                    currentRoute = response.body()?.routes()?.first()
                    if (currentRoute != null) {
                        navigationMapRoute?.addRoute(currentRoute)
                    }
                }
            })
    }

    @SuppressLint("MissingPermission")
    private fun enableLocationComponent(loadedMapStyle: Style) {
        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            val customLocationComponentOptions = LocationComponentOptions.builder(this)
                .trackingGesturesManagement(true)
                .accuracyColor(ContextCompat.getColor(this, R.color.mapbox_blue))
                .build()

            val locationComponentActivationOptions = LocationComponentActivationOptions.builder(this, loadedMapStyle)
                .locationComponentOptions(customLocationComponentOptions)
                .build()

            mapboxMap.locationComponent.activateLocationComponent(locationComponentActivationOptions)
            mapboxMap.locationComponent.isLocationComponentEnabled = true
            mapboxMap.locationComponent.addOnLocationClickListener(this)
            mapboxMap.locationComponent.cameraMode = CameraMode.TRACKING
            mapboxMap.locationComponent.renderMode = RenderMode.COMPASS
            mapboxMap.locationComponent.forceLocationUpdate(lastLocation)
        } else {
            permissionsManager = PermissionsManager(this)
            permissionsManager.requestLocationPermissions(this)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onExplanationNeeded(permissionsToExplain: MutableList<String>?) {
        Toast.makeText(this, R.string.user_location_permission_explanation, Toast.LENGTH_LONG).show()
    }

    override fun onPermissionResult(granted: Boolean) {
        if (granted) {
            enableLocationComponent(mapboxMap.style!!)
        } else {
            Toast.makeText(this, R.string.user_location_permission_not_granted, Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private class LocationListeningCallback internal constructor(activity: MainActivity) :
        LocationEngineCallback<LocationEngineResult> {
        private val activityWeakReference: WeakReference<MainActivity> = WeakReference(activity)

        override fun onSuccess(result: LocationEngineResult?) {
            val activity: MainActivity? = activityWeakReference.get()
            if (activity != null) {
                val location: Location = result?.lastLocation ?: return
                Toast.makeText(activity, result.lastLocation?.latitude.toString() + ", " + result.lastLocation?.longitude.toString(), Toast.LENGTH_SHORT).show()
                if (result.lastLocation != null) {
                    activity.mapboxMap.locationComponent.forceLocationUpdate(result.lastLocation)
                }
            }
        }

        override fun onFailure(exception: Exception) {
            val activity: MainActivity? = activityWeakReference.get()
            if (activity != null) {
                Toast.makeText(
                    activity, exception.localizedMessage,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onLocationComponentClick() {
        Toast.makeText(this, getString(R.string.clicked_on_location_component), Toast.LENGTH_SHORT)
            .show()
    }

    override fun onMapClick(point: LatLng):Boolean {
        clearMarkers()
        BtnNavigation.startAnimation(btnOpen)
        BtnNavigation.isEnabled = true
        BtnNavigation.isFocusable = true
        originLocation.run {
            val location = mapboxMap.locationComponent.lastKnownLocation
            val startPoint = Point.fromLngLat(location?.longitude!!, location.latitude)
            val endPoint = Point.fromLngLat(point.longitude, point.latitude)
            getRoute(startPoint, endPoint)
        }
        addMarker(point)
        return true
    }

    private fun addMarker(point: LatLng) {
        symbol = symbolManager.create(SymbolOptions()
            .withIconImage("marker_icon")
            .withLatLng(point)
        )
        symbolManager.addLongClickListener { symbol ->
            symbolManager.delete(symbol)
            navigationMapRoute?.removeRoute()
            BtnNavigation.startAnimation(btnClose)
            BtnNavigation.isEnabled = false
            BtnNavigation.isFocusable = false
        }
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_AUTOCOMPLETE) {
            val selectedCarmenFeature = PlaceAutocomplete.getPlace(data)
            val style = mapboxMap.style
            if (style != null) {
                val search = LatLng(
                    (selectedCarmenFeature.geometry() as Point?)!!.latitude(),
                    (selectedCarmenFeature.geometry() as Point?)!!.longitude()
                )
                mapboxMap.animateCamera(
                    CameraUpdateFactory.newCameraPosition(
                        CameraPosition.Builder()
                            .target(search)
                            .zoom(16.0)
                            .build()
                    ), 1000
                )
                clearMarkers()
                BtnNavigation.startAnimation(btnOpen)
                BtnNavigation.isEnabled = true
                BtnNavigation.isFocusable = true
                originLocation.run {
                    val location = mapboxMap.locationComponent.lastKnownLocation
                    val startPoint = Point.fromLngLat(location?.longitude!!, location.latitude)
                    val endPoint = Point.fromLngLat(search.longitude, search.latitude)
                    getRoute(startPoint, endPoint)
                }
                addMarker(search)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onStop() {
        super.onStop()
        locationEngine.removeLocationUpdates(callback)
        mapView.onStop()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
        outState.putParcelable(SAVED_STATE_LOCATION, mapboxMap.locationComponent.lastKnownLocation)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        locationEngine.removeLocationUpdates(callback)
        mapView.onDestroy()
    }
}
