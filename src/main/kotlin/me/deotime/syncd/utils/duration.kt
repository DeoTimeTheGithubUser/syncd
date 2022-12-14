package me.deotime.syncd.utils

import com.github.ajalt.clikt.parameters.arguments.RawArgument
import com.github.ajalt.clikt.parameters.arguments.convert
import kotlin.time.Duration

fun RawArgument.duration() =
    convert { Duration.parse(it) }