package com.lapis.mycharacter.util

import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import com.google.gson.reflect.TypeToken
import com.lapis.mycharacter.DNDCharacter
import com.lapis.mycharacter.interfaces.AlertBuilder
import javafx.scene.control.Alert
import org.apache.http.client.methods.*
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.HttpClientBuilder
import java.io.File
import java.net.URL
import java.net.URLDecoder
import java.net.URLEncoder
import java.time.LocalDateTime
import java.util.HashMap
import java.util.concurrent.ThreadLocalRandom
import kotlin.Exception

class DatabaseUser(userId: String, syncDate: LocalDateTime = LocalDateTime.now(), characters: ArrayList<DNDCharacter> = arrayListOf(), autoSyncEnabled: Boolean = false)
{
    private var baseInfo: CharacterUserStorable = CharacterUserStorable(userId, syncDate, ArrayList(characters), autoSyncEnabled)

    val userId: String
        get() = baseInfo.userId

    val syncDate: LocalDateTime
        get() = baseInfo.syncDate

    val characterIds: Collection<Int>
        get() = baseInfo.characters.map { it.characterId }

    val characters: Collection<DNDCharacter>
        get() = baseInfo.characters

    var saveFile: File? = null

    var autoSyncEnabled = autoSyncEnabled
    set(value) {
        field = value
        baseInfo.autoSyncEnabled = value

        saveToFile()
        if (value) push()
    }

    private fun urlEncodeString(string: String): String
            = URLEncoder.encode(string, "UTF-8")

    private fun urlDecodeString(string: String): String
            = URLDecoder.decode(string, "UTF-8")

    fun setUserId(new: String, onResult: ((ConnectionResult) -> Unit)? = null) {
        val oldId = baseInfo.userId
        val newId = new

        AsyncDispatchQueue {
            DatabaseUser(urlEncodeString(newId)).pull { connectionResult, characterUserResult ->
                if (characterUserResult.characters?.size ?: -1 > 0)
                {
                    onResult?.invoke(ConnectionResult.Error
                        .apply {
                            message = "User ID already exists"
                            warning = ConnectionResult.WarningMessage.UserExists
                        })
                    it.halt()
                }
                else
                    baseInfo.userId = newId
            }
        }
            .then { handler ->
                push {
                if (!it.isSuccess())
                {
                    baseInfo.userId = oldId
                    handler.halt()
                    onResult?.invoke(ConnectionResult.Fail)
                }
            } }
            .then { handler ->

                if (!createDeleteRequest(URL("$baseUrl/${urlEncodeString(oldId)}.json")).isSuccess())
                {
                    handler.halt()
                    baseInfo.userId = oldId
                    onResult?.invoke(ConnectionResult.Fail)
                }
            }
            .then { saveToFile() }
            .then {
                onResult?.invoke(ConnectionResult.Success)
            }
            .start()
    }

    fun pull(onPullRequestComplete: ((ConnectionResult, CharacterUserResult) -> Unit)? = null)
    {
        val url = URL("$baseUrl/$userId.json")
        AsyncDispatchQueue {
            try
            {
                val returnedText = url.readText()
                if (returnedText != "null") baseInfo = GsonUtility().create(returnedText, baseInfo.javaClass)

                baseInfo.syncDate = LocalDateTime.now()
                onPullRequestComplete?.invoke(ConnectionResult.Success, baseInfo.createConsumable())
            }
            catch (e: Exception)
            {
                e.printStackTrace()
                onPullRequestComplete?.invoke(ConnectionResult.Fail, baseInfo.createConsumable())
            }
        }.start()
    }

    fun push(onPushRequestComplete: ((ConnectionResult) -> Unit)? = null)
    {
        AsyncDispatchQueue {
            val result = createPatchRequest(URL("$baseUrl.json"), mapOf(Pair(userId, baseInfo.apply { syncDate = LocalDateTime.now() })))
            if (result == ConnectionResult.Success) baseInfo.syncDate = LocalDateTime.now()
            onPushRequestComplete?.invoke(result)
        }.start()
    }

    fun writeCharacter(character: DNDCharacter) 
    {
        baseInfo.characters.removeIf { it.characterId == character.characterId }
        baseInfo.characters.add(character)

        saveToFile()
        if (autoSyncEnabled) push()
    }

    fun removeCharacter(characterId: Int) {
        baseInfo.characters.removeIf { it.characterId == characterId }

        saveToFile()
        if (autoSyncEnabled) push()
    }

    fun removeCharacter(character: DNDCharacter) {
        removeCharacter(character.characterId)
    }

    fun initialize(onPullRequestComplete: ((ConnectionResult, CharacterUserResult) -> Unit)? = null)
    {
        pull(onPullRequestComplete)
    }

    fun saveToFile(file: File? = saveFile) {
        file?.writeText(toJson())
    }

    fun toJson(embedInUser: Boolean = false): String
    {
        val toSerialize: Any =
            if (embedInUser) mapOf(Pair(userId, baseInfo))
            else baseInfo

        return GsonUtility().toJson(toSerialize)
    }

    companion object {

        private const val baseUrl = "https://my-character-7ab0f.firebaseio.com/users"

        fun createOrRead(file: File, onFileUnreadable: (File) -> Unit = {}): DatabaseUser {
            val toReturn = DatabaseUser(
                ThreadLocalRandom.current()
                    .nextInt(10000, 999999).toString()
            )

            if (file.exists()) {
                val input = file.readText()
                try
                {
                    val storable = Gson().fromJson(input, CharacterUserStorable::class.java)
                    return storable.createDatabaseUser().apply { saveFile = file }

                } catch (e: Exception)
                {
                    val result = DNDCharacterLegacy().checkFile(file)
                    if (result.first) {
                        toReturn.baseInfo.characters.addAll(result.second)
                        if (file.delete()) toReturn.saveToFile(file)
                    } else
                    {
                        System.err.println("Found old character file that cannot successfully convert")
                        val renameFile = File("${file.name}-1.bak")
                        file.renameTo(renameFile)

                        onFileUnreadable(renameFile)
                    }
                }
            }

            toReturn.saveToFile(file)
            return toReturn.apply { saveFile = file }
        }

        fun getUserList(onResult: (Collection<DatabaseUser>) -> Unit)
        {
            val url = URL("$baseUrl.json")
            AsyncDispatchQueue {
                try
                {
                    val returnedText = url.readText()
                    var result = arrayListOf<DatabaseUser>()
                    if (returnedText != "null")
                    {
                        var x = (GsonUtility().gson.fromJson(returnedText, TypeNotices.databaseUserType) as HashMap<String, CharacterUserStorable>)
                        onResult(x.map { it.value.createDatabaseUser() })
                    }
                    else onResult(result)
                }
                catch (e: Exception)
                {
                    e.printStackTrace()
                }
            }
                .start()
        }
    }
}


private data class CharacterUserStorable(var userId: String = "Unknown",
                                         var syncDate: LocalDateTime = LocalDateTime.now(),
                                         var characters: ArrayList<DNDCharacter> = arrayListOf(),
                                         var autoSyncEnabled: Boolean = false)
{
    fun createConsumable(): CharacterUserResult
        = CharacterUserResult(userId, syncDate, characters, autoSyncEnabled)

    fun createDatabaseUser(): DatabaseUser
        = DatabaseUser(userId, syncDate, characters, autoSyncEnabled)
}

data class CharacterUserResult(val userId: String,
                               val syncDate: LocalDateTime?,
                               val characters: ArrayList<DNDCharacter>?,
                               val autoSyncEnabled: Boolean = false)


enum class ConnectionResult
{
    Success, Fail, Error;

    var warning = WarningMessage.None
    var message = warning.getMessage()

    fun isSuccess(): Boolean
        = this == Success

    enum class WarningMessage
    {
        None, UserExists;

        fun getMessage(): String
        {
            return when (this)
            {
                None -> "No error is present."
                UserExists -> "The new user already exists."
            }
        }
    }
}

private fun createDeleteRequest(url: URL): ConnectionResult
{
    val client = HttpClientBuilder.create().build()

    val req = HttpDelete(url.toString())

    try
    {
        val response = client.execute(req)
        return if ((200..299).contains(response.statusLine.statusCode))
            ConnectionResult.Success else {
            System.err.println(response.toString())
            println(url)
            ConnectionResult.Fail
        }
    }
    catch (e: Exception)
    {
        e.printStackTrace()
        return ConnectionResult.Fail
    }
}

private fun createPatchRequest(url: URL, map: Map<String, Any>): ConnectionResult
{
    val client = HttpClientBuilder.create().build()
    val toSend = GsonUtility().toJson(map)

    val patchReq = HttpPatch(url.toString())
        .apply {
            addHeader("Accept", "*/*");
            addHeader("Content-type", "application/json")

            entity = StringEntity(toSend, "UTF-8").apply {
                setContentType("application/json")
            }
        }

    try
    {
        val response = client.execute(patchReq)
        return if ((200..299).contains(response.statusLine.statusCode))
            ConnectionResult.Success else
        {
            System.err.println(response.toString())
            println(toSend)
            ConnectionResult.Fail
        }
    }
    catch (e: Exception)
    {
        e.printStackTrace()
        return ConnectionResult.Fail
    }

}

private fun createPutRequest(url: URL, toSend: String): ConnectionResult
{
    val client = HttpClientBuilder.create().build()

    val patchReq = HttpPut(url.toString())
        .apply {
            addHeader("Accept", "*/*");
            addHeader("Content-type", "application/json")

            entity = StringEntity(toSend, "UTF-8").apply {
                setContentType("application/json")
            }

        }

    try
    {
        val response = client.execute(patchReq)
        return if ((200..299).contains(response.statusLine.statusCode))
            ConnectionResult.Success else
        {
            System.err.println(response.toString())
            println(toSend)
            ConnectionResult.Fail
        }
    }
    catch (e: Exception)
    {
        e.printStackTrace()
        return ConnectionResult.Fail
    }

}