package com.lapis.mycharacter.util

import java.io.File

class FileKt(private val base: File)
{
    fun writeText(toWrite: String)
    {
        base.writeText(toWrite)
    }

    fun readText(): String = base.readText()
}