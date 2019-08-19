package com.lapis.mycharacter.interfaces.screens

import com.lapis.mycharacter.DNDBaseSkill
import com.lapis.mycharacter.DNDSkill
import com.lapis.mycharacter.interfaces.CustomFxmlListCell
import javafx.fxml.FXML
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.ComboBox
import javafx.scene.control.TextField
import java.net.URL

class SkillCreatorCell: CustomFxmlListCell<DNDSkill>()
{

    @FXML lateinit var root: Node
    @FXML lateinit var skillTextField: TextField
    @FXML lateinit var baseCombo: ComboBox<DNDBaseSkill>
    @FXML lateinit var removeButton: Button

    var onRemove: ((DNDSkill) -> Unit)? = null

    override fun onUpdateItem(item: DNDSkill)
    {
        if (item.name.isNotBlank()) skillTextField.text = item.name

        skillTextField.textProperty().addListener { _, _, newValue ->
            item.name = newValue
        }

        baseCombo.setOnAction {
            item.base = baseCombo.value
        }

        baseCombo.items.addAll(DNDBaseSkill.values())

        removeButton.setOnAction { onRemove?.invoke(this.item) }
    }

    override fun getRootNode(): Node
            = root

    override fun getFxmlLocation(): URL
            = javaClass.getResource("/fxml/skill_creation_cell.fxml")

    fun getSkill(): DNDSkill
        = this.item
}