import me.deotime.syncd.SyncdCommand
import me.deotime.syncd.config.Config
import kotlin.test.Test

const val TestArgs = "testfolder"

@Test
fun main() {
    println("Current config name: ${Config.Name}")
    Config.Name = "test"
    println("New config name: ${Config.Name}")
    SyncdCommand().main(TestArgs.split(" "))
}