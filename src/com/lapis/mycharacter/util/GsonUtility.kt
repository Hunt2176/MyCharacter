package com.lapis.mycharacter.util

import com.google.gson.Gson
import java.io.File

class GsonUtility
{
    val gson = Gson()

    inline fun <reified T> writeArray(to: File, toWrite: ArrayList<T>)
    {
        to.writeText(gson.toJson(toWrite.toTypedArray()))
    }

    inline fun <reified T> toJson(from: T): String
    {
        return gson.toJson(from)
    }

    inline fun <reified T> create(from: String, clazz: Class<T>): T
    {
        return gson.fromJson(from, clazz)
    }

    inline fun <reified T> readArray(from: File, to: ArrayList<T> = arrayListOf()): ArrayList<T>
    {
        val json = from.readLines().joinToString("")
        gson.fromJson<Array<T>>(json, to.toTypedArray().javaClass).toCollection(to)
        return to
    }
    inline fun <reified T> readArray(from: String, to: ArrayList<T> = arrayListOf()): ArrayList<T>
    {
        gson.fromJson<Array<T>>(from, to.toTypedArray().javaClass).toCollection(to)
        return to
    }

    inline fun <reified T> embedIntoUser(username: String, toEmbed: ArrayList<T>): String
    {
        val map = mutableMapOf<String, Array<T>>()
        map[username] = toEmbed.toTypedArray()
        return gson.toJson(map)
    }
}