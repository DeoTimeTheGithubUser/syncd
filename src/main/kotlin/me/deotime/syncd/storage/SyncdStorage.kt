package me.deotime.syncd.storage

interface SyncdStorage : AppdataStorage {
    override val root get() = super.root + "Syncd"
}