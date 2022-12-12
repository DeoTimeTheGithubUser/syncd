import me.deotime.syncd.SyncdCommand
import kotlin.test.Test

const val TestArgs = "testfolder"

@Test
fun main() {
    SyncdCommand().main(TestArgs.split(" "))
}