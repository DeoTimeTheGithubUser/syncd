import me.deotime.syncd.main

//class TestMain {
//
//    @Test
//    fun projects() = runBlocking {
//        syncd("projects add test C:/testproject")
//        syncd("projects")
//        delay(10_000)
//        syncd("projects changes test")
//    }
//
//
//    private fun syncd(args: String) {
//        main(args.split(" ").toTypedArray())
//    }
//}

fun main() {
    while(true) {
        val input = readLine()!!
        syncd(input.removePrefix("syncd "))
    }
}

private fun syncd(args: String) {
    main(args.split(" ").toTypedArray())
}