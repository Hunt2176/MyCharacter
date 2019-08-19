package com.lapis.mycharacter.interfaces.screens

import com.lapis.mycharacter.DNDSkill
import com.lapis.mycharacter.interfaces.*
import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.stage.Stage
import javafx.util.Callback
import java.lang.Math.abs
import java.net.URL
import java.util.concurrent.ThreadLocalRandom

class DiceRoller(stage: Stage, val name: String, private var diceToRoll: Int, val bonus: Int): UIController(stage)
{
    @FXML lateinit var rerollButton: Button
    @FXML lateinit var attribName: Label
    @FXML lateinit var rollResult: Label
    @FXML lateinit var resultBox: Label

    private var rolls = 0
    var historyDelegate: DiceHistoryDelegate? = null
    var completionCallback: CompletionCallBack<DiceRoller, Unit>? = null

    override fun getFxmlLocation(): URL = javaClass.getResource("/fxml/dice_roller.fxml")

    override fun onCreate()
    {
        super.onCreate()
        diceToRoll += 1
        getStage()?.isResizable = false

        attribName.text = name
        roll()
    }

    fun roll()
    {
        val roll = ThreadLocalRandom.current().nextInt(1, diceToRoll)
        val result = roll + bonus

        resultBox.text = "Rolled: $roll\n${if (bonus >= 0) "+$bonus" else "-${abs(bonus)}"}"
        rollResult.text = result.toString()

        rerollButton.text = if (rolls == 0) "Reroll" else "Reroll($rolls)"
        rerollButton.setOnAction {
            rolls += 1
            roll()
        }

        historyDelegate?.addToHistory(DiceRoll(getStage()?.title ?: attribName.text, result))
    }

    override fun onClose()
    {
        super.onClose()
        completionCallback?.onComplete?.invoke(this)
    }
}