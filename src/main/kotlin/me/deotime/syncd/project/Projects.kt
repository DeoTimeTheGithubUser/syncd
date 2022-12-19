package me.deotime.syncd.project

import me.deotime.syncd.SyncdStorage
import me.deotime.warehouse.map
import me.deotime.warehouse.property


object Projects : SyncdStorage {
    override val name = "projects"

    val All by map<Project.Id, Project>()
}