package dev.syoritohatsuki.fabricapplicationlauncher.util

import kotlin.properties.ReadOnlyProperty

/*   Linux + Darwin   */
val HOME by environment("~/")

/*   Linux   */
val XDG_DATA_DIRS by environment("/usr/share")

inline fun <reified T : Any> environment(defaultValue: T): ReadOnlyProperty<Any?, T> = ReadOnlyProperty { _, property ->
    System.getenv(property.name) as? T ?: defaultValue
}