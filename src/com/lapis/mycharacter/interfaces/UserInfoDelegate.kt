package com.lapis.mycharacter.interfaces

import com.lapis.mycharacter.DNDCharacter
import com.lapis.mycharacter.util.DatabaseUser
import java.io.File
import java.time.LocalDateTime

interface UserInfoDelegate
{
    val userInfo: DatabaseUser
    val saveFile: File

    val userId: String
        get() = userInfo.userId

    val syncDate: LocalDateTime
        get() = userInfo.syncDate

    val characterIds: Collection<Int>
        get() = userInfo.characters.map { it.characterId }

    val characters: Collection<DNDCharacter>
        get() = userInfo.characters

    fun writeCharacter(character: DNDCharacter)
    {
        userInfo.writeCharacter(character)
    }

    fun removeCharacter(character: DNDCharacter)
    {
        userInfo.removeCharacter(character)
    }

    fun removeCharacter(charId: Int)
    {
        userInfo.removeCharacter(charId)
    }
}