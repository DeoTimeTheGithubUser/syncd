import me.deotime.syncd.SyncdCommand
import me.deotime.syncd.config.Config
import kotlin.test.Test

const val TestArgs = "testfolder"

@Test
fun main() {
    SyncdCommand().main(TestArgs.split(" "))
}