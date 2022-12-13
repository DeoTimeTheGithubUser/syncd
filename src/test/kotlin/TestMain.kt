import me.deotime.syncd.main
import kotlin.test.Test

class TestMain {

    @Test
    fun projects() {
        syncd("projects add test C:/testproject")
        syncd("projects")
    }


    private fun syncd(args: String) {
        main(args.split(" ").toTypedArray())
    }
}