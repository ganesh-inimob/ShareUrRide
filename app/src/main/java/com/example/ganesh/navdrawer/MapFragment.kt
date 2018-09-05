package com.example.ganesh.navdrawer



import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.content.res.ResourcesCompat.getDrawable
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.Toast
import com.beust.klaxon.*
import com.example.ganesh.navdrawer.R.id.from_place
import com.example.ganesh.navdrawer.R.drawable.focus_do_border
import com.example.ganesh.navdrawer.R.drawable.focus_no_border
import com.google.android.gms.location.*
import com.google.android.gms.location.places.GeoDataClient
import com.google.android.gms.location.places.PlaceBufferResponse
import com.google.android.gms.location.places.Places
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import java.io.IOException
import kotlinx.android.synthetic.main.fragment_map.*
import org.jetbrains.anko.async
import org.jetbrains.anko.uiThread
import org.json.JSONObject
import java.lang.Boolean.TRUE
import java.net.URL


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 *
 */
class MapFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {


    private lateinit var mMap: GoogleMap
    private lateinit var mapView: View
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var lastLocation: Location
    lateinit var mLocationRequest: LocationRequest
    lateinit var mLocationCallback: LocationCallback
    lateinit var mGeoDataClient: GeoDataClient
    lateinit var placesAdapter: PlacesAdapter
    lateinit var mSettingsClient: SettingsClient
    lateinit var mLocationSettingsRequest: LocationSettingsRequest
    private val REQUEST_CHECK_SETTINGS = 0x1
    var isAutoCompleteLocation = false
    val BOUNDS_INDIA = LatLngBounds(LatLng(23.63936, 68.14712), LatLng(28.20453, 97.34466))
    lateinit var location: Location
    lateinit var latLng: LatLng
    internal lateinit var MarkerPoints: ArrayList<LatLng>

    private var currentMarker: Marker? = null

    private var fromMarker: Marker? = null
    private var toMarker: Marker? = null

    var fromLatLng :LatLng?= null
    var toLatLng :LatLng?= null

   private  var polylineFinal : Polyline? = null


    private var nameMarker: EditText? = null


    private fun getAddress(latLng: LatLng): String {
        // 1
        val geocoder = Geocoder(this!!.activity)
        val addresses: List<Address>?
        val address: Address?
        var addressText = ""

        try {
            // 2
           //addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)

            addresses = geocoder.getFromLocation(12.9317, 80.2308, 1)
            // 3
            if (null != addresses && !addresses.isEmpty()) {
                address = addresses[0]
                for (i in 0 until address.maxAddressLineIndex) {
                    addressText += if (i == 0) address.getAddressLine(i) else "\n" + address.getAddressLine(i)
                }
            }
        } catch (e: IOException) {
            Log.e("MapsActivity", e.localizedMessage)
        }

        return addressText
    }


    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    override fun onMarkerClick(p0: Marker?) = false


    private fun setUpMap() {
        if (ActivityCompat.checkSelfPermission(activity,
                        android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity,
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), MapsActivity.LOCATION_PERMISSION_REQUEST_CODE)
            return
        }

        // 1
        mMap.isMyLocationEnabled = true





// 2
        fusedLocationClient.lastLocation.addOnSuccessListener(activity) { location ->
            // Got last known location. In some rare situations this can be null.
            // 3
            if (location != null) {
                lastLocation = location
                val currentLatLng = LatLng(location.latitude, location.longitude)
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 18f))

                placeMarkerOnMap(currentLatLng)






            }
        }


        mMap!!.setOnMapClickListener { point ->
            MarkerPoints.add(point)

            //lastLocation = location
            val currentLatLng = point
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 18f))


            val options = MarkerOptions()

            options.position(point)
            //placeMarkerOnMap(currentLatLng)
            //mMap!!.clear()


            if (from_place.hasFocus() ==  TRUE ){

                if (fromMarker != null)
                {
                    fromMarker!!.remove()
                }
                options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                fromMarker = mMap!!.addMarker(options)
                //val titleStr = point.latitude.toString()+" " + point.longitude.toString()
                val titleStr = getAddress(point)
              //  Toast.makeText(activity,"From", Toast.LENGTH_LONG).show()
               // Toast.makeText(activity,titleStr, Toast.LENGTH_LONG).show()
            }


            if (To_Place.hasFocus() ==  TRUE ){

                if (toMarker != null)
                {
                    toMarker!!.remove()
                }
                options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET))
                toMarker = mMap!!.addMarker(options)
                //val titleStr = point.latitude.toString()+" " + point.longitude.toString()
                val titleStr = getAddress(point)
              //  Toast.makeText(activity,"To", Toast.LENGTH_LONG).show()
                //Toast.makeText(activity,titleStr, Toast.LENGTH_LONG).show()
            }

            if (fromMarker != null  && toMarker != null )
            {
                fromLatLng = fromMarker!!.position
                toLatLng = toMarker!!.position

                calculateDistance()
            }


        }

    }


    private fun getURL(from : LatLng, to : LatLng) : String {
        val origin = "origin=" + from.latitude + "," + from.longitude
        val dest = "destination=" + to.latitude + "," + to.longitude
        val sensor = "sensor=false"
        val params = "$origin&$dest&$sensor"
        return "https://maps.googleapis.com/maps/api/directions/json?$params"
    }


    private  fun calculateDistance() {

        var dist : String = "test"
        val LatLongB = LatLngBounds.Builder()

        val url = getURL(fromLatLng!!, toLatLng!!)
        val options = PolylineOptions()
        options.color(Color.RED)
        options.width(5f)




        async {
            // Connect to URL, download content and convert into string asynchronously
            val result = URL(url).readText()
            uiThread {
                // When API call is done, create parser and convert into JsonObjec
                val parser: Parser = Parser()
                val stringBuilder: StringBuilder = StringBuilder(result)
                val json: JsonObject = parser.parse(stringBuilder) as JsonObject
                // get to the correct element in JsonObject
                val routes = json.array<JsonObject>("routes")



                val legs = routes!!["legs"][0] as JsonArray<JsonObject>

                val distobj = (legs[0] as JsonObject).get("distance") as? JsonObject

               // Log.e("test 123")
                if (polylineFinal != null)
                {
                    polylineFinal!!.remove()
                }

               var distance = distobj!!.get("text") as String

                dist = distance;

                Toast.makeText(activity,distance, Toast.LENGTH_LONG).show()

               // polyline = ((jSteps.get(k) as JSONObject).get("polyline") as JSONObject).get("points") as  String




                val points = routes!!["legs"]["steps"][0] as JsonArray<JsonObject>
                // For every element in the JsonArray, decode the polyline string and pass all points to a List
                val polypts = points.flatMap { decodePoly(it.obj("polyline")?.string("points")!!)  }
                // Add  points to polyline and bounds
                options.add(fromLatLng)
                LatLongB.include(fromLatLng)
                for (point in polypts)  {
                    options.add(point)
                    LatLongB.include(point)
                }
                options.add(toLatLng)
                LatLongB.include(toLatLng)
                // build bounds
                val bounds = LatLongB.build()
                // add polyline to the map
                polylineFinal = mMap!!.addPolyline(options)
                // show map with route centered
                mMap!!.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))
            }
        }



         fun decodePoly(encoded: String): List<LatLng> {
            val poly = ArrayList<LatLng>()
            var index = 0
            val len = encoded.length
            var lat = 0
            var lng = 0

            while (index < len) {
                var b: Int
                var shift = 0
                var result = 0
                do {
                    b = encoded[index++].toInt() - 63
                    result = result or (b and 0x1f shl shift)
                    shift += 5
                } while (b >= 0x20)
                val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
                lat += dlat

                shift = 0
                result = 0
                do {
                    b = encoded[index++].toInt() - 63
                    result = result or (b and 0x1f shl shift)
                    shift += 5
                } while (b >= 0x20)
                val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
                lng += dlng

                val p = LatLng(lat.toDouble() / 1E5,
                        lng.toDouble() / 1E5)
                poly.add(p)
            }

            return poly
        }


    }


    private fun decodePoly(encoded: String): List<LatLng> {
        val poly = ArrayList<LatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0

        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].toInt() - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat

            shift = 0
            result = 0
            do {
                b = encoded[index++].toInt() - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng

            val p = LatLng(lat.toDouble() / 1E5,
                    lng.toDouble() / 1E5)
            poly.add(p)
        }

        return poly
    }


    private fun placeMarkerOnMap(location: LatLng) {
        val markerOptions = MarkerOptions().position(location)

        val titleStr = getAddress(location)  // add these two lines
        markerOptions.title(titleStr)

        mMap.addMarker(markerOptions)


    }


    override fun onMapReady(p0: GoogleMap?) {

//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        mMap = p0!!;

        //mMap.getUiSettings().setZoomControlsEnabled(true)


        if (mapView != null &&
                mapView.findViewById<View>(Integer.parseInt("1")) != null) {
            // Get the button view
            val locationButton = (mapView.findViewById<View>(Integer.parseInt("1")).parent as View).findViewById<View>(Integer.parseInt("2"));
            // and next place it, on bottom right (as Google Maps app)
            val layoutParams = locationButton.layoutParams as (RelativeLayout.LayoutParams)

            // position on right bottom
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
            layoutParams.setMargins(0, 0, 30, 30);
        }

        //val sydney = LatLng(-34.0, 151.0)
        //mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
       // mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))

        setUpMap()



    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.fragment_map, container, false)
        val view :View = inflater!!.inflate(R.layout.fragment_map,container,false);



        return view
    }

    override fun onStart() {
        super.onStart()
        from_place.setOnFocusChangeListener(object : View.OnFocusChangeListener{
            override fun onFocusChange(p0: View?, p1: Boolean) {
                if (p1){
                    p0!!.setBackgroundResource(focus_do_border)
                }
                else
                {
                    p0!!.setBackgroundResource(focus_no_border)
                }
            }
        })

        To_Place.setOnFocusChangeListener(object : View.OnFocusChangeListener{
            override fun onFocusChange(p0: View?, p1: Boolean) {
                if (p1){
                    p0!!.setBackgroundResource(focus_do_border)
                }
                else
                {
                    p0!!.setBackgroundResource(focus_no_border)
                }
            }
        })


        cancel.setOnFocusChangeListener(object : View.OnFocusChangeListener{
            override fun onFocusChange(p0: View?, p1: Boolean) {
                if (p1){
                    p0!!.setBackgroundResource(focus_do_border)
                }
            }
        })

    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var mapFragment : SupportMapFragment?=null
        mapFragment = childFragmentManager.findFragmentById(R.id.map1) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
        MarkerPoints = ArrayList<LatLng>()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity)

        mGeoDataClient = Places.getGeoDataClient(activity, null);

        mapView = mapFragment!!.getView()!!;


        mLocationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                val loc = locationResult!!.lastLocation
                if (!isAutoCompleteLocation) {
                    location = loc
                    latLng = LatLng(location.latitude, location.longitude)
                    assignToMap()
                }
            }

        }


        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval((10 * 1000).toLong())        // 10 seconds, in milliseconds
                .setFastestInterval((6 * 1000).toLong()) // 1 second, in milliseconds

        mSettingsClient = LocationServices.getSettingsClient(activity)
        val builder = LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest)
        mLocationSettingsRequest = builder.build()

        placesAdapter = PlacesAdapter(activity, android.R.layout.simple_list_item_1, mGeoDataClient, null, BOUNDS_INDIA)
        from_place.setAdapter(placesAdapter)
        from_place.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (count > 0) {
                    cancel.visibility = View.VISIBLE
                } else {
                    cancel.visibility = View.GONE
                }
            }
        })
        from_place.setOnItemClickListener({ parent, view, position, id ->
            //getLatLong(placesAdapter.getPlace(position))
            //hideKeyboard()
            val item = placesAdapter.getItem(position)
            val placeId = item?.getPlaceId()
            val primaryText = item?.getPrimaryText(null)

            Log.i("Autocomplete", "Autocomplete item selected: " + primaryText)


            val placeResult = mGeoDataClient.getPlaceById(placeId)
            placeResult.addOnCompleteListener(object : OnCompleteListener<PlaceBufferResponse> {
                override fun onComplete(task: Task<PlaceBufferResponse>) {
                    val places = task.getResult()
                    val place = places.get(0)

                    val placeId = place.id
                    isAutoCompleteLocation = true
                    latLng = place.latLng
                    places.release()
                    assignToMap()


                }

            })

            /* Toast.makeText(applicationContext, "Clicked: " + primaryText,
                     Toast.LENGTH_SHORT).show()*/
        })
        cancel.setOnClickListener {
            from_place.setText("")
        }
        if (::mMap.isInitialized) {
            mMap!!.setOnMapClickListener(object : GoogleMap.OnMapClickListener {
                override fun onMapClick(latLng: LatLng) {
                    currentMarker = null
                }
            })
        }

    }

    fun hideKeyboard() {
        try {
            val inputMethodManager = getActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(activity.currentFocus!!.windowToken, 0)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun assignToMap() {
        mMap?.clear()

        val options = MarkerOptions()
                .position(latLng)
                .title("My Location")
        mMap?.apply {
            addMarker(options)
            moveCamera(CameraUpdateFactory.newLatLng(latLng))
            animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
        }
    }


}
