package com.lapis.mycharacter.interfaces.screens

import com.lapis.mycharacter.DNDBaseSkill
import com.lapis.mycharacter.DNDSkill
import com.lapis.mycharacter.SkillSchema
import com.lapis.mycharacter.interfaces.CustomListCellFactory
import com.lapis.mycharacter.interfaces.UIController
import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.control.ListView
import javafx.scene.control.TextField
import javafx.stage.Stage
import java.net.URL

class SkillCreator(stage: Stage, val onSave: (SkillSchema) -> Unit): UIController(stage)
{
    override fun getFxmlLocation(): URL
        = this.javaClass.getResource("/fxml/skill_maker.fxml")

    @FXML lateinit var attribNameField: TextField
    @FXML lateinit var saveButton: Button
    @FXML lateinit var addAttrib: Button
    @FXML lateinit var attribList: ListView<DNDSkill>

    private val schema = SkillSchema("")

    override fun onCreate()
    {
        super.onCreate()

        attribNameField.textProperty().addListener { _, _, newValue ->
            schema.name = newValue
        }

        saveButton.setOnAction {
            onSave(schema)
            close()
        }

        addAttrib.setOnAction {
            attribList.items.add(DNDSkill("", DNDBaseSkill.Charisma).also { schema.add(it) })
        }

        attribList.cellFactory = CustomListCellFactory.createFactory(SkillCreatorCell().javaClass)
        {
            it.onRemove = { schema.remove(it) }
        }
    }

    fun getSchema(): SkillSchema
        = schema
}