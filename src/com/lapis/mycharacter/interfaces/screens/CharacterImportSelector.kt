package com.lapis.mycharacter.interfaces.screens

import com.lapis.mycharacter.CharacterLoader
import com.lapis.mycharacter.DNDCharacter
import com.lapis.mycharacter.interfaces.UIController
import com.lapis.mycharacter.interfaces.UserInfoDelegate
import javafx.fxml.FXML
import javafx.scene.control.Alert
import javafx.scene.control.Button
import javafx.scene.control.ListView
import javafx.scene.control.TextField
import javafx.stage.Stage
import java.io.File
import java.lang.Exception
import java.net.URL

class CharacterImportSelector(stage: Stage, val userInfo: UserInfoDelegate, val characterLoader: CharacterLoader): UIController(stage)
{
    @FXML lateinit var exportEntry: TextField
    @FXML lateinit var importButton: Button
    @FXML lateinit var characterPicker: ListView<DNDCharacter>

    override fun getFxmlLocation(): URL = javaClass.getResource("/fxml/character_import_selector.fxml")

    override fun onCreate()
    {
        super.onCreate()
        importButton.setOnAction {
            try
            {
                val result = DNDCharacter.fromJson(exportEntry.text)
                characterPicker.items.clear()
                characterPicker.items.addAll(result)
            }
            catch (e: Exception)
            {
                Alert(Alert.AlertType.ERROR).apply {
                    title = "Error"
                    contentText = e.localizedMessage
                }
                    .show()
            }
        }
        characterPicker.setOnMouseClicked {
            if (it.clickCount == 2)
            {
                characterPicker.selectionModel.selectedItem.also { character ->
                    userInfo.userInfo.writeCharacter(character)
                    characterLoader.loadCharacter(character)
                }
                closeToRoot()
            }
        }
    }
}