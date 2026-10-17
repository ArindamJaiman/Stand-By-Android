package com.standbypro.ui.faces

/**
 * Pure, unit-tested logic behind the "algorithmic" faces (word clock, Fibonacci clock,
 * dot-matrix and seven-segment fonts, roman numerals).
 */
object FaceMath {

    // ---------------- Word clock (QLOCKTWO layout) ----------------

    val wordGrid = listOf(
        "ITLISASAMPM",
        "ACQUARTERDC",
        "TWENTYFIVEX",
        "HALFSTENFTO",
        "PASTERUNINE",
        "ONESIXTHREE",
        "FOURFIVETWO",
        "EIGHTELEVEN",
        "SEVENTWELVE",
        "TENSEOCLOCK"
    )

    /** row, start column, length */
    data class Word(val row: Int, val col: Int, val len: Int)

    private val IT = Word(0, 0, 2)
    private val IS = Word(0, 3, 2)
    private val A = Word(1, 0, 1)
    private val QUARTER = Word(1, 2, 7)
    private val TWENTY = Word(2, 0, 6)
    private val FIVE_M = Word(2, 6, 4)
    private val HALF = Word(3, 0, 4)
    private val TEN_M = Word(3, 5, 3)
    private val TO = Word(3, 9, 2)
    private val PAST = Word(4, 0, 4)
    private val OCLOCK = Word(9, 5, 6)
    private val hourWords = listOf(
        Word(8, 5, 6), // twelve
        Word(5, 0, 3), // one
        Word(6, 8, 3), // two
        Word(5, 6, 5), // three
        Word(6, 0, 4), // four
        Word(6, 4, 4), // five
        Word(5, 3, 3), // six
        Word(8, 0, 5), // seven
        Word(7, 0, 5), // eight
        Word(4, 7, 4), // nine
        Word(9, 0, 3), // ten
        Word(7, 5, 6)  // eleven
    )

    fun wordClockWords(hour24: Int, minute: Int): List<Word> {
        val m5 = minute / 5
        val words = mutableListOf(IT, IS)
        when (m5) {
            0 -> {}
            1 -> words += listOf(FIVE_M, PAST)
            2 -> words += listOf(TEN_M, PAST)
            3 -> words += listOf(A, QUARTER, PAST)
            4 -> words += listOf(TWENTY, PAST)
            5 -> words += listOf(TWENTY, FIVE_M, PAST)
            6 -> words += listOf(HALF, PAST)
            7 -> words += listOf(TWENTY, FIVE_M, TO)
            8 -> words += listOf(TWENTY, TO)
            9 -> words += listOf(A, QUARTER, TO)
            10 -> words += listOf(TEN_M, TO)
            else -> words += listOf(FIVE_M, TO)
        }
        val hour = (if (m5 >= 7) hour24 + 1 else hour24) % 12
        words += hourWords[hour]
        if (m5 == 0) words += OCLOCK
        return words
    }

    fun wordClockLit(hour24: Int, minute: Int): Array<BooleanArray> {
        val lit = Array(wordGrid.size) { BooleanArray(wordGrid[0].length) }
        wordClockWords(hour24, minute).forEach { w ->
            for (c in w.col until w.col + w.len) lit[w.row][c] = true
        }
        return lit
    }

    fun wordClockSentence(hour24: Int, minute: Int): String =
        wordClockWords(hour24, minute).joinToString(" ") { wordGrid[it.row].substring(it.col, it.col + it.len) }

    // ---------------- Fibonacci clock ----------------

    val fibSquares = intArrayOf(1, 1, 2, 3, 5)

    /** 0 = off (white), 1 = hours (red), 2 = minutes (green), 3 = both (blue). */
    fun fibonacciColors(hour24: Int, minute: Int, variant: Int = 0): IntArray {
        val h = (hour24 % 12).let { if (it == 0) 12 else it }
        val m = minute / 5
        val hourSets = subsetsSumming(h)
        val minuteSets = subsetsSumming(m)
        val hm = hourSets[Math.floorMod(variant, hourSets.size)]
        val mm = minuteSets[Math.floorMod(variant / 2, minuteSets.size)]
        return IntArray(5) { i ->
            val inH = hm and (1 shl i) != 0
            val inM = mm and (1 shl i) != 0
            when {
                inH && inM -> 3
                inH -> 1
                inM -> 2
                else -> 0
            }
        }
    }

    private fun subsetsSumming(target: Int): List<Int> {
        val result = mutableListOf<Int>()
        for (mask in 0 until 32) {
            var sum = 0
            for (i in 0 until 5) if (mask and (1 shl i) != 0) sum += fibSquares[i]
            if (sum == target) result += mask
        }
        return result.ifEmpty { listOf(0) }
    }

    fun fibonacciDecode(colors: IntArray): Pair<Int, Int> {
        var h = 0
        var m = 0
        colors.forEachIndexed { i, c ->
            if (c == 1 || c == 3) h += fibSquares[i]
            if (c == 2 || c == 3) m += fibSquares[i]
        }
        return h to m * 5
    }

    // ---------------- Dot-matrix 5x7 font ----------------

    private val dotFont: Map<Char, List<String>> = mapOf(
        '0' to listOf(" ### ", "#   #", "#  ##", "# # #", "##  #", "#   #", " ### "),
        '1' to listOf("  #  ", " ##  ", "  #  ", "  #  ", "  #  ", "  #  ", " ### "),
        '2' to listOf(" ### ", "#   #", "    #", "   # ", "  #  ", " #   ", "#####"),
        '3' to listOf("#####", "   # ", "  #  ", "   # ", "    #", "#   #", " ### "),
        '4' to listOf("   # ", "  ## ", " # # ", "#  # ", "#####", "   # ", "   # "),
        '5' to listOf("#####", "#    ", "#### ", "    #", "    #", "#   #", " ### "),
        '6' to listOf("  ## ", " #   ", "#    ", "#### ", "#   #", "#   #", " ### "),
        '7' to listOf("#####", "    #", "   # ", "  #  ", " #   ", " #   ", " #   "),
        '8' to listOf(" ### ", "#   #", "#   #", " ### ", "#   #", "#   #", " ### "),
        '9' to listOf(" ### ", "#   #", "#   #", " ####", "    #", "   # ", " ##  "),
        ':' to listOf("     ", "  #  ", "  #  ", "     ", "  #  ", "  #  ", "     "),
        ' ' to listOf("     ", "     ", "     ", "     ", "     ", "     ", "     ")
    )

    fun dotGlyph(c: Char): List<String> = dotFont[c] ?: dotFont.getValue(' ')

    // ---------------- Seven segment ----------------

    /** Segments a..g as bits 0..6. */
    fun sevenSegment(digit: Char): Int = when (digit) {
        '0' -> 0b0111111
        '1' -> 0b0000110
        '2' -> 0b1011011
        '3' -> 0b1001111
        '4' -> 0b1100110
        '5' -> 0b1101101
        '6' -> 0b1111101
        '7' -> 0b0000111
        '8' -> 0b1111111
        '9' -> 0b1101111
        else -> 0
    }

    // ---------------- Roman numerals ----------------

    fun roman(n: Int, watchmakerFour: Boolean = true): String {
        if (n == 4 && watchmakerFour) return "IIII"
        val values = intArrayOf(1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1)
        val symbols = arrayOf("M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I")
        var x = n
        val sb = StringBuilder()
        for (i in values.indices) {
            while (x >= values[i]) {
                x -= values[i]
                sb.append(symbols[i])
            }
        }
        return sb.toString()
    }

    fun bits(value: Int, count: Int): List<Boolean> = (count - 1 downTo 0).map { (value shr it) and 1 == 1 }

    /** Natural-language time used by the "Spoken" face, e.g. "quarter past seven". */
    fun spokenTime(hour24: Int, minute: Int): String {
        val names = listOf("twelve", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten", "eleven")
        val nextHour = names[(hour24 + 1) % 12]
        val hour = names[hour24 % 12]
        return when (minute) {
            0 -> "$hour o'clock"
            15 -> "quarter past $hour"
            30 -> "half past $hour"
            45 -> "quarter to $nextHour"
            in 1..29 -> "${numberWords(minute)} past $hour"
            else -> "${numberWords(60 - minute)} to $nextHour"
        }
    }

    fun numberWords(n: Int): String {
        val ones = listOf("zero", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten",
            "eleven", "twelve", "thirteen", "fourteen", "fifteen", "sixteen", "seventeen", "eighteen", "nineteen")
        val tens = listOf("", "", "twenty", "thirty", "forty", "fifty")
        return if (n < 20) ones[n] else if (n % 10 == 0) tens[n / 10] else "${tens[n / 10]}-${ones[n % 10]}"
    }
}
