package me.deotime.syncd

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.types.file
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import me.deotime.syncd.project.Project
import me.deotime.syncd.project.Projects
import me.deotime.syncd.project.project
import me.deotime.syncd.project.update
import me.deotime.syncd.watch.WatcherScope
import me.deotime.syncd.watch.watcher
import java.io.File

fun main(args: Array<String>) {
    Syncd().subcommands(
        Syncd.Watch(),
        Syncd.Projects().subcommands(
            Syncd.Projects.Add(),
            Syncd.Projects.Changes(),
            Syncd.Projects.Delete()
        )
    ).main(args)
}

private typealias ProjectsData = Projects

class Syncd : CliktCommand(name = "syncd") {


    override fun run() {

    }

    class Watch : CliktCommand("watch") {

        private val project by argument().project()

        override fun run() {
            WatcherScope.launch {
                echo("Watching project ${project.name}.")
                File(project.directory).watcher().listen().collect {
                    project.update { copy(modified = modified + it.file.absolutePath) }
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
                val proj = Project(name, directory.absolutePath)
                ProjectsData.All = ProjectsData.All + (name to proj)
                echo("Added project ${proj.name}")
            }
        }

        class Changes : CliktCommand(name = "changes") {
            private val project by argument().project()

            override fun run() {
                echo("Current changes in ${project.name}")
                project.modified.forEach {
                    println("File: $it")
                }
            }
        }

        class Delete : CliktCommand(name = "delete") {
            private val project by argument().project()
            override fun run() {
                project.update { null }
                echo("Removed project ${project.name}")
            }
        }
    }
}