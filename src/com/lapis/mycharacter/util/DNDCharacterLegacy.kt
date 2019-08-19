package com.lapis.mycharacter.util

import com.lapis.mycharacter.*
import java.io.File
import java.util.concurrent.ThreadLocalRandom

private class DNDCharacterLegacyEX
{
    var name = ""
    var armourClass = 0
    var proficiencyBonus = 0

    val characterId = ThreadLocalRandom.current().nextInt()
    var items = arrayListOf<DNDItem>()

    private var skillLevels = mutableMapOf<Int, Int>()
    private var skills = SkillSchema("")
    private var gauges = arrayListOf<DNDValueGauge>()
    private var proficiencies = arrayListOf<DNDSkill>()

    fun createCurrent(): DNDCharacter
    {
        val character = DNDCharacter()
        character.name = name
        character.armourClass = armourClass
        character.proficiencyBonus = proficiencyBonus
        character.characterId = characterId
        character.items = items

        character.setSkillSchema(skills)
        skillLevels.forEach { (key: Int, value: Int) ->
            skills.skillFromId(key).ifNotNull { character.setLevel(value, it) }
        }

        character.setGauges(gauges)
        character.setProficiencies(proficiencies.toTypedArray())

        return character
    }
}

class DNDCharacterLegacy
{
    fun checkFile(file: File): Pair<Boolean, ArrayList<DNDCharacter>>
    {
        if (file.exists())
        {
            return try
            {
                Pair(true, ArrayList(GsonUtility().readArray<DNDCharacterLegacyEX>(file).map { it.createCurrent() }))
            }
            catch (e: Exception)
            {
                e.printStackTrace()
                Pair(false, arrayListOf())
            }
        }
        return Pair(true, arrayListOf())
    }
}