package com.lapis.mycharacter.interfaces.screens

import com.lapis.mycharacter.CharacterLoader
import com.lapis.mycharacter.DNDCharacter
import com.lapis.mycharacter.interfaces.*
import javafx.fxml.FXML
import javafx.scene.control.*
import javafx.stage.Stage
import java.net.URL

class CharacterManager(stage: Stage, private val loader: CharacterLoader,
                       private val userInfoDelegate: UserInfoDelegate,
                       private val closeTo: UIController): UIController(stage)
{
    @FXML lateinit var fileField: TextField
    @FXML lateinit var browseButton: Button
    @FXML lateinit var characterPicker: ListView<DNDCharacter>

    override fun getFxmlLocation(): URL = javaClass.getResource("/fxml/character_file_selector.fxml")

    override fun onCreate()
    {
        super.onCreate()
        setTitle("Manage Characters")

        characterPicker.cellFactory = CustomListCellFactory.createFactory(CustomListCell<DNDCharacter>().javaClass)
        {
            it.onCreate = { cell, item ->
                cell.text = item.toString()
                cell.addToContextMenu(
                    Pair("Edit")
                    {
                        val creator = CharacterCreator(Stage(), userInfoDelegate)
                        getRootController().openNewWindow(creator, true)
                        creator.loadCharacter(item)
                        closeToRoot()
                    },
                    Pair("Delete") {
                    runLater {
                        AlertBuilder(Alert.AlertType.CONFIRMATION)
                            .setTitle("Delete Character")
                            .setHeader("Delete ${item.name}")
                            .setContextText("Would you like to remove this character?\n\nThis will only remove locally." +
                                    " To remove from remote you will need to push changes.")
                            .setButtonAction(ButtonType.OK)
                            {
                                characterPicker.items.remove(item)
                                userInfoDelegate.removeCharacter(item)
                            }
                            .show()
                    }
                    })
            }
        }

        fileField.text = userInfoDelegate.saveFile.absolutePath
        fileField.isEditable = false

        browseButton.isDisable = true

        characterPicker.setOnMouseClicked {
            if (it.clickCount == 2)
            {
                loadCharacter(characterPicker.selectionModel.selectedItem)
            }
        }

        loadFromUserInfo()
    }

    fun loadFromUserInfo()
    {
        characterPicker.items.addAll(userInfoDelegate.userInfo.characters)
    }

    fun loadCharacter(character: DNDCharacter)
    {
        loader.loadCharacter(character)
        closeTo(closeTo)
    }
}