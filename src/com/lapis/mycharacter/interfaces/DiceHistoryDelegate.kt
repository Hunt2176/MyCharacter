package com.lapis.mycharacter.interfaces

import com.lapis.mycharacter.DNDSkill
import com.lapis.mycharacter.interfaces.screens.DiceHistoryController

interface DiceHistoryDelegate
{
    val diceHistory: ArrayList<DiceRoll>
    var diceHistoryController: DiceHistoryController?

    fun addToHistory(roll: DiceRoll)
    {
        this.diceHistory.add(0, roll)
        this.diceHistoryController?.addRoll(roll)
    }
}

data class DiceRoll(val name: String, val value: Int)
{
    constructor(skill: DNDSkill, value: Int): this(skill.name, value)

    override fun toString(): String = "$name: $value"
}