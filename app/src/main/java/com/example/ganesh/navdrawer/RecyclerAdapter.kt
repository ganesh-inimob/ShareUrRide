package com.example.ganesh.navdrawer


import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_main.view.*






class RecyclerAdapter(private val riders: ArrayList<Rider>) : RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    val TYPE_HEADER = 0
    val TYPE_ROWDATA = 1

    var rowposition = 0


    override fun getItemCount(): Int {

        println("getitemcount    ${riders.size}")
        return riders.size
    }

    override fun getItemViewType(position: Int): Int {

        println("position inside getItemViewtype ${position}")

        if (position == 0  && rowposition ==0  ) {
            return TYPE_HEADER
            rowposition=1
        }

        return TYPE_ROWDATA
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        println("position onBindViewHolder ${position}")

        if (holder is HeaderViewHolder )
        {
            println("insider headerview holder")
            holder.headerName.setText("Name")
            holder.headerDistance.setText("Distance Travelled")
            holder.headerFare.setText("Ur Share Amt.")
        }
        if  (holder is ListViewHolder ) {

            var rider = riders[position]

            println("position inside list view if   $position")
            //if (position < riders.size && position > 0) {

            //if (view.getTag().toString() != "11") {
            //Picasso.with(view.context).load(photo.url).into(view.itemImage)

            var name = rider.name.toString()
            holder.editName.setText(rider.name)

            holder.editDistance.setText(rider.rideKm.toString())

            holder.editFare.setText(rider.rideAmount.toString())
            //}

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {

        println("viewType ${viewType}")
        if (viewType == TYPE_HEADER)
        {
            val layoutInflater = LayoutInflater.from(parent!!.context)

            val inflatedheaderView = layoutInflater.inflate(R.layout.recyclerheader, parent,false)
            return HeaderViewHolder(inflatedheaderView)

        }
        else  {
            val layoutInflater = LayoutInflater.from(parent!!.context)
            val inflatedView = layoutInflater.inflate(R.layout.rowshare_ride, parent,false)
            return ListViewHolder(inflatedView)
        }

    }

    inner class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var headerName: TextView

        var headerDistance: TextView

        var headerFare: TextView


        init {
            headerName = itemView.findViewById<TextView>(R.id.textViewName) as TextView
            headerDistance = itemView.findViewById<TextView>(R.id.textViewdistance) as TextView
            headerFare = itemView.findViewById<TextView>(R.id.textViewFare) as TextView

        }


    }

    inner class ListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var editName: EditText
        var editDistance: EditText
        var editFare: EditText



        init {
            editName = itemView.findViewById<EditText>(R.id.editTextName) as EditText
            editDistance = itemView.findViewById<EditText>(R.id.editTextkm) as EditText
            editFare = itemView.findViewById<EditText>(R.id.editTextFare) as EditText


        }
    }

}

