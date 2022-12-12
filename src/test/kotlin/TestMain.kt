import me.deotime.syncd.main
import kotlin.test.Test

class TestMain {

    @Test
    fun projects() {
        syncd("projects delete test")
    }


    private fun syncd(args: String) {
        main(args.split(" ").toTypedArray())
    }
}