package com.lapis.mycharacter.util;

import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;

public class TypeNotices
{
    public static Type databaseUserType = new TypeToken<HashMap<String, CharacterUserStorable>>(){}.getType();
}
