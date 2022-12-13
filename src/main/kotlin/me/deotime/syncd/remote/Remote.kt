package me.deotime.syncd.remote

import com.github.ajalt.clikt.parameters.arguments.RawArgument
import com.github.ajalt.clikt.parameters.arguments.convert
import kotlinx.serialization.Serializable
import me.deotime.syncd.project.Project
import me.deotime.syncd.project.Projects

@Serializable
data class Remote(val ip: String, val port: Int)


fun RawArgument.remote() =
    convert { input ->

        runCatching {
            val split = input.split(":")
            val ip = split[0]
            val port = split[1].toInt()
            Remote(ip, port)
        }.getOrNull() ?: error("Invalid remote \"$input\".")


    }