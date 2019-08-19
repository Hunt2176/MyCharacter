package com.lapis.mycharacter.interfaces.screens

import com.lapis.mycharacter.interfaces.DiceHistoryDelegate
import com.lapis.mycharacter.interfaces.DiceRoll
import com.lapis.mycharacter.interfaces.UIController
import javafx.fxml.FXML
import javafx.scene.control.ListView
import javafx.stage.Stage
import java.net.URL

class DiceHistoryController(stage: Stage?, private val delegate: DiceHistoryDelegate): UIController(stage)
{
    @FXML lateinit var historyList: ListView<DiceRoll>

    override fun getFxmlLocation(): URL = javaClass.getResource("/fxml/dice_history.fxml")

    override fun onCreate()
    {
        setTitle("Dice History")
        historyList.items.addAll(delegate.diceHistory)
    }

    override fun onClose()
    {
        delegate.diceHistoryController = null
    }

    fun addRoll(roll: DiceRoll)
    {
        historyList.items.add(0, roll)
    }
}