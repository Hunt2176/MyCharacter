package com.lapis.mycharacter.interfaces.screens

import com.lapis.mycharacter.DNDCharacter
import com.lapis.mycharacter.DNDSkill
import com.lapis.mycharacter.interfaces.CustomFxmlListCell
import com.lapis.mycharacter.interfaces.CustomListCellFactory
import com.lapis.mycharacter.util.ifNotNull
import javafx.fxml.FXML
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.ComboBox
import javafx.scene.control.ListCell
import javafx.scene.control.ListView
import java.net.URL

class ProficiencyCreationCell(val char: DNDCharacter): CustomFxmlListCell<DNDSkill>()
{
    @FXML lateinit var root: Node
    @FXML lateinit var profSelCombo: ComboBox<DNDSkill>
    @FXML lateinit var removeButton: Button

    override fun getRootNode(): Node = root
    override fun getFxmlLocation(): URL = javaClass.getResource("/fxml/char_create_prof_cell.fxml")

    override fun onUpdateItem(item: DNDSkill)
    {
        profSelCombo.items.addAll(char.getSkills().getSkills()
            .toMutableList().apply { removeIf { (0..5).contains(it.skillId) }})

        if (index < char.getProficiencies().size)
        {
            profSelCombo.selectionModel.select(char.getProficiencies()[index])
        }

        profSelCombo.valueProperty().addListener { _, oldValue, newValue ->
            if (oldValue != null) char.removeProficiency(oldValue)
            if (newValue != null) char.addProficiency(newValue)
        }

        removeButton.setOnAction {
            profSelCombo.selectionModel.selectedItem.ifNotNull { char.removeProficiency(it) }
            listView.items.removeAt(index)
        }
    }

    companion object
    {
        fun createFactory(char: DNDCharacter): CustomListCellFactory<DNDSkill>
        {
            return object: CustomListCellFactory<DNDSkill>()
            {
                override fun call(param: ListView<DNDSkill>?): ListCell<DNDSkill> = ProficiencyCreationCell(char)
            }
        }
    }
}