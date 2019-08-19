package com.lapis.mycharacter.interfaces

import javafx.scene.control.Alert
import javafx.scene.control.ButtonType

/**
 * Builder Style for JavaFX Alerts
 */
class AlertBuilder(alertType: Alert.AlertType)
{
    private val alert = Alert(alertType)
    private val buttonMap = mutableMapOf<ButtonType, () -> Unit>()

    /**
     * Sets the title of the alert
     * @param string - Title to be set
     */
    fun setTitle(string: String): AlertBuilder
    {
        alert.title = string
        return this
    }

    /**
     * Sets the header text of the alert
     * @param string - Header to be set
     */
    fun setHeader(string: String): AlertBuilder
    {
        alert.headerText = string
        return this
    }

    /**
     * Sets the message text of the alert
     * @param string - Message body of the alert to be set
     */
    fun setContextText(string: String): AlertBuilder
    {
        alert.contentText = string
        return this
    }

    /**
     * Sets the action of the button to be triggered when pressed.
     * Will add the button to the alert if the button is not already present.
     *
     * @param buttonType - ButtonType to correspond the action with
     * @param action - Action to be triggered by this button event
     */
    fun setButtonAction(buttonType: ButtonType, action: () -> Unit): AlertBuilder
    {
        buttonMap[buttonType] = action
        if (!alert.buttonTypes.contains(buttonType))
        {
            alert.buttonTypes.add(buttonType)
        }
        return this
    }

    /**
     * Removes the button from the alert
     * @param buttonType - ButtonType to be removed from the alert
     */
    fun removeButton(buttonType: ButtonType): AlertBuilder
    {
        alert.buttonTypes.remove(buttonType)
        return this
    }

    /**
     * Shows the alert and waits until an action is taken on the alert.
     * Will execute stored button actions if one was pressed.
     */
    fun show()
    {
        val result = alert.showAndWait()
        result.ifPresent { buttonMap[it]?.invoke() }
    }

}