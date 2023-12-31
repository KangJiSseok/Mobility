package com.example.mobility.model

class ItemData {
    var Profile = hashMapOf(
        "id" to "",
        "name" to "",
        "phone number" to ""
    )
    var CarInfo = hashMapOf(
        "model" to "",
        "year" to "",
        "odo" to "0",
        "lastDate" to "2023-07-01"
    )
    var RepairInfo = hashMapOf(
        "engineOdo" to "0",
        "engineDate" to "2023-07-01",
        "acOdo" to "0",
        "acDate" to "2023-07-01",
        "tireOdo" to "0",
        "tireDate" to "2023-07-01",
    )
}