package me.deotime.syncd.project

import me.deotime.syncd.storage.Storage.Companion.property
import me.deotime.syncd.storage.SyncdStorage

object Projects : SyncdStorage {
    override val name = "projects"

    var All by property(emptyMap<String, Project>())

    operator fun get(name: String) = All[name]
}