package com.example.ganesh.navdrawer

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.widget.ImageView
import android.widget.Toast
import io.realm.Realm
import io.realm.RealmList
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.android.synthetic.main.activity_view_rides.*
import kotlinx.android.synthetic.main.viewrides.view.*
import kotlin.collections.ArrayList

class ViewRides :   Fragment() {

    var realm = Realm.getDefaultInstance()
    var ridemodel = RideModel()
    var RideList : ArrayList<Ride> = ArrayList()

    public var adapter = MyRecyclerAdapter(RideList)

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view :View = inflater!!.inflate(R.layout.activity_view_rides,container,false);

        return view


    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

       // super.onCreate(savedInstanceState)
     //   setContentView(R.layout.activity_view_rides)

        //title = "View Rides"

        //var actionbar : android.support.v7.app.ActionBar = getSupportActionBar()!!

       // actionbar.setDisplayHomeAsUpEnabled(true)


        var linearLayoutManager = LinearLayoutManager(activity)


        recyclerviewRides.layoutManager = linearLayoutManager

        refresh()

        println("count ridelist  ${RideList.size}")

        adapter = MyRecyclerAdapter(RideList)
        recyclerviewRides.adapter = adapter







        //setContentView(R.layout.activity_view_rides);


    }

    fun refresh_adapter(position: Int)
    {adapter.notifyDataSetChanged()
    }

    fun refresh()
    {
        RideList = ArrayList()


        var results = ridemodel.getRides(realm)


        RideList =  realm.copyFromRealm(results) as ArrayList<Ride>

        println(" Total rows ${RideList.size}")

        var arrlist : RealmList<Rider> = RealmList()

        var ride: Ride = Ride()

        for ( i in 1..RideList.size)

        {

            ride = RideList[i-1] as Ride


            println ("Ride Details ${ride!!.TotalDistance} - ${ride!!.TotalFare} - ${ride!!.NoofRiders}")

            arrlist = ride!!.Riders

            for (i in 1..arrlist.size)
            {
                println(" Rider Details ${arrlist[i-1]!!.name.toString()}--${arrlist[i-1]!!.rideKm}--${arrlist[i-1]!!.rideAmount}" )



            }

            /*

            open class Rider
(        open var _ID: Int = 0,
        open var name: String = "",
        open var rideKm: Double = 0.0,
        open var rideAmount: Int = 0) : RealmObject()


                open var TotalDistance: Double = 0.0
    open var TotalFare: Int = 0
    open var NoofRiders: Int = 0
    open var RideDescr: String = ""
    open var rideDate: java.util.Date? = null
    open var Riders: RealmList<Rider>  = RealmList()
             */
        }




    }

    public class MyRecyclerAdapter (private val rides: ArrayList<Ride>) : RecyclerView.Adapter<MyRecyclerAdapter.myViewHolder>() {


        public fun deleteride (item: Int)  : Boolean{

            //val position = rides.indexOf(item)

            rides.removeAt(item)

            notifyItemRemoved(item)

            return true;

        }

        override fun onBindViewHolder(holder: MyRecyclerAdapter.myViewHolder?, position: Int)
        {
            println(" on bind " + position)
            var ride = rides[position]
            println("on bind "+ ride.NoofRiders + "-"+ ride.TotalDistance)
            holder!!.bindRide(ride)
        }

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): MyRecyclerAdapter.myViewHolder
        {
            println("on create view holder")
            //val inflatedView = parent!!.inflate(R.layout.viewrides, false)
            val layoutInflater = LayoutInflater.from(parent!!.context)
            val inflatedView = layoutInflater.inflate(R.layout.viewrides, parent,false)
            return myViewHolder(inflatedView)
        }

        override fun getItemCount(): Int {
            return rides.size
        }
        inner class myViewHolder(v: View) : RecyclerView.ViewHolder(v), View.OnClickListener
        {


            //2
            private var view: View = v
            private var ride: Ride? = null



            //3
            init {
                v.setOnClickListener(this)
            }

            //4
            override fun onClick(v: View)
            {





                var deleteimage = v.findViewById<ImageView>(R.id.imageViewDelete)



                if (deleteimage != null) deleteimage.setOnClickListener{

                    //  var position = v.recyclerviewRides.getChildAdapterPosition(this)

                    println("position ${position}")


                    var realm = Realm.getDefaultInstance()
                    var ridemodel = RideModel()

                    val textid = v.textViewuid.text

                    println("inside on click  ${textid.toString()}")


                    val result = ridemodel.removeRide(realm,textid.toString())

                    if (result)
                    {
                        println("Delete successful")
                        Toast.makeText(v.getContext(), "Delete Successful", Toast.LENGTH_SHORT).show();
                        val position = adapterPosition
                        val b = deleteride(position)
                    }

                }


                //println("hello")

            }

            fun bindRide(ride: Ride) {

                println("insdie bindRide")
                this.ride = ride
                //Picasso.with(view.context).load(photo.url).into(view.itemImage)

                //textkm = find
                this.view.textViewTkm.text = "Total Distance ${ride.TotalDistance}"
                this.view.textViewTFare.text = "Total Fare ${ride.TotalFare}"
                this.view.textViewNoRiders.text = "No. of Riders ${ride.NoofRiders}"
                this.view.textViewuid.text = ride._ID
                println("ride id ${ride._ID}")

                if ( ride.rideDate != null)
                {
                    this.view.textViewdate.text = date_text(ride.rideDate as Date)
                }

                if (ride.RideDescr != null)
                {
                    this.view.textViewDescr.text = ride.RideDescr
                }

                this.view.textViewRiders.text = riders_text(ride.Riders)


                /**/

            }

            fun riders_text(riders: RealmList<Rider>) : String{
                var riderdtl : String = "Rider Details"
                // val sb = StringBuilder()
                for (i in 1..riders.size)

                {
                    riderdtl = riderdtl+ " ${riders[i-1]!!.name.toString()}--${riders[i-1]!!.rideKm}--${riders[i-1]!!.rideAmount} "

                    if (i != riders.size  )
                    {
                        riderdtl = riderdtl + ";"
                    }



                }

                return  riderdtl



            }

            fun date_text(rdate : Date) : String
            {
                val cal: Date = rdate

                val myFormat = "dd.MM.yyyy" // mention the format you need
                val sdf = SimpleDateFormat(myFormat, Locale.US)
                return sdf.format(cal.time)
            }



            /*companion object {
                //5
                private val PHOTO_KEY = "PHOTO"
            }*/

        }

    }



}








