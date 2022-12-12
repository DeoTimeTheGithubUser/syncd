package me.deotime.syncd.watch

import java.io.File
import java.nio.file.Path
import java.nio.file.StandardWatchEventKinds
import java.nio.file.WatchEvent

sealed interface FileEvent {
    val file: File

    data class Create(override val file: File) : FileEvent
    data class Modify(override val file: File) : FileEvent
    data class Delete(override val file: File) : FileEvent
}

internal fun WatchEvent<*>.toFileEvent() = run {
    val context = (context() as Path).toFile()
    when (kind()) {
        StandardWatchEventKinds.ENTRY_CREATE -> FileEvent.Create(context)
        StandardWatchEventKinds.ENTRY_MODIFY -> FileEvent.Modify(context)
        StandardWatchEventKinds.ENTRY_DELETE -> FileEvent.Delete(context)
        else -> error("Unknown event")
    }
}