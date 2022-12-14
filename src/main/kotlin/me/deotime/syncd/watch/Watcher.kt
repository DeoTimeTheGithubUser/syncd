package me.deotime.syncd.watch

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import java.io.File
import java.nio.file.Path
import java.nio.file.StandardWatchEventKinds


class Watcher(private val path: Path) {

    suspend fun listen() = flow {
        while (currentCoroutineContext().isActive) {
            val watcher = path.fileSystem.newWatchService()
            path.register(
                watcher,
                StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_MODIFY,
                StandardWatchEventKinds.ENTRY_DELETE
            )
            val update = watcher.take()
            watcher.close()
            emitAll(update.pollEvents().map { it.toFileEvent() }.asFlow())
        }
    }.flowOn(Dispatchers.IO)

}

fun File.watcher() = Watcher(toPath())