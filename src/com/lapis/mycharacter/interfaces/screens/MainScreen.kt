package com.lapis.mycharacter.interfaces.screens

import com.lapis.mycharacter.*
import com.lapis.mycharacter.interfaces.*
import com.lapis.mycharacter.util.*
import javafx.fxml.FXML
import javafx.scene.control.*
import javafx.stage.Stage
import java.io.File
import java.net.URL

class MainScreen(stage: Stage?): UIController(stage), CharacterLoader, DiceHistoryDelegate, ItemControlDelegate, UserInfoDelegate
{
    override var character: DNDCharacter = DNDCharacter(0).apply { getSkills().usesBaseAsSkills = false }
    override var diceHistoryController: DiceHistoryController? = null
    override val diceHistory: ArrayList<DiceRoll> = arrayListOf()

    override var saveFile: File = File("characters")
    override var userInfo = DatabaseUser.createOrRead(saveFile)
    {
        AlertBuilder(Alert.AlertType.ERROR)
            .setTitle("Save File Error")
            .setHeader("Unable to read local save file")
            .setContextText("The save file at \"${saveFile.absolutePath}\" could not be read." +
                    "\nThe file has been moved to \"${it.absolutePath}\" and a new user file has been created.")
            .show()
    }

    val rollerWindows = mutableMapOf<String, DiceRoller>()
    val attribMods = mutableMapOf<Int, MutableMap<DNDSkill, Int>>()

    @FXML lateinit var createCharacterMenu: MenuItem
    @FXML lateinit var manageCharacter: MenuItem
    @FXML lateinit var loadExport: MenuItem
    @FXML lateinit var diceHistoryMenu: MenuItem
    @FXML lateinit var rollInitiative: MenuItem
    @FXML lateinit var manageRemote: MenuItem
    @FXML lateinit var menuDatabasePush: MenuItem
    @FXML lateinit var menuDatabasePull: MenuItem

    @FXML lateinit var rollMenu: Menu

    @FXML lateinit var attributeList: ListView<DNDSkill>
    @FXML lateinit var gaugeList: ListView<DNDValueGauge>
    @FXML lateinit var itemList: ListView<DNDItem>

    @FXML lateinit var itemAdd: Button
    @FXML lateinit var quickSelectChoice: ChoiceBox<DNDCharacter>
    @FXML lateinit var sortSkillsChoice: ChoiceBox<SortChoice>

    @FXML lateinit var nameLabel: Label

    override fun getFxmlLocation(): URL = javaClass.getResource("/fxml/main_screen.fxml")

    override fun onCreate()
    {
        setTitle("My Character - User: ${userInfo.userId}")

        rollMenu.items.addAll(
            DNDDice.values().map { MenuItem(it.name.capitalize()).apply {

                val title = "D${it.sides}"
                setOnAction { _ ->
                    if (!rollerWindows.containsKey(title))
                        rollerWindows[title] = DiceRoller(Stage(), title, it.sides, 0)
                            .apply {
                                setTitle(title)
                                historyDelegate = this@MainScreen
                                completionCallback = CompletionCallBack { rollerWindows.remove(name) }
                            }
                            .also { openNewWindow(it) }

                    rollerWindows[title]?.getStage()?.toFront()
                }
            } }
        )

        nameLabel.text = "${character.name} - AC: ${character.armourClass}"
        createCharacterMenu.setOnAction {
            val screen = CharacterCreator(Stage(), this)
            openNewWindow(load(screen), true)
        }

        manageCharacter.setOnAction {
            openNewWindow(CharacterManager(Stage(), this, this, this), true)
        }

        loadExport.setOnAction {
            openNewWindow(CharacterImportSelector(Stage(), this, this), true)
        }

        manageRemote.setOnAction {
            openNewWindow(RemoteManagerScreen(Stage(), this), true)
        }

        menuDatabasePush.setOnAction {
            AlertBuilder(Alert.AlertType.CONFIRMATION)
                .setTitle("Confirm Push")
                .setHeader("Overwriting Data")
                .setContextText("Pushing character information can possibly overwrite what has been stored remotely.\n\n" +
                        "Would you like to continue?")
                .setButtonAction(ButtonType.OK)
                {
                    userInfo.push {
                        if (!it.isSuccess())
                        { runLater {
                            AlertBuilder(Alert.AlertType.ERROR)
                                .setTitle("Error")
                                .setHeader("Failure to Push")
                                .setContextText("Unable to save information to remote")
                                .show()
                        } }
                    }
                }
                .show()
        }

        menuDatabasePull.setOnAction {
            userInfo.pull { connectionResult, _ ->
                if (connectionResult.isSuccess()) runLater { loadQuickSelectMenu() }
                else { runLater {
                        AlertBuilder(Alert.AlertType.ERROR)
                            .setTitle("Error")
                            .setHeader("Failure to Pull")
                            .setContextText("Unable to load new information.")
                            .show()
                    }
                }
            }
        }

        sortSkillsChoice.items.addAll(SortChoice.values())
        sortSkillsChoice.setOnAction {
            sortAttributes(sortSkillsChoice.selectionModel.selectedItem)
        }
        sortSkillsChoice.selectionModel.select(0)

        gaugeList.cellFactory = CustomListCellFactory.createFactory(GaugeCell().javaClass)
        { cell ->
            cell.apply {
                onIncrement = {
                    it.increment()
                    cell.gaugeName.text = it.getStatus()
                    cell.gaugeBar.progress = it.getProgress()
                    userInfo.writeCharacter(character)
                }
                onDecrement = {
                    it.decrement()
                    cell.gaugeName.text = it.getStatus()
                    cell.gaugeBar.progress = it.getProgress()
                    userInfo.writeCharacter(character)
                }
            }
        }

        attributeList.cellFactory = CustomListCellFactory.createFactory(CustomListCell<DNDSkill>().javaClass)
        { cell ->
            cell.onCreate = { customListCell, skill ->
                val hasMod = attribMods[character.characterId]?.containsKey(skill) ?: false
                val plusVal = attribMods[character.characterId]?.get(skill) ?: character.getSkillPlus(skill)
                customListCell.text = "$skill ${if (plusVal >= 0) "+" else ""}$plusVal " +
                        if (hasMod) "- Temporary" else ""
            }

            cell.addToContextMenu(
                MenuItem("Modify Bonus").apply {
                    setOnAction {
                        val currentMod = attribMods[character.characterId]?.get(cell.item) ?: 0
                        val modScreen = NumberModScreen(Stage(), "${cell.item}", currentMod)
                        { newValue ->
                            attribMods[character.characterId]?.set(cell.item, newValue)
                        }
                        modScreen.setTitle("Set Skill Bonus")

                        openNewWindow(modScreen, true)
                    }
                },
                MenuItem("Clear").apply {
                    setOnAction {
                        attribMods[character.characterId]?.remove(cell.item)
                        cell.onCreate?.invoke(cell, cell.item)
                    }
                }
            )
        }

        itemList.cellFactory = CustomListCellFactory.createFactory(ItemCell::class.java)
        {
            it.addToContextMenu(
                Pair("Edit")
                {
                    openNewWindow(createEditItemScreen(it.item), true)
                },

                Pair("Delete"){
                runLater {
                    AlertBuilder(Alert.AlertType.CONFIRMATION)
                        .setTitle("Confirm Deletion")
                        .setHeader("Delete ${it.item.name}")
                        .setContextText("Would you like to delete this item?")
                        .setButtonAction(ButtonType.OK)
                        {
                            itemList.items.remove(it.item)
                            character.items.remove(it.item)
                            userInfo.writeCharacter(character)
                        }
                        .show()
                }
            })
        }

        itemAdd.setOnAction {
            openNewWindow(createAddItemScreen())
        }

        attributeList.setOnMouseClicked {
            if (it.clickCount == 2)
            {
                val item = attributeList.selectionModel.selectedItem
                val title = "${character.name}: ${item.name}"

                if (rollerWindows.containsKey(title))
                {
                    rollerWindows[title]!!.getStage()?.toFront()
                }
                else
                {
                    val window = DiceRoller(Stage(), item.name, character.getDiceSides(),
                        attribMods[character.characterId]?.get(item) ?: character.getSkillPlus(item))
                        .apply {
                            setTitle(title)
                            historyDelegate = this@MainScreen
                            completionCallback = CompletionCallBack { rollerWindows.remove(title) }
                        }
                    openNewWindow(window)
                    rollerWindows[title] = window
                }
            }
        }

        diceHistoryMenu.setOnAction {
            if (diceHistoryController != null)
            {
                diceHistoryController?.getStage()?.toFront()
            }
            else
            {
                diceHistoryController = DiceHistoryController(Stage(), this)
                openNewWindow(diceHistoryController!!)
            }
        }

        rollInitiative.setOnAction {
            openNewWindow(DiceRoller(Stage(), "Initiative", character.getDiceSides(), character.getSkillPlus(DNDBaseSkill.Dexterity.getSkill()))
                .apply {
                    historyDelegate = this@MainScreen
                    completionCallback = CompletionCallBack { rollerWindows.remove(it.name) }
                })
        }

        gaugeList.items.addAll(character.getGauges())
        attributeList.items.addAll(character.getSkills().getSkills())

        quickSelectChoice.valueProperty().addListener { observable, oldValue, newValue ->
            if (newValue != null) loadCharacter(newValue)
        }
    }

    override fun onFocusChange(isFocused: Boolean)
    {
        super.onFocusChange(isFocused)
        if (isFocused)
        {
            loadQuickSelectMenu()
            setTitle("My Character - User: ${userInfo.userId}")
        }
    }

    fun loadQuickSelectMenu()
    {
        val current = character.characterId

        quickSelectChoice.items.clear()
        quickSelectChoice.items.addAll(userInfo.characters)
        quickSelectChoice.items
            .firstIndexWhereOrNull { it.characterId == current }
            .ifNotNull { quickSelectChoice.selectionModel.select(it) }
    }

    fun sortAttributes(choice: SortChoice?)
    {
        choice.ifNotNull {
            when (it)
            {
                SortChoice.Level -> attributeList.items.sortBy { character.getSkillPlus(it) }
                SortChoice.Name -> attributeList.items.sortBy { it.name }
            }
        }

    }

    override fun loadCharacter(char: DNDCharacter)
    {
        this.character = char
        if (!attribMods.containsKey(char.characterId)) attribMods[char.characterId] = mutableMapOf()

        gaugeList.items.clear()
        attributeList.items.clear()
        itemList.items.clear()

        gaugeList.items.addAll(char.getGauges())
        attributeList.items.addAll(char.getSkills().getSkills())
        itemList.items.addAll(char.items)
        nameLabel.text = "AC - ${char.armourClass}"

        sortAttributes(sortSkillsChoice.selectionModel.selectedItem)
    }

    override fun onItemUpdate(item: DNDItem)
    {
        if (character.characterId != 0) userInfo.writeCharacter(character)
        loadCharacter(character)
    }
}