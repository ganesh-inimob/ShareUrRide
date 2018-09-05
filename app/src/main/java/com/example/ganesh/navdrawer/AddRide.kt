package com.example.ganesh.navdrawer

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.app.ActionBar
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import io.realm.Realm
import io.realm.RealmList
import kotlinx.android.synthetic.main.fragment_addride.*
import java.text.SimpleDateFormat
import java.util.*

class AddRide : Fragment(),TextWatcher {

    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        // TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private lateinit var adapter: RecyclerAdapter
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var gridLayoutManager: GridLayoutManager

    private var ridersList : ArrayList<Rider> = ArrayList()


    private val lastVisibleItemPosition: Int
        get() = if (RecyclerView.layoutManager == linearLayoutManager) {
            linearLayoutManager.findLastVisibleItemPosition()
        } else {
            gridLayoutManager.findLastVisibleItemPosition()
        }


    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
       val view :View = inflater!!.inflate(R.layout.fragment_addride,container,false);

       return view


    }






    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //title=getString(R.string.titleshare)

        //setSupportActionBar(toolbar)

        // setSupportActionBar(toolbar)

        //title="Share Ur Ride"



        var realm = Realm.getDefaultInstance()



        linearLayoutManager = LinearLayoutManager(getActivity())


        RecyclerView.layoutManager = linearLayoutManager


        //Date field

        val textView = view!!.findViewById<TextView>(R.id.textViewDateVal)

        textView.text = SimpleDateFormat("dd.MM.yyyy").format(System.currentTimeMillis())


        var cal = Calendar.getInstance()

        val dateSetListener = DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, monthOfYear)
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            val myFormat = "dd.MM.yyyy" // mention the format you need
            val sdf = SimpleDateFormat(myFormat, Locale.US)
            textView.text = sdf.format(cal.time)
            println("hello $cal.getTime()")

        }

        textView.setOnClickListener {
            DatePickerDialog(getActivity(), dateSetListener,
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)).show()
        }

        val totdist = view!!.findViewById<EditText>(R.id.editTextTotalDistance1)
        totdist.requestFocus()


        editTextPassngerCount.addTextChangedListener(this)

        val buttonresetride = view!!.findViewById<Button>(R.id.buttonReset)


        buttonresetride.setOnClickListener()
        {
            val totdist = view!!.findViewById<EditText>(R.id.editTextTotalDistance1)

            totdist.setText("")

            val totfare = view!!.findViewById<EditText>(R.id.editTextTotalFare)

            totfare.setText("")

            val passcnt = view!!.findViewById<EditText>(R.id.editTextPassngerCount)

            passcnt.setText("")

            val ridedtls = view!!.findViewById<EditText>(R.id.editTextRideDetails)

            ridedtls.setText("")

            totdist.requestFocus()




        }


        val buttonshareride = view!!.findViewById<Button>(R.id.buttonComputeShare)

        fun fn_estimatefare()
        {
            var totkm :Double = 0.0


            for ( i in 1..adapter.itemCount-1) {

                val view = RecyclerView.getChildAt(i)

                val km = view.findViewById<EditText>(R.id.editTextkm)

                totkm = totkm + km.text.toString().toDouble()

                println("kilo meter $totkm ")

            }

            val totfare = view!!.findViewById<EditText>(R.id.editTextTotalFare)

            val perkmfare = totfare.text.toString().toDouble()/totkm

            var ridersarr : ArrayList<Rider> = ArrayList()

            var rider = Rider(0, "header", 0.00 ,0)
            var a = ridersarr.add(rider)


            for ( i in 1..adapter.itemCount-1) {

                val view = RecyclerView.getChildAt(i)

                val km = view.findViewById<EditText>(R.id.editTextkm)

                var textFare = view.findViewById<EditText>(R.id.editTextFare)


                val kmtravelled :Double = km.text.toString().toDouble()


                val fare :Int =  (kmtravelled * perkmfare).toInt()

                var textname = view.findViewById<EditText>(R.id.editTextName)

                val ename = textname.text.toString()

                var rider = Rider(i+1, ename, kmtravelled, fare)

                var a = ridersarr.add(rider)

                println("fare $fare")

                // textFare.setText(fare)

            }
            adapter = RecyclerAdapter(ridersarr)
            RecyclerView.adapter = adapter

            val inputManager: InputMethodManager =getActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

            //if (currentFocus.windowToken != null)
            //inputManager.hideSoftInputFromWindow(currentFocus.windowToken, InputMethodManager.SHOW_FORCED)


        }

        buttonshareride.setOnClickListener(){

            var pcnt :EditText = view!!.findViewById<EditText>(R.id.editTextPassngerCount)

            var passengercnt: Int =0

            if (pcnt.text.toString().isNotEmpty()) {
                fn_estimatefare()
            }



        }

        //var buttonViewRide = view!!.findViewById<Button>(R.id.buttonViewRides)

        /*buttonViewRide.setOnClickListener{
            var addIntent = Intent(this,ViewRides::class.java)
            startActivity(addIntent)
        }*/




        fun saveRide()
        {
            val totdistance = TotalKm()
            val totfare = TotalFare()
            val noofriders=passengerCnt()
            val ridedescr= view!!.findViewById<EditText>(R.id.editTextRideDetails)
            val ridedescrtext = ridedescr.text.toString()

            var ridersarr : RealmList<Rider> = RealmList()

            for ( i in 1..adapter.itemCount-1) {

                val view = RecyclerView.getChildAt(i)

                val km = view.findViewById<EditText>(R.id.editTextkm)

                val kmdouble :Double = km.text.toString().toDouble()

                var textFare = view.findViewById<EditText>(R.id.editTextFare)

                val textFareint = Integer.parseInt(textFare.text.toString())


                var name = view.findViewById<EditText>(R.id.editTextName).text


                var rider = Rider(i+1, name.toString(),kmdouble,textFareint)

                var a = ridersarr.add(rider)

                // textFare.setText(fare)




            }


            var ride1 = Ride::class.java.newInstance()


            ride1._ID = UUID.randomUUID().toString()
            ride1.NoofRiders= noofriders
            ride1.RideDescr=ridedescrtext
            ride1.TotalDistance=totdistance.toDouble()
            ride1.TotalFare=totfare
            ride1.Riders = ridersarr
            ride1.rideDate = cal.time





            var ridemodel = RideModel()

            ridemodel.addRide(realm,ride1)

            Toast.makeText(activity,"Ride Saved successfully",Toast.LENGTH_SHORT).show()


            var results = ridemodel.getRides(realm)

            println(results[0]!!.TotalDistance)

        }

        var buttonSaveRide = view!!.findViewById<Button>(R.id.buttonSaveRide)
        buttonSaveRide.setOnClickListener{


            if (passengerCnt() > 0 ) {
                saveRide()
            }
        }





        RecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            var currentScrollPosition = 0


            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                //println("On scrolled")
                super.onScrolled(recyclerView, dx, dy)

                currentScrollPosition = recyclerView!!.computeVerticalScrollOffset() + recyclerView?.computeVerticalScrollExtent()
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) { }
        })
        //}






    }



    fun passengerCnt(): Int{

        var pcnt :EditText = view!!.findViewById<EditText>(R.id.editTextPassngerCount)

        var passengercnt: Int =0

        if (pcnt.text.toString().isNotEmpty()) {
            passengercnt = Integer.parseInt(pcnt.text.toString())
        }


        return passengercnt

    }

    fun recyclerview_population(){

        val pcnt = passengerCnt()


        if (  pcnt > 0 ){
            getRiderList()
        }
        else {
            ridersList = ArrayList()
        }

        adapter = RecyclerAdapter(ridersList)
        println("before adapter assignment call")
        RecyclerView.adapter = adapter
        //setRecyclerViewScrollListener()

    }

    fun TotalFare(): Int{

        var totfare :EditText = view!!.findViewById<EditText>(R.id.editTextTotalFare)

        var totalfare :Int = Integer.parseInt(totfare.text.toString())

        return totalfare

    }


    fun TotalKm(): Int{
        var totkm :EditText = view!!.findViewById<EditText>(R.id.editTextTotalDistance1)
        var totalkm :Int = Integer.parseInt(totkm.text.toString() )
        return totalkm
    }


    fun getRiderList()
    {
        var ridermodel = RiderModel()
        ridersList = ridermodel.getRidersdummy(TotalFare(),TotalKm(),passengerCnt())
        println("after getRiderList")
        // ridersList = ridermodel.getRidersdummy(1000,10,8)
    }


     override fun afterTextChanged(p0: Editable?) {

        //toast("after text change")
        recyclerview_population()

        val inputManager: InputMethodManager =getActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputManager.hideSoftInputFromWindow(getActivity().currentFocus.windowToken, InputMethodManager.SHOW_FORCED)
    }



}