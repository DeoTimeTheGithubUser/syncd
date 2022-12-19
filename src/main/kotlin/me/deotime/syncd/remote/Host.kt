package me.deotime.syncd.remote

import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.routing.routing
import io.ktor.server.websocket.receiveDeserialized
import io.ktor.server.websocket.webSocket
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.isActive
import me.deotime.syncd.Syncd
import me.deotime.syncd.project.Project
import me.deotime.syncd.project.Projects
import me.deotime.syncd.utils.ServerSockets
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean


object Host {


    private val initialized = AtomicBoolean(false)
    private val projectHosts = mutableMapOf<Project.Id, Channel<Project.Update>>()

    fun hostProject(id: Project.Id): Flow<Project.Update> {
        if (!initialized.get()) initialize()
        projectHosts[id]?.let { error("Project ${id.name} is already being hosted.") }
        return Channel<Project.Update>().also { projectHosts[id] = it }.receiveAsFlow()
    }

    suspend fun processUpdate(update: Project.Update) {
        val project = Projects.All.get(update.project) ?: return
        update.changes.forEach { (relative, content) ->
            val file = File(project.directory, relative)
            file.writeText(content)
        }
    }

    private fun initialize() {
        // TODO SECURITY
        val server = embeddedServer(Netty) {
            install(ServerSockets)
            routing {
                webSocket(Syncd.Constants.HostSocketPath) {
                    while (isActive) {
                        val update = receiveDeserialized<Project.Update>()
                        val host = projectHosts[update.project] ?: continue
                        host.send(update)
                    }
                }
            }
        }

        println("Started host server with properties: ${server.environment.connectors}")
    }


}