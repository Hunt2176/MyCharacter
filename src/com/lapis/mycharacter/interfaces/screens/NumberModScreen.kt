package com.lapis.mycharacter.interfaces.screens

import com.lapis.mycharacter.interfaces.UIController
import javafx.fxml.FXML
import javafx.scene.control.*
import javafx.stage.Stage
import java.net.URL

class NumberModScreen(stage: Stage, private val title: String, private val valueToMod: Int,
                      val changeListener: (Int) -> Unit): UIController(stage)
{
    @FXML lateinit var titleLabel: Label
    @FXML lateinit var currentValueLabel: Label
    @FXML lateinit var valueField: TextField
    @FXML lateinit var saveButton: Button

    private var newValue = 0

    override fun getFxmlLocation(): URL
        = this.javaClass.getResource("/fxml/number_mod.fxml")

    override fun onCreate()
    {
        super.onCreate()
        titleLabel.text = title
        currentValueLabel.text = "Current Value: $valueToMod"

        valueField.textProperty().addListener { observable, oldValue, newValue ->
            when
                {
                    newValue.isEmpty() -> this.newValue = 0
                    newValue == "-" -> { this.newValue = -1 }
                    newValue.toIntOrNull() == null ->
                    {
                        valueField.text = oldValue.toIntOrNull()?.toString() ?: ""
                    }
                    else -> this.newValue = newValue.toInt()
                }
        }

        saveButton.setOnAction {
            changeListener(newValue)
            closeToRoot()
        }
    }
}

