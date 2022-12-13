package me.deotime.syncd.project

import com.github.ajalt.clikt.parameters.arguments.RawArgument
import com.github.ajalt.clikt.parameters.arguments.convert
import kotlinx.serialization.Serializable

@Serializable
data class Project(
    val id: Id,
    val directory: String,
    val modified: List<String> = emptyList()
) {

    @Serializable
    data class Id(val name: String) {
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
    convert { name -> Projects[Project.Id(name)] ?: error("No project found \"$name\".") }
