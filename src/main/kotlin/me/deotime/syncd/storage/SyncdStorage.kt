package me.deotime.syncd.storage

interface SyncdStorage : Storage {
    override val root get() = ".syncd"
}