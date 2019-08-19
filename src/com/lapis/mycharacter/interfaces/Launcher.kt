package com.lapis.mycharacter.interfaces

import javafx.application.Application
import javafx.stage.Stage

/**
 * Launcher for JavaFX applications based on the UIController
 */
class Launcher: Application()
{
    /**
     * This method should only be called by the JavaFX Application
     */
    override fun start(primaryStage: Stage?)
    {
        rootConstructor?.invoke(primaryStage)?.show()
    }

    companion object
    {
        private var rootConstructor: ((Stage?) -> UIController)? = null

        /**
         * Constructs the root of the UIController hierarchy and starts the JavaFX Application
         * @param constructor - Function called to generate the root controller
         */
        fun start(constructor: (Stage?) -> UIController)
        {
            rootConstructor = constructor
            launch(Launcher::class.java)
        }
    }
}