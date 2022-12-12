package me.deotime.syncd.project

import com.github.ajalt.clikt.parameters.arguments.RawArgument
import com.github.ajalt.clikt.parameters.arguments.convert
import kotlinx.serialization.Serializable
import me.deotime.syncd.config.Config

@Serializable
data class Project(
    val name: String,
    val directory: String,
    val modified: List<String> = emptyList()
)

// this is incredibly scuffed and should be reworked (optics maybe?)
inline fun Project.update(new: Project.() -> Project?) {
    val projects = Config.Projects.associateBy { it.name }.toMutableMap()
    new()?.let { projects[name] = it } ?: projects.remove(name)
    Config.Projects = projects.values.toList()
}

fun RawArgument.project() =
    convert { input -> Config.Projects.firstOrNull { it.name == input } ?: error("No project found.") }
