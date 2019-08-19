package com.lapis.mycharacter

interface CharacterLoader
{
    var character: DNDCharacter
    fun loadCharacter(character: DNDCharacter)
    {
        this.character = character
    }
}