package me.deotime.syncd

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.arguments.argument
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
import me.deotime.syncd.watch.Watcher
import me.deotime.syncd.watch.watcher
import java.io.File
import kotlin.time.Duration.Companion.milliseconds

fun main(args: Array<String>) {
    Syncd().subcommands(
        Syncd.Watch(),
        Syncd.Changes(),
        Syncd.HostProject(),
        Syncd.Projects().subcommands(
            Syncd.Projects.Add(),
            Syncd.Projects.Delete()
        )
    ).main(args)
}

private typealias ProjectsData = Projects

class Syncd : CliktCommand(name = "syncd") {


    override fun run() = Unit

    class HostProject : CliktCommand(
        name = "host",
        help = "Host a project.",
        epilog = Constants.HostDescription
    ) {
        private val project by argument().project()

        override fun run() {
            RemoteScope.launch {
                Host.hostProject(project.id).collect {
                    println("Received update: $it")
                    Host.processUpdate(it)
                }
            }
        }
    }

    class Sync : CliktCommand(
        name = "sync",
        help = "Syncs a project to a remote.",
        epilog = Constants.SyncDescription
    ) {

        private val project by argument().project()
        private val remote by argument().remote()
        private val updateInterval by option().long()

        override fun run() {
            RemoteScope.launch {
                RemoteSync.sync(project.id, remote, updateInterval?.milliseconds)
            }
        }
    }

    class Watch : CliktCommand(
        name = "watch",
        help = "Begins watching a project.",
        epilog = Syncd.Constants.WatchDescription
    ) {

        private val project by argument().project()

        override fun run() {
            Watcher.Scope.launch {
                echo("Watching project ${project.id}.")
                File(project.directory).watcher().listen().collect {
                    val value = it.file.absolutePath.removePrefix(project.directory)
                    if (value in project.modified) return@collect
                    project.id.update { copy(modified = modified + value) }
                }
            }

        }
    }

    class Changes : CliktCommand(
        name = "changes",
        help = "Current file changes in a project.",
        epilog = Syncd.Constants.ChangesProjectHelp
    ) {
        private val project by argument().project()

        override fun run() {
            echo("Current changes in ${project.id}")
            project.modified.forEach {
                println("File: $it")
            }
        }
    }

    class Projects : CliktCommand(
        name = "projects",
        invokeWithoutSubcommand = true
    ) {
        override fun run() {
            currentContext.invokedSubcommand ?: run {
                echo("Projects: ${ProjectsData.All.keys}")
            }
        }

        class Add : CliktCommand(
            name = "add",
            help = "Creates a new project."
        ) {
            private val name by argument()
            private val directory by argument().file(canBeDir = true, canBeFile = false, mustExist = true)
            override fun run() {
                val id = Project.Id(name)
                val proj = Project(id, directory.absolutePath)
                ProjectsData.All = ProjectsData.All + (id to proj)
                echo("Added project $name")
            }
        }

        class Delete : CliktCommand(
            name = "delete",
            help = "Deletes a project.",
            epilog = Syncd.Constants.DeleteProjectHelp

        ) {
            private val project by argument().project()
            override fun run() {
                project.id.update { null }
                echo("Removed project ${project.id}")
            }
        }
    }

    object Constants {
        const val HostDescription = """
            Starts a host to a project on this machine. This will allow
            other devices to connect to this as a remote, and upload
            its files a corresponding project.
        """

        const val SyncDescription = """
            Syncs a project to a remote host. This will send all currently
            watched files that have been modified, and replicate them in the
            corresponding project on the host.
        """

        const val WatchDescription = """
            Starts watching a project. This will track all files that
            are created, modified, or deleted in a directory. These can
            then be used to automatically sync files to a remote host.
        """

        const val DeleteProjectHelp = """
            Deletes a project, permanently. Note that this only deletes
            the local instance of the projects, and does not delete
            files locally or on a remote.
        """

        const val ChangesProjectHelp = """
            Displays all current file changes in a project. Changes are
            only tracked while a project is being actively watdched.
        """

        const val ProjectArgumentHelp = "Name of a project. Use 'syncd projects' to find all created projects."
        const val RemoteArgumentHelp = "A remote address. Format should be {IP}:{PORT}."


        const val HostSocketPath = "/projecthost"
    }
}