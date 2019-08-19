package com.lapis.mycharacter.interfaces.screens

import com.lapis.mycharacter.DNDCharacter
import com.lapis.mycharacter.DNDValueGauge
import com.lapis.mycharacter.interfaces.CustomFxmlListCell
import com.lapis.mycharacter.interfaces.CustomListCellFactory
import javafx.fxml.FXML
import javafx.scene.Node
import javafx.scene.control.*
import java.net.URL

class GaugeCreationCell(val character: DNDCharacter): CustomFxmlListCell<DNDValueGauge>()
{
    @FXML lateinit var root: Node
    @FXML lateinit var gaugeName: TextField
    @FXML lateinit var gaugeMax: TextField
    @FXML lateinit var  gaugeAboveMax: CheckBox
    @FXML lateinit var removeButton: Button

    override fun getRootNode(): Node = root
    override fun getFxmlLocation(): URL = javaClass.getResource("/fxml/gauge_creation_cell.fxml")

    override fun onUpdateItem(item: DNDValueGauge)
    {
        if (character.getGauge(item) != null)
        {
            val gauge = character.getGauge(item)!!
            gaugeName.text = gauge.name
            gaugeMax.text = gauge.getMax().toString()
            gaugeAboveMax.isSelected = gauge.allowAboveMax
        }
        else
        {
            gaugeName.text = item.name
            gaugeMax.text = item.getMax().toString()
            gaugeAboveMax.isSelected = item.allowAboveMax
        }

        gaugeName.textProperty().addListener { observable, oldValue, newValue -> if (newValue.isNotEmpty()) item.name = newValue }
        gaugeMax.textProperty().addListener { observable, oldValue, newValue ->
            when
            {
                newValue.isBlank() -> item.setMax(1)
                newValue.toIntOrNull() != null -> { item.setMax(newValue.toInt()); item.setCurrent(newValue.toInt()) }
                else -> gaugeMax.text = oldValue
            }
        }
        gaugeAboveMax.selectedProperty().addListener { observable, oldValue, newValue -> item.allowAboveMax = newValue }

        removeButton.setOnAction {
            listView.items.remove(item)
            character.removeGauge(item)
        }
    }

    companion object
    {
        fun createFactory(char: DNDCharacter): CustomListCellFactory<DNDValueGauge>
        {
            return object: CustomListCellFactory<DNDValueGauge>()
            {
                override fun call(param: ListView<DNDValueGauge>?): ListCell<DNDValueGauge> = GaugeCreationCell(char)
            }
        }
    }
}