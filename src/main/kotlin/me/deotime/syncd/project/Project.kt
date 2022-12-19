package me.deotime.syncd.project

import com.github.ajalt.clikt.parameters.arguments.RawArgument
import com.github.ajalt.clikt.parameters.arguments.convert
import com.github.ajalt.clikt.parameters.arguments.help
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import me.deotime.syncd.Syncd

@Serializable
data class Project(
    val id: Id,
    val directory: String,
    val modified: List<String> = emptyList()
) {

    @Serializable
    @JvmInline
    value class Id(val name: String) {
        override fun toString() = name
    }

    @Serializable
    data class Update(
        val project: Id,
        val changes: Map<String, String>
    )
}

suspend inline fun Project.Id.update(new: Project.() -> Project?) {
    Projects.All.get(this)?.let {
        Projects.All.set(this, new(it))
    }
}

fun RawArgument.project() =
    // TEMPORARY FIX UNTIL WE FIND OUT WHY INLINE CLASSES ARE BROKEN
    convert { name -> runBlocking { Projects.All.get(Project.Id(name)) } ?: error("No project found \"$name\".") }
        .help(Syncd.Constants.ProjectArgumentHelp)
