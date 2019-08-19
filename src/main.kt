import com.lapis.mycharacter.interfaces.Launcher
import com.lapis.mycharacter.interfaces.screens.MainScreen

fun main()
{
    Launcher.start { MainScreen(it) }
}