package com.insset.easypark.receiver

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.icu.text.DateFormat.getDateTimeInstance
import android.util.Log
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.time.LocalDateTime

class NotificationBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent?.action

        val notificationManager = context.applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if(action.equals("yes")) {
            Log.d("notif", "Yes")

            val db = Firebase.firestore

            //Remplacer 59 par le departement acruel
            val parkingCode = intent.getStringExtra("parkingCode")
            val data = hashMapOf("yesNb" to 1)
            val time = getDateTimeInstance()
            val day = LocalDateTime.now().getDayOfMonth()
            val hour = LocalDateTime.now().getHour()
            if (parkingCode != null) {
                var dayHour = db.collection("disposParDepartement")
                    .document("59")
                    .collection("parking")
                    .document(parkingCode)
                    .collection(day.toString())
                    .document(hour.toString())

                dayHour.get().addOnSuccessListener { document ->
                    if (document.data != null) {
                        Log.d("notif", "DocumentSnapshot data: ${document.data}")
                        dayHour.update("yesNb", FieldValue.increment(1))
                            .addOnSuccessListener { Log.d("notif", "DocumentSnapshot successfully written!") }
                            .addOnFailureListener { e -> Log.w("notif", "Error writing document", e) }
                    } else {
                        Log.d("notif", "No such document")
                        db.collection("disposParDepartement").document("59").collection("parking").document(parkingCode).collection(day.toString()).document(hour.toString()).set(data)
                            .addOnSuccessListener { Log.d("notif", "DocumentSnapshot successfully written!") }
                            .addOnFailureListener { e -> Log.w("notif", "Error writing document", e) }
                    }
                }


            }

            notificationManager.cancel(1)
        }
        else if(action.equals("no")){
            Log.d("notif", "no")
            val db = Firebase.firestore

            //Remplacer 59 par le departement acruel
            val parkingCode = intent.getStringExtra("parkingCode")
            val data = hashMapOf("noNb" to 1)
            val time = getDateTimeInstance()
            val day = LocalDateTime.now().getDayOfMonth()
            val hour = LocalDateTime.now().getHour()
            if (parkingCode != null) {
                var dayHour = db.collection("disposParDepartement")
                    .document("59")
                    .collection("parking")
                    .document(parkingCode)
                    .collection(day.toString())
                    .document(hour.toString())

                dayHour.get().addOnSuccessListener { document ->
                    if (document.data != null) {
                        Log.d("notif", "DocumentSnapshot data: ${document.data}")
                        dayHour.update("noNb", FieldValue.increment(1))
                            .addOnSuccessListener { Log.d("notif", "DocumentSnapshot successfully written!") }
                            .addOnFailureListener { e -> Log.w("notif", "Error writing document", e) }
                    } else {
                        Log.d("notif", "No such document")
                        db.collection("disposParDepartement").document("59").collection("parking").document(parkingCode).collection(day.toString()).document(hour.toString()).set(data)
                            .addOnSuccessListener { Log.d("notif", "DocumentSnapshot successfully written!") }
                            .addOnFailureListener { e -> Log.w("notif", "Error writing document", e) }
                    }
                }


            }

            notificationManager.cancel(1)        }
    }
}
