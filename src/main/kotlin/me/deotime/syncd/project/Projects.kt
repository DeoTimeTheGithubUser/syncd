package me.deotime.syncd.project

import me.deotime.syncd.storage.SyncdStorage
import me.deotime.syncd.storage.property

object Projects : SyncdStorage {
    override val name = "projects"

    var All by property(emptyMap<String, Project>())

    operator fun get(name: String) = All[name]
}