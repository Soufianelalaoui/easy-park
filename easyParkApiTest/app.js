const express = require('express');
const app = express();
const axios = require('axios');
const port = 80;
let bodyParser = require('body-parser');
const { initializeApp } = require('firebase-admin/app');
app.use(bodyParser.urlencoded({ extended: false }))
app.use(bodyParser.json());


app.listen(port, () => {
    console.log(`Example app listening at http://localhost:${port}`)
})
app.get('/', (req, res) => {
    axios.get('https://opendata.lillemetropole.fr//explore/dataset/disponibilite-parkings/download?format=json&timezone=Europe/Berlin&use_labels_for_header=false')
        .then(res => {
            res.data.forEach( parking => console.log(parking.fields))
        })
        .catch(err => {
            console.log('Error: ', err.message);
        });
});











