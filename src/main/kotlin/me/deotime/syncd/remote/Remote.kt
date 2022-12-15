package me.deotime.syncd.remote

import com.github.ajalt.clikt.parameters.arguments.RawArgument
import com.github.ajalt.clikt.parameters.arguments.convert
import com.github.ajalt.clikt.parameters.arguments.help
import kotlinx.serialization.Serializable
import me.deotime.syncd.Syncd

@Serializable
data class Remote(val ip: String, val port: Int) {
    override fun toString() = "$ip:$port"
}


fun RawArgument.remote() =
    convert { input ->

        Remotes[input] ?: runCatching {
            val split = input.split(":")
            val ip = split[0]
            val port = split[1].toInt()
            Remote(ip, port)
        }.getOrNull() ?: error("Invalid remote \"$input\".")


    }.help(Syncd.Constants.RemoteArgumentHelp)