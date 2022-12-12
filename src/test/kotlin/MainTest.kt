import com.github.ajalt.clikt.core.subcommands
import me.deotime.syncd.SyncdCommand
import kotlin.test.Test

@Test
fun main() {
    while(true) {
        val input = readLine()!!.removePrefix("syncd ")
        command(input)
    }
}

private fun command(input: String) {
    SyncdCommand().subcommands(
        SyncdCommand.Watch(),
        SyncdCommand.Projects().subcommands(
            SyncdCommand.Projects.Add()
        )
    ).main(input.split(" "))
}