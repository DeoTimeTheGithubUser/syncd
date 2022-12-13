package me.deotime.syncd.storage

import me.deotime.syncd.storage.Storage

abstract class SyncdStorage : Storage() {
    override val root = ".syncd"
}