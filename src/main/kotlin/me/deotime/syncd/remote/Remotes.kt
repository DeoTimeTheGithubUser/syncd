package me.deotime.syncd.remote

import me.deotime.syncd.SyncdStorage
import me.deotime.warehouse.AppdataStorage
import me.deotime.warehouse.map

object Remotes : SyncdStorage {
    override val name = "remotes"

    val All by map<String, Remote>()

}
