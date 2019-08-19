package com.lapis.mycharacter.util

inline fun <T: Any?> T?.ifNotNull(func: (T) -> Unit)
{
    if (this != null) func.invoke(this)
}

fun Boolean?.nullTrue(): Boolean
{
    return this ?: false
}

inline fun <T> Collection<T>.firstIndexWhereOrNull(predicate: (T) -> Boolean): Int?
{
    for (i in 0 until this.size)
    {
        if (predicate(this.elementAt(i))) return i
    }
    return null
}

fun <T> MutableCollection<T>.addIf(value: T, predicate: (T) -> Boolean)
{
    if (predicate(value)) add(value)
}

inline fun <reified T> MutableCollection<T>.removeIfButOne(predicate: (T) -> Boolean): Collection<T>
{
    var found = false
    val indexes = arrayListOf<Int>()

    forEachIndexed {index, it -> if (predicate(it)) {
        if (!found) found = true
        else indexes.add(0, index)
    }}

    val array = ArrayList(this)
    indexes.forEach { array.removeAt(it) }
    return array
}

inline fun <T> Collection<T>.firstIndexWhere(predicate: (T) -> Boolean): Int
{
    for (i in 0 until this.size)
    {
        if (predicate(this.elementAt(i))) return i
    }
    return -1
}

fun <T> Collection<T>.containsMatch(predicate: (T) -> Boolean): Boolean
{
    for (i in this)
    {
        if (predicate(i)) return true
    }
    return false
}

enum class SortChoice
{
    Name, Level;
}