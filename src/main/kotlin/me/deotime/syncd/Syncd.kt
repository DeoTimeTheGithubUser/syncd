package me.deotime.syncd

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.file
import com.github.ajalt.clikt.parameters.types.long
import kotlinx.coroutines.launch
import me.deotime.syncd.project.Project
import me.deotime.syncd.project.Projects
import me.deotime.syncd.project.project
import me.deotime.syncd.project.update
import me.deotime.syncd.remote.Host
import me.deotime.syncd.remote.RemoteScope
import me.deotime.syncd.remote.RemoteSync
import me.deotime.syncd.remote.remote
import me.deotime.syncd.utils.toBase64
import me.deotime.syncd.watch.Watcher
import me.deotime.syncd.watch.watcher
import java.io.File
import kotlin.time.Duration.Companion.milliseconds

fun main(args: Array<String>) {
    Syncd().subcommands(
        Syncd.Watch(),
        Syncd.HostProject(),
        Syncd.Projects().subcommands(
            Syncd.Projects.Add(),
            Syncd.Projects.Changes(),
            Syncd.Projects.Delete()
        )
    ).main(args)
}

private typealias ProjectsData = Projects

class Syncd : CliktCommand(name = "syncd") {


    override fun run() = Unit

    class HostProject : CliktCommand(name = "host") {
        private val project by argument().project()

        override fun run() {
            RemoteScope.launch {
                Host.hostProject(project.id).collect {
                    println("Received update: $it")
                }
            }
        }
    }

    class Sync : CliktCommand(name = "sync") {

        private val project by argument().project()
        private val remote by argument().remote()
        private val updateInterval by option().long()

        override fun run() {
            RemoteScope.launch {
                RemoteSync.sync(project.id, remote, updateInterval?.milliseconds)
            }
        }
    }

    class Watch : CliktCommand(name = "watch") {

        private val project by argument().project()

        override fun run() {
            Watcher.Scope.launch {
                echo("Watching project ${project.id}.")
                File(project.directory).watcher().listen().collect {
                    val value = it.file.absolutePath
                    if (value in project.modified) return@collect
                    project.id.update { copy(modified = modified + value) }
                }
            }

        }
    }

    class Projects : CliktCommand(name = "projects", invokeWithoutSubcommand = true) {
        override fun run() {
            currentContext.invokedSubcommand ?: run {
                echo("Projects: ${ProjectsData.All.keys}")
            }
        }

        class Add : CliktCommand(name = "add") {
            private val name by argument()
            private val directory by argument().file(canBeDir = true, canBeFile = false, mustExist = true)
            override fun run() {
                val id = Project.Id(name)
                val proj = Project(id, directory.absolutePath)
                ProjectsData.All = ProjectsData.All + (id to proj)
                echo("Added project $name")
            }
        }

        class Changes : CliktCommand(name = "changes") {
            private val project by argument().project()

            override fun run() {
                echo("Current changes in ${project.id}")
                project.modified.forEach {
                    println("File: $it")
                }
            }
        }

        class Delete : CliktCommand(name = "delete") {
            private val project by argument().project()
            override fun run() {
                project.id.update { null }
                echo("Removed project ${project.id}")
            }
        }
    }

    // todo
    object Constants
}