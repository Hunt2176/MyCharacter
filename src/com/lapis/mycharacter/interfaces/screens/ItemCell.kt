package com.lapis.mycharacter.interfaces.screens

import com.lapis.mycharacter.DNDItem
import com.lapis.mycharacter.interfaces.CustomFxmlListCell
import javafx.fxml.FXML
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.Tooltip
import java.net.URL

class ItemCell: CustomFxmlListCell<DNDItem>()
{
    @FXML lateinit var root: Node

    @FXML lateinit var itemQuantButton: Button
    @FXML lateinit var itemName: Label

    override fun onUpdateItem(item: DNDItem)
    {
        itemName.text = item.name
        itemQuantButton.text = item.quantity.toString()
        if (!item.description.isBlank()) tooltip = Tooltip(item.description)
    }

    override fun getRootNode(): Node = root
    override fun getFxmlLocation(): URL = javaClass.getResource("/fxml/item_cell.fxml")
}