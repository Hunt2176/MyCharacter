package com.lapis.mycharacter.interfaces

import com.lapis.mycharacter.util.ifNotNull
import javafx.fxml.FXMLLoader
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.control.*
import javafx.util.Callback

abstract class CustomFxmlListCell<T>: CustomListCell<T>(), FXMLLoadable
{
    abstract fun onUpdateItem(item: T)
    abstract fun getRootNode(): Node

    override fun getLoader(): FXMLLoader = FXMLLoader(getFxmlLocation())
    override fun loadFXML()
    {
        val loader = getLoader()
        loader.setController(this)
        loader.load<Parent>()
    }

    override fun updateItem(item: T, empty: Boolean)
    {
        onCreate = { _, _ ->
            loadFXML()
            graphic = getRootNode()
            onUpdateItem(item)
        }
        super.updateItem(item, empty)
    }
}

open class CustomListCell<T>: ListCell<T>()
{
    open var onCreate: ((CustomListCell<T>, T) -> Unit)? = null

    fun addToContextMenu(vararg items: MenuItem)
    {
        contextMenu = ContextMenu()
        contextMenu.items.addAll(items)
    }

    fun addToContextMenu(vararg items: Pair<String, () -> Unit>)
    {
        contextMenu = ContextMenu()
        contextMenu.items.addAll(
            items.map { item -> MenuItem(item.first).apply { setOnAction { item.second.invoke() } } }
        )
    }

    override fun updateItem(item: T, empty: Boolean)
    {
        super.updateItem(item, empty)

        if (empty || item == null)
        {
            tooltip = null
            isDisable = true
            text = ""
            graphic = null
        }
        else
        {
            isDisable = false
            onCreate?.invoke(this, item)
        }
    }
}

abstract class CustomListCellFactory<T>: Callback<ListView<T>, ListCell<T>>
{
    companion object
    {
        /**
         * Creates a ListCellFactory that creates the type passed into it and applies customizations
         * on each cell.
         * @param clazz - ListCell child class to generate from
         * @param customizer - Lambda called on creation of ListCells with newly generated cells as parameter
         */
        inline fun <T, reified E : ListCell<T>> createFactory(
            clazz: Class<E>,
            crossinline customizer: (E) -> Unit = { _ -> }): CustomListCellFactory<T>
        {
            return object : CustomListCellFactory<T>()
            {
                override fun call(param: ListView<T>?): ListCell<T> = clazz.newInstance().apply(customizer) }
            }
        }
}





