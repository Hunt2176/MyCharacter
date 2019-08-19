package com.lapis.mycharacter.interfaces.screens

import com.lapis.mycharacter.DNDItem
import com.lapis.mycharacter.interfaces.ItemControlDelegate
import com.lapis.mycharacter.interfaces.UIController
import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.control.TextArea
import javafx.scene.control.TextField
import javafx.stage.Stage
import java.net.URL

class EditItemScreen(stage: Stage?, val item: DNDItem, val itemDelegate: ItemControlDelegate): UIController(stage)
{
    @FXML lateinit var itemName: TextField
    @FXML lateinit var itemDesc: TextArea
    @FXML lateinit var itemSave: Button

    override fun getFxmlLocation(): URL = javaClass.getResource("/fxml/edit_item_screen.fxml")

    override fun onCreate()
    {
        super.onCreate()

        if (item.name == "") setTitle("Item Creation")
        else setTitle("Editing ${item.name}")

        if (!item.name.isBlank()) itemName.text = item.name
        if (!item.description.isBlank()) itemDesc.text = item.description

        itemName.textProperty().addListener { _, _, newValue -> if (!newValue.isNullOrEmpty()) item.name = newValue }
        itemDesc.textProperty().addListener { _, _, newValue -> if (!newValue.isNullOrEmpty()) item.description = newValue }
        itemSave.setOnAction { close(); itemDelegate.updateItem(item) }

        itemName.requestFocus()
    }

    companion object
    {
        fun creatorWindow(delegate: ItemControlDelegate): EditItemScreen
        {
            return EditItemScreen(Stage(), DNDItem("", ""), delegate)
        }

        fun editItem(item: DNDItem, delegate: ItemControlDelegate): EditItemScreen
        {
            return EditItemScreen(Stage(), item, delegate)
        }
    }
}