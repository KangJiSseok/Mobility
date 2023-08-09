package com.example.mobility.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallback
import androidx.core.content.ContextCompat
import androidx.core.view.drawToBitmap
import com.example.mobility.R
import com.github.ybq.android.spinkit.SpinKitView
import com.github.ybq.android.spinkit.sprite.Sprite
import com.github.ybq.android.spinkit.style.DoubleBounce
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import noman.googleplaces.NRPlaces
import noman.googleplaces.Place
import noman.googleplaces.PlacesException
import noman.googleplaces.PlacesListener
import java.io.IOException
import java.util.Locale


class GoogleMapActivity() : AppCompatActivity(), OnMapReadyCallback,
    OnRequestPermissionsResultCallback,PlacesListener {
    private var part: String? = null
    private var name: String? = null
    private var mMap: GoogleMap? = null

    // 카메라 위치 저장
    private var cameraPosition: LatLng? = null

    private var currentMarker: Marker? = null
    var needRequest = false
    var previous_marker: MutableList<Marker>? = null

    private var firstLocation : Boolean = true

    // 앱을 실행하기 위해 필요한 퍼미션을 정의합니다.
    var REQUIRED_PERMISSIONS = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ) // 외부 저장소
    var mCurrentLocatiion: Location? = null
    var currentPosition: LatLng? = null

    private var mFusedLocationClient: FusedLocationProviderClient? = null
    private var locationRequest: LocationRequest? = null
    private var location: Location? = null
    private var mLayout // Snackbar 사용하기 위해서는 View가 필요합니다.
            : View? = null

    private lateinit var loadingView: View

    // (참고로 Toast에서는 Context가 필요했습니다.)
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Intent 에서 데이터 가져오기
        part = intent.getStringExtra("part")
        when(part){
            "car_repair" -> name = "정비소"
            "gas_station" -> name = "주유소"
            "car_wash" -> name = "세차장"
        }
        title = "주변 $name 검색"

        supportActionBar?.setDisplayHomeAsUpEnabled(true);
        window.setFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        )
        setContentView(com.example.mobility.R.layout.activity_google_map)
        mLayout = findViewById<View>(com.example.mobility.R.id.layout_google_map)
        locationRequest = LocationRequest()
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            .setInterval(UPDATE_INTERVAL_MS.toLong())
            .setFastestInterval(FASTEST_UPDATE_INTERVAL_MS.toLong())

        val builder = LocationSettingsRequest.Builder()
        builder.addLocationRequest(locationRequest!!)

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        val mapFragment = supportFragmentManager
            .findFragmentById(com.example.mobility.R.id.map) as SupportMapFragment?
        mapFragment!!.getMapAsync(this)

        previous_marker = ArrayList()

        val button: Button = findViewById<View>(com.example.mobility.R.id.fab) as Button
        loadingView = findViewById(com.example.mobility.R.id.loading)

        button.setOnClickListener {
            doSomethingWithCenterLatLng()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        Log.d(TAG, "onMapReady :")
        mMap = googleMap

        loadingView.visibility = View.VISIBLE
        //런타임 퍼미션 요청 대화상자나 GPS 활성 요청 대화상자 보이기전에
        //지도의 초기위치를 서울로 이동
        setDefaultLocation()


        //런타임 퍼미션 처리
        // 1. 위치 퍼미션을 가지고 있는지 체크합니다.
        val hasFineLocationPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        val hasCoarseLocationPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
            hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED
        ) {

            // 2. 이미 퍼미션을 가지고 있다면
            // ( 안드로이드 6.0 이하 버전은 런타임 퍼미션이 필요없기 때문에 이미 허용된 걸로 인식합니다.)
            startLocationUpdates() // 3. 위치 업데이트 시작
        } else {  //2. 퍼미션 요청을 허용한 적이 없다면 퍼미션 요청이 필요합니다. 2가지 경우(3-1, 4-1)가 있습니다.

            // 3-1. 사용자가 퍼미션 거부를 한 적이 있는 경우에는
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    REQUIRED_PERMISSIONS[0]
                )
            ) {

                // 3-2. 요청을 진행하기 전에 사용자가에게 퍼미션이 필요한 이유를 설명해줄 필요가 있습니다.
                Snackbar.make(
                    mLayout!!, "이 기능을 사용하려면 위치 접근 권한이 필요합니다.",
                    Snackbar.LENGTH_INDEFINITE
                )
                    .setAction("확인") { // 3-3. 사용자게에 퍼미션 요청을 합니다. 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                        ActivityCompat.requestPermissions(
                            this@GoogleMapActivity, REQUIRED_PERMISSIONS,
                            PERMISSIONS_REQUEST_CODE
                        )
                    }.show()
            } else {
                // 4-1. 사용자가 퍼미션 거부를 한 적이 없는 경우에는 퍼미션 요청을 바로 합니다.
                // 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions(
                    this, REQUIRED_PERMISSIONS,
                    PERMISSIONS_REQUEST_CODE
                )
            }
        }
        mMap!!.uiSettings.isMyLocationButtonEnabled = true
        mMap!!.uiSettings.isZoomControlsEnabled =true

        mMap!!.setOnCameraMoveListener {
            cameraPosition = mMap!!.cameraPosition.target
        }
    }

    fun doSomethingWithCenterLatLng() {
        CoroutineScope(Dispatchers.Main).launch {
//            loadingView.visibility = View.VISIBLE

            if (cameraPosition != null) {
                val latitude = cameraPosition!!.latitude
                val longitude = cameraPosition!!.longitude
                // centerLatLng를 사용하여 원하는 작업 수행
                Log.d(TAG, "화면 정가운데 좌표 - 위도: $latitude, 경도: $longitude")
                showPlaceInformation(LatLng(latitude, longitude))
                val cameraUpdate = CameraUpdateFactory.newLatLngZoom(LatLng(latitude, longitude),15f)
                mMap!!.animateCamera(cameraUpdate)

            }
        }
    }

    var locationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)
            val locationList = locationResult.locations
            if (locationList.size > 0) {
                location = locationList[locationList.size - 1]
                currentPosition = LatLng(location!!.latitude, location!!.longitude)
                val markerTitle = getCurrentAddress(currentPosition!!)
                val markerSnippet =
                    "위도:" + location!!.latitude.toString() + " 경도:" + location!!.longitude.toString()
                Log.d(TAG, "onLocationResult : $markerSnippet")

                // 첫 번째 위치 업데이트 이후에는 카메라 고정을 해제
                if (firstLocation) {
                    setCurrentLocation(location, markerTitle, markerSnippet)
                    firstLocation = false
                    showPlaceInformation(currentPosition!!)
                }

                mCurrentLocatiion = location
            }
        }
    }


    private fun startLocationUpdates() {
        if (!checkLocationServicesStatus()) {
            Log.d(TAG, "startLocationUpdates : call showDialogForLocationServiceSetting")
            showDialogForLocationServiceSetting()
        } else {
            val hasFineLocationPermission = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            val hasCoarseLocationPermission = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            if (hasFineLocationPermission != PackageManager.PERMISSION_GRANTED ||
                hasCoarseLocationPermission != PackageManager.PERMISSION_GRANTED
            ) {
                Log.d(TAG, "startLocationUpdates : 퍼미션 안가지고 있음")
                return
            }
            Log.d(TAG, "startLocationUpdates : call mFusedLocationClient.requestLocationUpdates")
            mFusedLocationClient!!.requestLocationUpdates(
                locationRequest!!,
                locationCallback,
                Looper.myLooper()
            )
            if (checkPermission()) mMap!!.isMyLocationEnabled = true
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart")
        if (checkPermission()) {
            Log.d(TAG, "onStart : call mFusedLocationClient.requestLocationUpdates")
            mFusedLocationClient!!.requestLocationUpdates(locationRequest!!, locationCallback, null)
            if (mMap != null) mMap!!.isMyLocationEnabled = true
        }
    }

    override fun onStop() {
        super.onStop()
        if (mFusedLocationClient != null) {
            Log.d(TAG, "onStop : call stopLocationUpdates")
            mFusedLocationClient!!.removeLocationUpdates(locationCallback)
        }
    }

    fun getCurrentAddress(latlng: LatLng): String {

        //지오코더... GPS를 주소로 변환
        val geocoder = Geocoder(this, Locale.getDefault())
        val addresses: List<Address>?
        try {
            addresses = geocoder.getFromLocation(
                latlng.latitude,
                latlng.longitude,
                1
            )
        } catch (ioException: IOException) {
            //네트워크 문제
            Toast.makeText(this, "지오코더 서비스 사용불가", Toast.LENGTH_LONG).show()
            return "지오코더 서비스 사용불가"
        } catch (illegalArgumentException: IllegalArgumentException) {
            Toast.makeText(this, "잘못된 GPS 좌표", Toast.LENGTH_LONG).show()
            return "잘못된 GPS 좌표"
        }
        if (addresses.isNullOrEmpty()) {
            Toast.makeText(this, "주소 미발견", Toast.LENGTH_LONG).show()
            return "주소 미발견"
        } else {
            val address = addresses[0]
            return address.getAddressLine(0).toString()
        }
    }

    fun checkLocationServicesStatus(): Boolean {
        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        return (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
    }

    fun setCurrentLocation(location: Location?, markerTitle: String?, markerSnippet: String?) {
        if (currentMarker != null) currentMarker!!.remove()
        val currentLatLng = LatLng(
            location!!.latitude, location.longitude
        )
        val markerOptions = MarkerOptions()
        markerOptions.position(currentLatLng)
        markerOptions.title(markerTitle)
        markerOptions.snippet(markerSnippet)
        markerOptions.draggable(true)
        currentMarker = mMap!!.addMarker(markerOptions)
        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(currentLatLng,15f)
        mMap!!.animateCamera(cameraUpdate)
        mMap!!.moveCamera(cameraUpdate)
    }

    fun setDefaultLocation() {


        //디폴트 위치, Seoul
        val DEFAULT_LOCATION = LatLng(37.56, 126.97)
        val markerTitle = "위치정보 가져올 수 없음"
        val markerSnippet = "위치 퍼미션과 GPS 활성 요부 확인하세요"
        if (currentMarker != null) currentMarker!!.remove()
        val markerOptions = MarkerOptions()
        markerOptions.position(DEFAULT_LOCATION)
        markerOptions.title(markerTitle)
        markerOptions.snippet(markerSnippet)
        markerOptions.draggable(true)
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
        currentMarker = mMap!!.addMarker(markerOptions)
        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(DEFAULT_LOCATION, 15f)
        mMap!!.moveCamera(cameraUpdate)
    }

    //여기부터는 런타임 퍼미션 처리을 위한 메소드들
    private fun checkPermission(): Boolean {
        val hasFineLocationPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        val hasCoarseLocationPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        return hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
            hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED
    }

    /*
     * ActivityCompat.requestPermissions를 사용한 퍼미션 요청의 결과를 리턴받는 메소드입니다.
     */
    @SuppressLint("MissingSuperCall")
    override fun onRequestPermissionsResult(
        permsRequestCode: Int,
        permissions: Array<String>,
        grandResults: IntArray
    ) {
        if (permsRequestCode == PERMISSIONS_REQUEST_CODE && grandResults.size == REQUIRED_PERMISSIONS.size) {

            // 요청 코드가 PERMISSIONS_REQUEST_CODE 이고, 요청한 퍼미션 개수만큼 수신되었다면
            var check_result = true


            // 모든 퍼미션을 허용했는지 체크합니다.
            for (result: Int in grandResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false
                    break
                }
            }
            if (check_result) {

                // 퍼미션을 허용했다면 위치 업데이트를 시작합니다.
                startLocationUpdates()
            } else {
                // 거부한 퍼미션이 있다면 앱을 사용할 수 없는 이유를 설명해주고 앱을 종료합니다.2 가지 경우가 있습니다.
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        REQUIRED_PERMISSIONS[0]
                    )
                    || ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        REQUIRED_PERMISSIONS[1]
                    )
                ) {


                    // 사용자가 거부만 선택한 경우에는 앱을 다시 실행하여 허용을 선택하면 앱을 사용할 수 있습니다.
                    Snackbar.make(
                        mLayout!!, "퍼미션이 거부되었습니다. 앱을 다시 실행하여 퍼미션을 허용해주세요. ",
                        Snackbar.LENGTH_INDEFINITE
                    ).setAction(
                        "확인"
                    ) { finish() }.show()
                } else {


                    // "다시 묻지 않음"을 사용자가 체크하고 거부를 선택한 경우에는 설정(앱 정보)에서 퍼미션을 허용해야 앱을 사용할 수 있습니다.
                    Snackbar.make(
                        mLayout!!, "퍼미션이 거부되었습니다. 설정(앱 정보)에서 퍼미션을 허용해야 합니다. ",
                        Snackbar.LENGTH_INDEFINITE
                    ).setAction(
                        "확인"
                    ) { finish() }.show()
                }
            }
        }
    }

    //여기부터는 GPS 활성화를 위한 메소드들
    private fun showDialogForLocationServiceSetting() {
        val builder = AlertDialog.Builder(this@GoogleMapActivity)
        builder.setTitle("위치 서비스 비활성화")
        builder.setMessage(
            "앱을 사용하기 위해서는 위치 서비스가 필요합니다.\n"
                    + "위치 설정을 수정하실래요?"
        )
        builder.setCancelable(true)
        builder.setPositiveButton("설정") { dialog, id ->
            val callGPSSettingIntent =
                Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivityForResult(
                callGPSSettingIntent,
                GPS_ENABLE_REQUEST_CODE
            )
        }
        builder.setNegativeButton(
            "취소"
        ) { dialog, id -> dialog.cancel() }
        builder.create().show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            GPS_ENABLE_REQUEST_CODE ->
                //사용자가 GPS 활성 시켰는지 검사
                if (checkLocationServicesStatus()) {
                    if (checkLocationServicesStatus()) {
                        Log.d(TAG, "onActivityResult : GPS 활성화 되있음")
                        needRequest = true
                        return
                    }
                }
        }
    }

    companion object {
        private val TAG = "googlemap_example"
        private val GPS_ENABLE_REQUEST_CODE = 2001
        private val UPDATE_INTERVAL_MS = 1000 // 1초
        private val FASTEST_UPDATE_INTERVAL_MS = 500 // 0.5초

        // onRequestPermissionsResult에서 수신된 결과에서 ActivityCompat.requestPermissions를 사용한 퍼미션 요청을 구별하기 위해 사용됩니다.
        private val PERMISSIONS_REQUEST_CODE = 100
    }

    override fun onPlacesFailure(e: PlacesException?) {
        if (e.toString() == "noman.googleplaces.PlacesException: ZERO_RESULTS") {
            Snackbar.make(
                mLayout!!, "주변에 ${name}이(가) 없습니다.",
                Snackbar.LENGTH_SHORT
            ).apply {
                this.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).textAlignment = View.TEXT_ALIGNMENT_CENTER
            }
                .show()
            runOnUiThread {
                loadingView.startAnimation(AnimationUtils.loadAnimation(applicationContext,R.anim.fade_out))
            }
        }
    }

    override fun onPlacesStart() {
        Log.d("place","start")
    }

    override fun onPlacesSuccess(places: List<Place>) {

        runOnUiThread {
            for (place in places) {
                val latLng = LatLng(
                    place.latitude, place.longitude
                )
                val markerSnippet = getCurrentAddress(latLng)
                val markerOptions = MarkerOptions()
                markerOptions.position(latLng)
                markerOptions.title(place.name)
                markerOptions.snippet(markerSnippet)
                val item = mMap!!.addMarker(markerOptions)
                previous_marker!!.add(item!!)
            }

            //중복 마커 제거
            val hashSet = HashSet<Marker>()
            hashSet.addAll(previous_marker!!)
            previous_marker!!.clear()
            previous_marker!!.addAll(hashSet)
        }

    }

    override fun onPlacesFinished() {
        Log.d("place","finish")
        loadingView.startAnimation(AnimationUtils.loadAnimation(applicationContext,R.anim.fade_out))
    }

    fun showPlaceInformation(location: LatLng) {
        mMap!!.clear() //지도 클리어
        if (previous_marker != null) previous_marker!!.clear() //지역정보 마커 클리어
        NRPlaces.Builder()
            .listener(this@GoogleMapActivity)
            .key("AIzaSyDFUX-x4bgnuESK-ZMkcgOrszkqty801To")
            .latlng(location.latitude, location.longitude) //현재 위치
            .radius(600) //1000 미터 내에서 검색
            .type(part) //키워드
            .build()
            .execute()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }
}