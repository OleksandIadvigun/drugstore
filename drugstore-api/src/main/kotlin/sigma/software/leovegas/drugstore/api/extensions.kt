package sigma.software.leovegas.drugstore.api

fun String.messageSpliterator() = run {
    val step1 = split(":", ignoreCase = true, limit = 0)
    if (step1.size < 5) {
        step1.last().split(".").get(0)
    } else
        step1.get(6).split(".").get(0).substring(1)
}
