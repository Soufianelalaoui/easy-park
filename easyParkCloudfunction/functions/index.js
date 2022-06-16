const functions = require("firebase-functions");
const axios = require("axios");
const admin = require("firebase-admin");
admin.initializeApp(functions.config().firebase);

exports.actualiseParking = functions.pubsub.schedule("every 5 minutes").onRun((context) => {
  const db = admin.firestore();
  const batch = db.batch();
  axios.get("https://opendata.lillemetropole.fr//explore/dataset/disponibilite-parkings/download?format=json&timezone=Europe/Berlin&use_labels_for_header=false")
      .then((res) => {
        res.data.forEach( (parking) => {
          const value = {
            "id": parking.fields.id,
            "nom": parking.fields.libelle,
            "etat": parking.fields.etat,
            "ville": parking.fields.ville,
            "adresse": parking.fields.adresse,
            "placeTotal": parking.fields.max,
            "placeDispo": parking.fields.dispo,
            "datemaj": parking.fields.datemaj,
            "latitude": parking.fields.geometry.coordinates[1]+"",
            "longitude": parking.fields.geometry.coordinates[0]+"",
          };

          batch.set(db.collection("parkingParDepartement").doc("59").collection("parking").doc(parking.fields.id+""), value);
        });
        batch.commit();
      })
      .catch((err) => {
        functions.logger.info("Error: ", err.message);
      });
});


exports.actualiseParkingP = functions.pubsub.schedule("0 */1 * * *").onRun((context) => {
  const db = admin.firestore();
  const batch = db.batch();

  /**
 * Adds two numbers together.
 * @param {object} parking parking object
 * @return {int} The estimation result with baille.
 */
  function getProba(parking) {
    const notifOUI = Math.floor(Math.random() * (parking.fields.dispo-parking.fields.dispo/2)) + parking.fields.dispo/2;
    const proba = notifOUI/parking.fields.dispo * 100;

    if (proba) {
      return Math.trunc(proba) + "%";
    } else {
      return "0%";
    }
  }

  axios.get("https://opendata.lillemetropole.fr//explore/dataset/disponibilite-parkings/download?format=json&timezone=Europe/Berlin&use_labels_for_header=false")
      .then((res) => {
        const dateToday = new Date();
        res.data.forEach( (parking) => {
          const value1 = {
            "placeDispo": parking.fields.dispo,
            "annee": dateToday.getFullYear(),
            "mois": dateToday.getMonth(),
            "jours": dateToday.getDate(),
            "heures": dateToday.getHours(),
            "bailles": getProba(parking),
          };
          batch.set(db.collection("parkingDataProbas").doc(parking.fields.id).collection("parking").doc(dateToday.getFullYear() + "-" + dateToday.getMonth() + "-" + dateToday.getDate() + "-" + dateToday.getHours()),value1);
        });
        batch.commit();
      })
      .catch((err) => {
        functions.logger.info("Error: ", err.message);
      });
});
