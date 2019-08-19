package com.lapis.mycharacter.interfaces

data class SourceDelegate<T>(val source: T)
data class CompletionCallBack<T, E>(val onComplete: (T) -> E)