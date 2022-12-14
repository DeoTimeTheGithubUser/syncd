package me.deotime.syncd.remote

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.websocket.sendSerialized
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.http.HttpMethod
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import me.deotime.syncd.Syncd
import me.deotime.syncd.project.Project
import me.deotime.syncd.project.Projects
import me.deotime.syncd.project.update
import me.deotime.syncd.utils.ClientSockets
import me.deotime.syncd.utils.toBase64
import java.io.File
import kotlin.time.Duration

object RemoteSync {
    private val client by lazy {
        HttpClient(CIO) {
            install(ClientSockets)
        }
    }

    suspend fun sync(
        project: Project.Id,
        remote: Remote,
        updateInterval: Duration? = null
    ) {
        client.webSocket(
            HttpMethod.Post,
            host = remote.ip,
            port = remote.port,
            path = Syncd.Constants.HostSocketPath
        ) {
            while (isActive) {
                sendSerialized(
                    Project.Update(
                        project,
                        Projects[project]?.modified?.associateBy { File(it).readText().toBase64() } ?: emptyMap()
                    )
                )
                project.update { copy(modified = emptyList()) }
                updateInterval?.let { delay(it) } ?: break
            }

        }
    }

}