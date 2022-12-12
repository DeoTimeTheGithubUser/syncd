package me.deotime.syncd.watch

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.isActive
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import java.nio.file.Path
import java.nio.file.StandardWatchEventKinds

class Watcher(val path: Path) {

    suspend fun listen(): Flow<FileEvent> {
        return withContext(Dispatchers.IO) {

            flow {
                while (true) {
                    val watcher = path.fileSystem.newWatchService()
                    path.register(watcher,
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
    }

}