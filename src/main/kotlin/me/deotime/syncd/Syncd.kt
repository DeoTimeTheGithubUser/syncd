package me.deotime.syncd

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.types.file
import kotlinx.coroutines.runBlocking
import me.deotime.syncd.config.Config
import me.deotime.syncd.project.Project
import me.deotime.syncd.project.project
import me.deotime.syncd.project.update
import me.deotime.syncd.watch.watcher
import java.io.File

fun main(args: Array<String>) {
    Syncd().subcommands(
        Syncd.Watch(),
        Syncd.Projects().subcommands(
            Syncd.Projects.Add(),
            Syncd.Projects.Delete()
        )
    ).main(args)
}

class Syncd : CliktCommand(name = "syncd") {


    override fun run() {

    }

    class Watch : CliktCommand("watch") {

        private val project by argument().project()

        override fun run() {

            echo("Watching project ${project.name}.")
            runBlocking {
                File(project.directory).watcher().listen().collect {
                    project.update { copy(modified = modified + it.file.absolutePath) }
                }
            }

        }
    }

    class Projects : CliktCommand(name = "projects", invokeWithoutSubcommand = true) {
        override fun run() {
            currentContext.invokedSubcommand ?: run {
                echo("Projects: ${Config.Projects.map { it.name }}")
            }
        }

        class Add : CliktCommand(name = "add") {
            private val name by argument()
            private val directory by argument().file(canBeDir = true, canBeFile = false, mustExist = true)
            override fun run() {
                val proj = Project(name, directory.absolutePath)
                Config.Projects = Config.Projects + proj
                echo("Added project ${proj.name}")
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