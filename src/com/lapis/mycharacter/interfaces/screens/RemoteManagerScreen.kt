package com.lapis.mycharacter.interfaces.screens

import com.lapis.mycharacter.interfaces.AlertBuilder
import com.lapis.mycharacter.interfaces.UIController
import com.lapis.mycharacter.interfaces.UserInfoDelegate
import com.lapis.mycharacter.util.AsyncDispatchQueue
import com.lapis.mycharacter.util.ConnectionResult
import javafx.fxml.FXML
import javafx.scene.control.Alert
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.TextField
import javafx.stage.Stage
import java.net.URL
import java.time.Month

class RemoteManagerScreen(stage: Stage, val userInfoDelegate: UserInfoDelegate): UIController(stage)
{
    @FXML lateinit var userIdInput: TextField
    @FXML lateinit var updateUserId: Button
    @FXML lateinit var labelLastSync: Label

    override fun getFxmlLocation(): URL
        = this.javaClass.getResource("/fxml/remote_manager.fxml")

    override fun onCreate()
    {
        super.onCreate()
        setTitle("Manage User Data")

        updateDetails()
        updateUserId.setOnAction {
            if (userInfoDelegate.userInfo.userId != userIdInput.text)
            {
                AsyncDispatchQueue { handler ->
                    (userInfoDelegate.userInfo.setUserId(userIdInput.text)
                    { result ->
                        if (!result.isSuccess())
                        {
                            handler.halt(true)
                            if (result == ConnectionResult.Fail) runLater {
                                AlertBuilder(Alert.AlertType.ERROR)
                                    .setTitle("Error")
                                    .setHeader("Failed to Connect")
                                    .setContextText("Failed to connected to remote to update User ID")
                                    .show()
                            }
                            else if (result == ConnectionResult.Error) runLater {
                                AlertBuilder(Alert.AlertType.ERROR)
                                    .setTitle("Error")
                                    .setHeader(result.message)
                                    .show()
                            }
                        }
                        updateDetails()
                    })
                }
                    .start()
            }
        }
    }

    fun updateDetails()
    {
        runLater {
            userIdInput.text = userInfoDelegate.userId
            labelLastSync.text = "Last Sync: " +
                    "${userInfoDelegate.syncDate.month} ${userInfoDelegate.syncDate.dayOfMonth}  -  " +
                    "${userInfoDelegate.syncDate.hour}:" +
                    "${userInfoDelegate.syncDate.minute}:" +
                    "${userInfoDelegate.syncDate.second}"

            parent?.setTitle("My Character - User: ${userInfoDelegate.userInfo.userId}")
        }
    }

}