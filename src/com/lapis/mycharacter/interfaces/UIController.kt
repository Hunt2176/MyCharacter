package com.lapis.mycharacter.interfaces

import javafx.application.Platform
import javafx.scene.Scene
import javafx.fxml.FXMLLoader
import javafx.stage.Modality
import javafx.stage.Stage
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList

/**
 * Container class for JavaFX Stage. Has lifecycle events for the JavaFX Stage and Window.
 */
abstract class UIController(private var stage: Stage?): FXMLLoadable
{
    /**
     * Parent of this controller.
     */
    var parent: UIController? = null

    //Variable used to determine if FXML has been loaded
    private var loadedFXML = false

    private constructor(): this(Stage())

    override fun getLoader(): FXMLLoader = FXMLLoader(getFxmlLocation())

    open fun onCreate(){}
    open fun onStart(){}
    open fun onFocusChange(isFocused: Boolean){}
    open fun onHidden(){}
    open fun onClose(){}

    /**
     * Sets the title of the window
     * @param title - Title to set for the Window
     */
    fun setTitle(title: String)
    {
        stage?.title = title
    }

    /**
     * Shows the UIController
     */
    fun show() {
        if (!loadedFXML) loadFXML()
        stage?.show()
    }

    /**
     * Hides the UIController
     */
    fun hide()
    {
        stage?.hide()
    }

    /**
     * Closes the UIController
     */
    fun close()
    {
        stage?.close()
    }

    /**
     * Gets the backing stage for the UIController
     */
    fun getStage(): Stage? = this.stage

    /**
     * Returns the controller from the bottom of the parent hierarchy
     */
    fun getRootController(): UIController
    {
        var current = parent ?: return this
        while (current.parent != null)
        {
            current = current.parent!!
        }
        return current
    }

    /**
     * Returns a collection of controllers for the current controllers ancestors back to the root
     */
    fun getControllerStack(): ArrayList<UIController>
    {
        val list = arrayListOf(this)

        var current = this
        while (current.parent != null)
        {
            current = current.parent!!
            list.add(current)
        }

        return list
    }

    /**
     * Returns whether this controller is seen as the root of a hierarchy
     */
    fun isRoot(): Boolean
            = parent == null

    /**
     * Returns whether this controller is the ancestor of another controller
     */
    fun isAncestorOf(controller: UIController): Boolean
    {
        return getControllerStack().contains(controller)
    }

    /**
     * Closes all windows in the ancestor hierarchy until the passed controller is reached.
     * This method is exclusive and does not close the passed controller.
     */
    fun closeTo(controller: UIController)
    {
        close()
        var current: UIController? = this.parent ?: return

        while (current != null)
        {
            if (current == controller) return
            current.close()
            current = current.parent
        }
    }

    /**
     * Closes all controllers in the ancestor hierarchy until the root is reached.
     */
    fun closeToRoot()
    {
        close()
        var current = parent ?: return
        while (current.parent != null)
        {
            val previous = current
            current = current.parent!!
            previous.close()
        }
    }

    /**
     * Opens a new window, setting the passed controller as the child of the calling controller.
     *
     * @param controller - Child controller
     * @param holdFocus - Whether the new controller should hold focus until closed. Not allowing control of ancestors.
     */
    fun openNewWindow(controller: UIController, holdFocus: Boolean = false)
    {
        controller.parent = this
        controller.stage?.initOwner(stage?.owner)

        controller.loadFXML()
        if (holdFocus && !controller.isRoot())
        {
            controller.stage?.initModality(Modality.APPLICATION_MODAL)
            controller.stage?.show()
        }
        else controller.stage?.show()
    }

    /**
     * Calls UI Update events on the the JavaFX application thread.
     */
    fun runLater(toExecute: () -> Unit)
    {
        Platform.runLater { toExecute() }
    }

    /**
     * Sets the lifecycle methods for the controller.
     * Called by JavaFX Application.
     */
    fun initialize()
    {
        stage?.setOnShown {
            try {
                onStart()
            } catch (e: Exception){}
        }
        stage?.setOnHidden {
            try {
                onHidden()
            } catch (e: Exception){}
        }
        stage?.setOnCloseRequest {
            try {
                onClose()
            } catch (e: Exception){}
        }
        stage?.focusedProperty()?.addListener { observable, oldValue, newValue ->
            try {
                onFocusChange(newValue)
            } catch (e: Exception){}
        }
        try {
            onCreate()
        } catch (e: Exception){}
    }

    override fun loadFXML()
    {
        val loader = getLoader()
        loader.setController(this)

        stage?.scene = Scene(loader.load())
        loadedFXML = true
    }

    companion object
    {
        fun <T: UIController> load(toLoad: T): T
        {
            toLoad.loadFXML()
            return toLoad
        }
    }
}