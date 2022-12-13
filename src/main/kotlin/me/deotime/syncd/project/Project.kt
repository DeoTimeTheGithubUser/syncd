package me.deotime.syncd.project

import com.github.ajalt.clikt.parameters.arguments.RawArgument
import com.github.ajalt.clikt.parameters.arguments.convert
import kotlinx.serialization.Serializable

@Serializable
data class Project(
    val name: String,
    val directory: String,
    val modified: List<String> = emptyList()
)

inline fun Project.update(new: Project.() -> Project?) {
    val projects = Projects.All.toMutableMap()
    new()?.let { projects[name] = it } ?: projects.remove(name)
    Projects.All = projects
}

fun RawArgument.project() =
    convert { name -> Projects[name] ?: error("No project found \"$name\".") }
