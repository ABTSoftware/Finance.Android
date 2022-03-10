package com.scitrader.finance.state

class PropertyState {
    val values : MutableMap<String, String?>

    constructor(m: Map<out String, String?>) {
        values = HashMap(m)
    }
    constructor() {
        values = HashMap()
    }

    fun writeBool(property: String, value: Boolean) {
        writeString(property, value.toString())
    }

    fun writeInt(property: String, value: Int) {
        writeString(property, value.toString())
    }

    fun writeFloat(property: String, value: Float) {
        writeString(property, value.toString())
    }

    fun writeDouble(property: String, value: Double) {
        writeString(property, value.toString())
    }

    fun writeLong(property: String, value: Long) {
        writeString(property, value.toString())
    }

    fun writeString(property: String, value: String) {
        values[property] = value
    }

    fun readBool(property: String) : Boolean? {
        return readString(property)?.toBoolean()
    }

    fun readInt(property: String) : Int? {
        return readString(property)?.toInt()
    }

    fun readFloat(property: String) : Float? {
        return readString(property)?.toFloat()
    }

    fun readDouble(property: String) : Double? {
        return readString(property)?.toDouble()
    }

    fun readLong(property: String) : Long? {
        return readString(property)?.toLong()
    }

    fun readString(property: String) : String? {
        return values[property]
    }

}
