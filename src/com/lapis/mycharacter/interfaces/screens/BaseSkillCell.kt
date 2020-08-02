package com.lapis.mycharacter.interfaces.screens

import com.lapis.mycharacter.DNDCharacter
import com.lapis.mycharacter.DNDDice
import com.lapis.mycharacter.DNDSkill
import com.lapis.mycharacter.interfaces.CustomFxmlListCell
import com.lapis.mycharacter.interfaces.CustomListCell
import com.lapis.mycharacter.interfaces.CustomListCellFactory
import javafx.fxml.FXML
import javafx.scene.Node
import javafx.scene.control.*
import java.net.URL

class BaseSkillCell(val character: DNDCharacter): CustomFxmlListCell<DNDSkill>()
{
    @FXML lateinit var root: Node
    @FXML lateinit var skillName: Label
    @FXML lateinit var skillLevel: TextField
    @FXML lateinit var skillDice: ChoiceBox<DNDDice?>

    override fun onUpdateItem(item: DNDSkill)
    {
        skillName.text = item.name
        if (character.getSkillLevel(item) > 0) skillLevel.text = character.getSkillLevel(item).toString()

        skillLevel.textProperty().addListener { observable, oldValue, newValue ->
            when
            {
                (newValue.isBlank()) -> character.setLevel(1, item)
                (newValue.toIntOrNull() != null) -> character.setLevel(newValue.toInt(), item)
                else -> skillLevel.text = oldValue
            }
        }

        skillDice.selectionModel.selectedItemProperty().addListener { _, _, newValue -> item.die = newValue }

        val dice: ArrayList<DNDDice?> = arrayListOf(null)
        dice.addAll(DNDDice.values().toList())
        skillDice.items.addAll(dice)
        skillDice.selectionModel.select(item.die)
    }

    override fun getRootNode(): Node = root
    override fun getFxmlLocation(): URL = javaClass.getResource("/fxml/base_skill_cell.fxml")

    companion object
    {
        fun createFactory(character: DNDCharacter): CustomListCellFactory<DNDSkill>
        {
            return object: CustomListCellFactory<DNDSkill>()
            {
                override fun call(param: ListView<DNDSkill>?): ListCell<DNDSkill> = BaseSkillCell(character)
            }
        }
    }
}