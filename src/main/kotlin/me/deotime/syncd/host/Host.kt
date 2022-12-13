package me.deotime.syncd.host

import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.routing.routing
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.receiveDeserialized
import io.ktor.server.websocket.webSocket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.isActive
import me.deotime.syncd.project.Project
import java.util.concurrent.atomic.AtomicBoolean


object Host {


    object Scope : CoroutineScope by CoroutineScope(Dispatchers.Default)


    private val initialized = AtomicBoolean(false)
    private val projectHosts = mutableMapOf<String, Channel<Project.Update>>()

    fun hostProject(project: Project): Flow<Project.Update> {
        if(!initialized.get()) initialize()
        projectHosts[project.name]?.let { error("Project ${project.name} is already being hosted.") }
        return Channel<Project.Update>().also { projectHosts[project.name] = it }.receiveAsFlow()
    }

    private fun initialize() {
        // TODO SECURITY
        embeddedServer(Netty) {
            install(WebSockets)
            routing {
                webSocket("/projecthost") {
                    while(isActive) {
                        val update = receiveDeserialized<Project.Update>()
                        val host = projectHosts[update.project] ?: continue
                        host.send(update)
                    }
                }
            }
        }
    }


}