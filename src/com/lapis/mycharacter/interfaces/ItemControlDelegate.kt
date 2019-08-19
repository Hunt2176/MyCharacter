package com.lapis.mycharacter.interfaces

import com.lapis.mycharacter.DNDCharacter
import com.lapis.mycharacter.DNDItem
import com.lapis.mycharacter.interfaces.screens.EditItemScreen
import com.lapis.mycharacter.util.firstIndexWhereOrNull
import java.io.File

interface ItemControlDelegate
{
    var character: DNDCharacter
    var saveFile: File

    fun updateItem(item: DNDItem)
    {
        val index = character.items.firstIndexWhereOrNull { it.itemId == item.itemId }
        if (index != null)
        {
            character.items.removeAt(index)
            character.items.add(index, item)
        }
        else
        {
            character.items.add(item)
        }
        onItemUpdate(item)
    }

    fun onItemUpdate(item: DNDItem)

    fun createEditItemScreen(item: DNDItem): EditItemScreen = EditItemScreen.editItem(item, this)
    fun createAddItemScreen(): EditItemScreen = EditItemScreen.creatorWindow(this)
}