package com.caesar.mylocal

import android.content.Context
import android.content.SharedPreferences

/**
 * Created by Caesar
 * email : caesarshao@163.com
 */
class SpTool {
    private var sp:SharedPreferences? = null

    fun init(context: Context){
        sp =  context.getSharedPreferences("Local_Sp", Context.MODE_PRIVATE)
    }

    private fun edit(): SharedPreferences.Editor = sp!!.edit()

    val theLon = "THE_LONE"
    val theLat = "THE_LAT"

    fun getLon(): Float = sp!!.getFloat(theLon, 121.63983917236328F)

    fun setLon(value: Float) = edit().putFloat(theLon, value).commit()

    fun getLat(): Float = sp!!.getFloat(theLat, 29.885562896728516F)

    fun setLat(value: Float) = edit().putFloat(theLat, value).commit()




}