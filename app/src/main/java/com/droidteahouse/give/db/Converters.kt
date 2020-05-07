package com.droidteahouse.give.db

import androidx.room.TypeConverter
import com.droidteahouse.give.vo.Charity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {

    @TypeConverter
    fun fromAreaList(value: List<Charity>): String {
        val gson = Gson()
        val type = object : TypeToken<List<Charity>>() {}.type
        return gson.toJson(value, type)
    }

    @TypeConverter
    fun toAreaList(value: String): List<Charity> {
        val gson = Gson()
        val type = object : TypeToken<List<Charity>>() {}.type
        return gson.fromJson(value, type)
    }

}

