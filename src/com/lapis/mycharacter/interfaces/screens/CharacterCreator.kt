package com.lapis.mycharacter.interfaces.screens

import com.lapis.mycharacter.*
import com.lapis.mycharacter.interfaces.AlertBuilder
import com.lapis.mycharacter.interfaces.UIController
import com.lapis.mycharacter.interfaces.UserInfoDelegate
import com.lapis.mycharacter.util.containsMatch
import com.lapis.mycharacter.util.firstIndexWhereOrNull
import com.lapis.mycharacter.util.ifNotNull
import javafx.fxml.FXML
import javafx.scene.control.*
import javafx.stage.Stage
import java.io.File
import java.net.URL

class CharacterCreator(stage: Stage, val userInfoDelegate: UserInfoDelegate): UIController(stage), CharacterLoader
{
    @FXML lateinit var charName: TextField
    @FXML lateinit var charAC: TextField
    @FXML lateinit var saveButton: Button
    @FXML lateinit var loadButton: Button
    @FXML lateinit var diceTypeCombo: ComboBox<DNDDice>

    @FXML lateinit var usesBaseSkillsCheckBox: CheckBox
    @FXML lateinit var baseSkillList: ListView<DNDSkill>

    @FXML lateinit var addProfButton: Button
    @FXML lateinit var profBonus: TextField
    @FXML lateinit var skillSelectCombo: ComboBox<SkillSchema>
    @FXML lateinit var profList: ListView<DNDSkill>
    @FXML lateinit var createSetButton: Button

    @FXML lateinit var addGaugeButton: Button
    @FXML lateinit var gaugeList: ListView<DNDValueGauge>

    override var character: DNDCharacter = DNDCharacter(SkillSchema.defaultSkillSchema())

    override fun getFxmlLocation(): URL = javaClass.getResource("/fxml/character_create.fxml")

    override fun onCreate()
    {
        setTitle("Character Creator")

        loadButton.setOnAction {
            openNewWindow(CharacterManager(Stage(), this, userInfoDelegate, this))
        }

        diceTypeCombo.items.addAll(DNDDice.values())
        DNDDice.getFromSides(character.getDiceSides()).ifNotNull { diceTypeCombo.selectionModel.select(it.ordinal) }

        diceTypeCombo.setOnAction {
            character.setDice(diceTypeCombo.selectionModel.selectedItem)
        }

        saveButton.setOnAction {
            userInfoDelegate.userInfo.apply {
                if (charName.text.isNotBlank())
                {
                    writeCharacter(character)
                    closeToRoot()
                }
                else AlertBuilder(Alert.AlertType.ERROR)
                    .setTitle("Error")
                    .setHeader("Blank Character Name")
                    .setContextText("A character must have a name to be saved.")
                    .show()
            }
        }

        usesBaseSkillsCheckBox.setOnAction {
            character.getSkills().usesBaseAsSkills = usesBaseSkillsCheckBox.isSelected
        }

        charName.textProperty().addListener { _, _, newValue ->
            character.name = newValue
        }

        charAC.textProperty().addListener { _, oldValue, newValue ->
            when
            {
                newValue.isEmpty() -> character.armourClass = 0
                newValue.toIntOrNull() == null -> charAC.text = oldValue
                else -> character.armourClass = newValue.toInt()
            }
        }
        profBonus.textProperty().addListener { _, oldValue, newValue ->
            when
            {
                newValue.isEmpty() -> character.proficiencyBonus = 0
                newValue.toIntOrNull() == null -> profBonus.text = oldValue
                else -> character.proficiencyBonus = newValue.toInt()
            }
        }

        createSetButton.setOnAction {
            openNewWindow(SkillCreator(Stage())
            {
                skillSelectCombo.items.add(it)
            }, true)
        }

        addGaugeButton.setOnAction {
            val gauge = DNDValueGauge("", 1, 1)
            gaugeList.items.add(gauge)
            character.addGauge(gauge)
        }

        addProfButton.setOnAction {
            profList.items.add(character.getSkills().getSkills().first())
        }

        skillSelectCombo.items.add(SkillSchema.defaultSkillSchema())
        skillSelectCombo.valueProperty().addListener { _, _, newValue ->
            if (newValue != null)
            {
                character.setSkillSchema(newValue)
                character.getProficiencies().clear()
                profList.items.clear()
            }
        }

        loadCharacter(character)
    }

    override fun loadCharacter(character: DNDCharacter)
    {
        super.loadCharacter(character)

        baseSkillList.items.clear()
        gaugeList.items.clear()
        profList.items.clear()

        character.getSkills().schemaId
            .ifNotNull { id ->
            skillSelectCombo.items.firstIndexWhereOrNull { it.schemaId == id }
                .ifNotNull {
                skillSelectCombo.selectionModel.select(it)
            }
        }

        DNDDice.getFromSides(character.getDiceSides()).ifNotNull { diceTypeCombo.selectionModel.select(it.ordinal) }

        if (!skillSelectCombo.items.containsMatch { it.schemaId == character.getSkills().schemaId })
            skillSelectCombo.items.add(character.getSkills())

        skillSelectCombo.items.firstIndexWhereOrNull { it.schemaId == character.getSkills().schemaId }
            .ifNotNull { skillSelectCombo.selectionModel.select(it) }


        baseSkillList.cellFactory = BaseSkillCell.createFactory(character)
        gaugeList.cellFactory = GaugeCreationCell.createFactory(character)
        profList.cellFactory = ProficiencyCreationCell.createFactory(character)

        if (character.name.isNotBlank()) charName.text = character.name
        if (character.armourClass > 0) charAC.text = character.armourClass.toString()
        if (character.proficiencyBonus > 0) profBonus.text = character.proficiencyBonus.toString()

        usesBaseSkillsCheckBox.isSelected = character.getSkills().usesBaseAsSkills

        baseSkillList.items.addAll(character.getSkills().getSkills().filter { it.base.getSkill().skillId == it.skillId })
        gaugeList.items.addAll(character.getGauges())
        profList.items.addAll(character.getProficiencies())
    }
}