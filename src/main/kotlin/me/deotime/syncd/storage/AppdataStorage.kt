package me.deotime.syncd.storage

import java.io.File

interface AppdataStorage : Storage {
    override val root: String
        get() = System.getenv("APPDATA") + File.separator
}