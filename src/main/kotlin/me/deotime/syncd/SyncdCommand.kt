package me.deotime.syncd

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.types.file
import kotlinx.coroutines.runBlocking
import me.deotime.syncd.watch.Watcher

class SyncdCommand : CliktCommand(name = "syncd") {

    private val directory by argument().file(canBeDir = true)

    override fun run() {
        val watcher = Watcher(directory.toPath())
        runBlocking {
            watcher.listen().collect {
                println("Event: $it")
            }
        }
    }
}