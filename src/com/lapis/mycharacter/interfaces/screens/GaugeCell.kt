package com.lapis.mycharacter.interfaces.screens

import com.lapis.mycharacter.DNDValueGauge
import com.lapis.mycharacter.interfaces.CustomFxmlListCell
import javafx.fxml.FXML
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.ProgressBar
import javafx.scene.layout.GridPane
import java.net.URL

class GaugeCell: CustomFxmlListCell<DNDValueGauge>()
{
    @FXML lateinit var root: Node
    @FXML lateinit var gaugeName: Label
    @FXML lateinit var gaugeBar: ProgressBar
    @FXML lateinit var gaugeAdd: Button
    @FXML lateinit var gaugeSub: Button

    override fun getRootNode(): Node = root
    override fun getFxmlLocation(): URL = javaClass.getResource("/fxml/gauge_cell.fxml")

    var onIncrement = { _ : DNDValueGauge -> }
    var onDecrement = { _ : DNDValueGauge -> }

    override fun onUpdateItem(item: DNDValueGauge)
    {
        gaugeBar.progress = item.getProgress()
        gaugeBar.prefWidthProperty().bind((root as GridPane).widthProperty())
        gaugeName.text = item.getStatus()

        GridPane.setFillWidth(gaugeAdd, true)
        GridPane.setFillWidth(gaugeSub, true)

        gaugeAdd.setOnAction {
            onIncrement(item)
        }
        gaugeSub.setOnAction {
            onDecrement(item)
        }
    }
}