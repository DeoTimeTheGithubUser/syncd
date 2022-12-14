package me.deotime.syncd.project

import com.github.ajalt.clikt.parameters.arguments.RawArgument
import com.github.ajalt.clikt.parameters.arguments.convert
import com.github.ajalt.clikt.parameters.arguments.help
import kotlinx.serialization.Serializable
import me.deotime.syncd.Syncd

@Serializable
data class Project(
    val id: Id,
    val directory: String,
    val modified: List<String> = emptyList()
) {

    @Serializable @JvmInline
    value class Id(val name: String) {
        override fun toString() = name
    }

    @Serializable
    data class Update(
        val project: Id,
        val changes: Map<String, String>
    )
}

inline fun Project.Id.update(new: Project.() -> Project?) {
    val projects = Projects.All.toMutableMap()
    projects[this]?.let(new)?.let { projects[this] = it } ?: projects.remove(this)
    Projects.All = projects
}

fun RawArgument.project() =
    // TEMPORARY FIX UNTIL WE FIND OUT WHY INLINE CLASSES ARE BROKEN
    convert { name -> Projects.All.entries.find { it.key.name == name }?.value ?: error("No project found \"$name\".") }
        .help(Syncd.Constants.ProjectArgumentHelp)
