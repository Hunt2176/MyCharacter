package com.lapis.mycharacter.interfaces.screens

import com.lapis.mycharacter.DNDCharacter
import com.lapis.mycharacter.interfaces.*
import com.lapis.mycharacter.util.DatabaseUser
import javafx.fxml.FXML
import javafx.scene.control.*
import javafx.stage.Stage
import java.net.URL

class UserSelector(stage: Stage?, val userInfo: UserInfoDelegate): UIController(stage)
{
    override fun getFxmlLocation(): URL = javaClass.getResource("/fxml/user_selector.fxml")

    @FXML lateinit var btnSelectUser: Button
    @FXML lateinit var listUsers: ListView<DatabaseUser>
    @FXML lateinit var listCharacters: ListView<DNDCharacter>

    private var selectedUser: DatabaseUser = userInfo.userInfo

    override fun onCreate() {
        super.onCreate()

        listUsers.cellFactory = CustomListCellFactory.createFactory(CustomListCell<DatabaseUser>().javaClass)
        {
            it.onCreate = { cell, item ->
                cell.text = item.userId
            }
        }

        listUsers.selectionModel.selectedItemProperty().addListener { _, _, new ->
            selectedUser = new
            btnSelectUser.isDisable = selectedUser == userInfo.userInfo
            listCharacters.items.setAll(selectedUser.characters)
        }

        listCharacters.cellFactory = CustomListCellFactory.createFactory(CustomListCell<DNDCharacter>().javaClass)
        {
            it.onCreate = { cell, item ->
                cell.text = item.name
            }

            it.addToContextMenu(Pair("Copy Character")
            {
                userInfo.writeCharacter(it.item)
                AlertBuilder(Alert.AlertType.INFORMATION)
                    .setHeader("Character Copy")
                    .setContextText("Copied ${it.item.name} to user ${userInfo.userId}")
                    .show()
            })
        }

        btnSelectUser.setOnAction {
            selectedUser.saveFile = userInfo.saveFile
            userInfo.userInfo = selectedUser
            userInfo.userInfo.saveToFile()
            closeToRoot()
        }

        DatabaseUser.getUserList { listUsers.items.setAll(it.toList()) }
    }
}