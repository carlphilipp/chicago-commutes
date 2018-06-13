package fr.cph.chicago.repository.entity

import io.realm.RealmObject

open class PositionDb(var latitude: Double = 0.0, var longitude: Double = 0.0) : RealmObject()
