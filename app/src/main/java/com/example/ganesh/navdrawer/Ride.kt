package com.example.ganesh.navdrawer


import io.realm.Realm
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.RealmResults
import io.realm.annotations.PrimaryKey
import java.util.*

/**
 * Created by ganesh on 09-02-2018.
 */
open class Ride
(
) : RealmObject()
{

    @PrimaryKey open var _ID: String = UUID.randomUUID().toString()
    open var TotalDistance: Double = 0.0
    open var TotalFare: Int = 0
    open var NoofRiders: Int = 0
    open var RideDescr: String = ""
    open var rideDate: java.util.Date? = null
    open var Riders: RealmList<Rider>  = RealmList()

}

interface RideInterface {
    fun addRide(realm: Realm, ride: Ride): Boolean
    //fun delExpense(realm: Realm, _ID: Int): Boolean
    //fun editExpense(realm: Realm, expense: Expense): Boolean
    //fun getExpense(realm: Realm, _ID: Int): Expense
    fun removeRide(realm: Realm,_id: String) : Boolean
}

open class RideModel: RideInterface {

    override fun addRide(realm: Realm, ride: Ride): Boolean {
        try {
            realm.beginTransaction()
            realm.copyToRealm(ride)
            realm.commitTransaction()
            return true
        } catch (e: Exception) {
            println(e)
            realm.commitTransaction()

            return false
        }
    }

    override fun removeRide(realm: Realm,_id: String) : Boolean{
        try{
            realm.beginTransaction()
            var ride: Ride = Ride()
            var ride1 = realm.where(Ride::class.java).equalTo("_ID",_id)!!.findAll()!!
            ride1.deleteAllFromRealm()
            realm.commitTransaction()
            return true
        } catch (e: Exception) {
            println(e)
            realm.commitTransaction()

            return false
        }



    }

    fun getRides(realm: Realm): RealmResults<Ride> {
        return realm.where(Ride::class.java).findAll()
    }

}