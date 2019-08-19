package com.lapis.mycharacter.interfaces

import javafx.fxml.FXMLLoader
import java.net.URL

/**
 * Interface for a class that will need to have an FXML file initialized
 */
interface FXMLLoadable
{
    /**
     * Returns the location of the FXML file to initialize
     * @return URL of FXML Location
     */
    fun getFxmlLocation(): URL

    /**
     * Loads the FXML from the FXMLLoader
     */
    fun loadFXML()

    /**
     * Returns the FXMLLoader of the object
     * @return FXMLLoader for FXML Location
     */
    fun getLoader(): FXMLLoader

}