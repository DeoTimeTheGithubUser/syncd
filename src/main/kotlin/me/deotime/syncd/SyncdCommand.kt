package me.deotime.syncd

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.types.file
import kotlinx.coroutines.runBlocking
import me.deotime.syncd.watch.Watcher
import me.deotime.syncd.watch.watcher

class SyncdCommand : CliktCommand(name = "syncd") {

    private val directory by argument().file(canBeDir = true)

    override fun run() {
        runBlocking {
            directory.watcher().listen().collect {
                println("Event: $it")
            }
        }
    }
}