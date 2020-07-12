package com.lapis.mycharacter

import com.lapis.mycharacter.util.*
import java.io.File
import java.lang.Exception
import java.util.concurrent.ThreadLocalRandom
import kotlin.math.roundToInt

class DNDCharacter(characterID: Int? = null)
{
    constructor (skillSchema: SkillSchema): this()
    {
        skills = skillSchema
    }

    constructor(vararg gauges: DNDValueGauge): this()
    {
        gauges.forEach { addGauge(it) }
    }

    val version = currentVersion
    var name = ""
    var armourClass = 0
    var proficiencyBonus = 0

    var diceType = DNDDice.d20

    var characterId = characterID ?: ThreadLocalRandom.current().nextInt()
    var items = arrayListOf<DNDItem>()

    private var skillLevels = arrayListOf<CharacterSkillLevel>()
    private var skills = SkillSchema("")
    private var gauges = arrayListOf<DNDValueGauge>()
    private var proficiencies = arrayListOf<DNDSkill>()

    fun setProficiencyBonus(value: Int): DNDCharacter
    {
        this.proficiencyBonus = value
        return this
    }

    fun setProficiencies(profs: Array<DNDSkill>?): DNDCharacter
    {
        if (profs != null)
        {
            proficiencies.clear()
            profs.forEach { addProficiency(it) }
        }
        return this
    }

    fun setDice(dice: DNDDice): DNDCharacter
    {
        diceType = dice
        return this
    }

    fun setSkills(vararg skills: DNDSkill): DNDCharacter
    {
        skills.forEach { this.skills.add(it) }
        return this
    }

    fun setSkillSchema(schema: SkillSchema?): DNDCharacter
    {
        schema.ifNotNull { skills = it }
        return this
    }

    fun addGauges(gauges: Collection<DNDValueGauge>)
    {
        gauges.forEach { new -> this.gauges.removeIf { it.name == new.name } }
        this.gauges.addAll(gauges)
    }

    fun setGauges(vararg gauges: DNDValueGauge): DNDCharacter
    {
        gauges.forEach { new -> this.gauges.removeIf { it.name == new.name } }
        gauges.forEach { addGauge(it) }
        return this
    }

    fun setGauges(gauges: Collection<DNDValueGauge>?): DNDCharacter
    {
        if (gauges != null)
        {
            this.gauges.clear()
            gauges.forEach { addGauge(it) }
        }
        return this
    }

    fun addProficiency(vararg proficiencies: DNDSkill)
    {
        proficiencies.forEach { if (!this.proficiencies.contains(it)) this.proficiencies.add(it) }
    }

    fun removeProficiency(vararg skills: DNDSkill)
    {
        proficiencies.removeAll(skills)
    }

    fun addGauge(gauge: DNDValueGauge)
    {
        gauges.add(gauge)
    }

    fun removeGauge(gauge: DNDValueGauge)
    {
        gauges.remove(gauge)
    }

    fun setLevel(level: Int, skill: DNDSkill): DNDCharacter
    {
        val index = skillLevels.firstIndexWhereOrNull { it.skillId == skill.skillId }
        if (index != null)
        {
            skillLevels[index].skillLevel = level
        }
        else
        {
            skillLevels.add(CharacterSkillLevel(skill.skillId, level))
        }
        return this
    }

    fun getDiceSides(): Int
            = diceType.sides

    fun getSkills(): SkillSchema
            = skills

    fun getGauges(): ArrayList<DNDValueGauge>
            = gauges

    fun getProficiencies(): ArrayList<DNDSkill>
            = proficiencies

    fun getGauge(name: String): DNDValueGauge?
    {
        return gauges.find { it.name == name }
    }

    fun getGauge(gauge: DNDValueGauge): DNDValueGauge?
    {
        return gauges.find { it == gauge }
    }

    fun getSkillLevel(skill: DNDSkill): Int
    {
        return skillLevels.firstOrNull { it.skillId == skill.base.getSkill().skillId }?.skillLevel ?: 0
    }

    fun getSkillPlus(skill: DNDSkill): Int
    {
        val base = getSkillLevel(skill)
        var adv = if (base != 0) advCalculator(base) else 0
        if (proficiencies.firstOrNull { it.skillId == skill.skillId } != null) adv += proficiencyBonus

        return adv
    }

    override fun toString(): String = name

    private fun advCalculator(level: Int): Int
    {
        val x: Double = (level - 10.0)/2.0
        return if ((x - x.toInt()) != 0.0 && x != 0.0)
        {
            x.roundToInt() - 1
        }
        else
        {
            x.roundToInt()
        }
    }

    companion object
    {
        val currentVersion = 2

        fun fromJson(json: String): ArrayList<DNDCharacter>
        {
            val toReturn = arrayListOf<DNDCharacter>()
            try
            {
                GsonUtility().readArray(json, toReturn)
            }
            catch (e: Exception)
            {
                e.printStackTrace()
            }
            return toReturn
        }
    }

}

class SkillSchema(var name: String, private var skills: ArrayList<DNDSkill>, var usesBaseAsSkills: Boolean = true)
{
    var schemaId = ThreadLocalRandom.current().nextInt(1000, Int.MAX_VALUE)

    val size: Int
        get() = getSkills().size

    init
    {
        skills = skills.apply { addAll(0, DNDBaseSkill.values().map { it.getSkill() }) }
    }

    constructor (name: String, vararg skills: DNDSkill): this(name, ArrayList(skills.toList()))

    fun add(skill: DNDSkill)
    {
        skills = skills.apply { add(size - 1, skill) }
    }

    fun addAll(collection: Collection<DNDSkill>)
    {
        skills = skills.apply { addAll(size - 1, collection) }
    }

    fun clear()
    {
        skills.clear()
    }

    fun contains(skill: DNDSkill): Boolean
    {
        for (i in skills)
        {
            if (i.skillId == skill.skillId) return true
        }
        return false
    }

    fun skillFromId(skillId: Int): DNDSkill?
    {
        return skills.firstOrNull { it.skillId == skillId }
    }

    fun remove(at: Int)
    {
        skills.removeAt(at)
    }

    fun remove(skill: DNDSkill)
    {
        skills.remove(skill)
    }

    fun getSkills(): Collection<DNDSkill>
    {
        if (usesBaseAsSkills) return skills
        val toReturn = arrayListOf<DNDSkill>().apply { addAll(skills) }
        val baseIds = DNDBaseSkill.values().map { it.getSkill().skillId }
        toReturn.removeIf { baseIds.contains(it.skillId) }
        return toReturn
    }

    fun writeToFile(file: File)
    {
        val gson = GsonUtility()
        val array = if (file.exists()) gson.readArray<SkillSchema>(file) else arrayListOf()
        array.removeIf { it.schemaId == this.schemaId }
        array.add(this)
        gson.writeArray(file, array)
    }

    fun removeFromFile(file: File)
    {
        if (file.exists())
        {
            val gson = GsonUtility()
            val array = gson.readArray<SkillSchema>(file)
            val index: Int = array.firstIndexWhere { it.schemaId == this.schemaId }
            if (index > -1 && index < array.size) gson.writeArray(file, array.apply { removeAt(index) })
        }
    }

    override fun toString(): String = name

    companion object
    {
        fun readFrom(file: File): ArrayList<SkillSchema>
        {
            return if (file.exists()) GsonUtility().readArray(file) else arrayListOf()
        }

        fun defaultSkillSchema(): SkillSchema
        {
            return SkillSchema("Default", ArrayList(DNDBaseAttribute.values().map { it.getSkill() })).also { it.schemaId = 0 }
        }
    }
}

data class DNDItem(var name: String,
                   var description: String,
                   var quantity: Int = 1,
                   val itemId: Int = ThreadLocalRandom.current().nextInt())

class DNDSkill(var name: String, var base: DNDBaseSkill, val skillId: Int = ThreadLocalRandom.current().nextInt())
{
    override fun toString(): String = name
}

data class CharacterSkillLevel(val skillId: Int, var skillLevel: Int)

class DNDValueGauge(var name: String, private var max: Int, private var current: Int)
{
    var allowAboveMax = false
    set(value) {
        if (current > max && !value) setCurrent(max)
        field = value
    }

    init
    {
        setCurrent(current)
        setMax(max)
    }

    fun setMax(newMax: Int)
    {
        max = if (newMax < 0) 0 else newMax
    }

    fun setCurrent(newCurrent: Int)
    {
        if (newCurrent > max)
        {
            if (allowAboveMax) current = newCurrent
            return
        }
        current = newCurrent
    }

    fun increment(by: Int = 1)
    {
        setCurrent(current + by)
    }

    fun decrement(by: Int = 1)
    {
        setCurrent(current - by)
    }

    fun getStatus(): String = "$name $current/$max"

    fun getProgress(): Double = current.toDouble() / max

    fun getCurrent() = current

    fun getMax() = max


}

enum class DNDBaseSkill
{
    Charisma, Intelligence, Wisdom, Strength, Dexterity, Constitution;

    fun getSkill(): DNDSkill
    {
        return DNDSkill(this.name, this, this.ordinal)
    }
}

enum class DNDBaseAttribute
{
    Evasion,
    Learning,
    Hit,
    CombatStress,
    Interaction,
    Coordination,
    Tech,
    Athletics,
    Perception,
    Stealth,
    Reaction,
    Persuasion,
    Deception,
    Intimidation;

    fun getSkill(): DNDSkill
    {
        return DNDSkill(name.split(Regex("(?=[A-Z])")).toList().joinToString(" ").trim(), getBase(), ordinal + 6)
    }

    fun getBase(): DNDBaseSkill
    {
        return when (this)
        {
            Evasion -> DNDBaseSkill.Dexterity
            Learning -> DNDBaseSkill.Wisdom
            Hit -> DNDBaseSkill.Strength
            CombatStress -> DNDBaseSkill.Constitution
            Interaction -> DNDBaseSkill.Charisma
            Coordination -> DNDBaseSkill.Dexterity
            Tech -> DNDBaseSkill.Intelligence
            Athletics -> DNDBaseSkill.Strength
            Perception -> DNDBaseSkill.Wisdom
            Stealth -> DNDBaseSkill.Dexterity
            Reaction -> DNDBaseSkill.Dexterity
            Persuasion -> DNDBaseSkill.Charisma
            Deception -> DNDBaseSkill.Charisma
            Intimidation -> DNDBaseSkill.Charisma
        }
    }
}

enum class DNDDice(val sides: Int)
{
    d4(4),
    d6(6),
    d8(8),
    d10(10),
    d12(12),
    d20(20),
    d100(100);

    companion object
    {
        fun getFromSides(sides: Int): DNDDice?
            = when (sides)
                {
                    4 -> d4
                    6 -> d6
                    8 -> d8
                    10 -> d10
                    12 -> d12
                    20 -> d20
                    100 -> d100
                    else -> null
                }
    }
}

