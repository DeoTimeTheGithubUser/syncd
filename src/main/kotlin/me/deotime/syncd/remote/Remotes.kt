package me.deotime.syncd.remote

import me.deotime.syncd.storage.AppdataStorage
import me.deotime.syncd.storage.Storage.Companion.property

object Remotes : AppdataStorage {
    override val name = "remotes"

    var All by property(emptyMap<String, Remote>())

    operator fun get(name: String) = All[name]
}
