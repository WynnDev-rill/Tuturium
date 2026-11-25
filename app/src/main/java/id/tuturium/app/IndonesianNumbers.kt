package id.tuturium.app

import java.time.DateTimeException
import java.time.LocalDate
import java.util.Locale

private val digits = listOf("nol", "satu", "dua", "tiga", "empat", "lima", "enam", "tujuh", "delapan", "sembilan")
private val months = listOf("Januari", "Februari", "Maret", "April", "Mei", "Juni", "Juli",
    "Agustus", "September", "Oktober", "November", "Desember")
private val groups = listOf("", "ribu", "juta", "miliar", "triliun", "kuadriliun", "kuintiliun")
private val units = mapOf(
    "km" to "kilometer", "cm" to "sentimeter", "mm" to "milimeter", "m" to "meter",
    "kg" to "kilogram", "mg" to "miligram", "g" to "gram", "mL" to "mililiter", "ml" to "mililiter",
    "L" to "liter", "l" to "liter", "Hz" to "hertz", "kHz" to "kilohertz", "MHz" to "megahertz",
    "GB" to "gigabita", "MB" to "megabita", "km/jam" to "kilometer per jam",
    "m/s" to "meter per detik", "°C" to "derajat Celsius", "°F" to "derajat Fahrenheit", "°" to "derajat",
    "m²" to "meter persegi", "m³" to "meter kubik", "cm²" to "sentimeter persegi", "cm³" to "sentimeter kubik",
    "km²" to "kilometer persegi", "ha" to "hektare", "W" to "watt", "kW" to "kilowatt", "V" to "volt"
)
private val symbols = mapOf("+" to "tambah", "-" to "kurang", "−" to "kurang", "×" to "kali",
    "*" to "kali", "÷" to "bagi", "=" to "sama dengan", "≠" to "tidak sama dengan", "≥" to "lebih dari atau sama dengan",
    "≤" to "kurang dari atau sama dengan", ">" to "lebih dari", "<" to "kurang dari")
private val letters = listOf("a", "be", "ce", "de", "e", "ef", "ge", "ha", "i", "je", "ka", "el", "em",
    "en", "o", "pe", "ki", "er", "es", "te", "u", "ve", "we", "eks", "ye", "zet")
private const val number = """(?:[0-9]{1,3}(?:[.\u00a0\u202f][0-9]{3})+(?:,[0-9]+)?|[0-9]+(?:[.,][0-9]+)?)"""
private const val signed = """[+−-]?$number"""
private val unitTokens = units.keys.sortedByDescending(String::length).joinToString("|") { Regex.escape(it) }
private val roman = Regex("""M{0,3}(?:CM|CD|D?C{0,3})(?:XC|XL|L?X{0,3})(?:IX|IV|V?I{0,3})""")
private val superDigits = "⁰¹²³⁴⁵⁶⁷⁸⁹"
private val integerGrouping = Regex("""\d{1,3}(?:\.\d{3})+""")
private val englishGrouping = Regex("""\d{1,3}(?:,\d{3})+(?:\.\d+)?""")
private val rangeDash = Regex("[–—-]")
private val numberTokens = Regex(
    """(?<protected>(?i:https?://|www\.)[^\s<>]+|(?<![\p{L}\d._%+-])[\p{L}\d._%+-]+@[\p{L}\d.-]+\.[\p{L}]{2,}|\b[vV]\d+(?:\.\d+){2,}\b)""" +
    """|(?<date>\b(?:\d{4}-\d{2}-\d{2}|\d{1,2}(?<dateSep>[/.-])\d{1,2}\k<dateSep>\d{4})\b)""" +
    """|(?<clock>\b(?:(?i:pukul|jam)\s+\d{1,2}[.:]\d{2}|\d{1,2}:\d{2})(?::\d{2})?\b)""" +
    """|(?<phone>(?<![\p{L}\d])(?:\+62|08)(?:[ \t-]?\d){7,12}(?![\p{L}\d]))""" +
    """|(?<money>(?<![\p{L}\d])(?:(?i:Rp)\.?|IDR|US\$|USD|\$|€|EUR|£|GBP|¥|JPY)\s*(?:[+−-]?\d{1,3}(?:,\d{3})+\.\d+|$signed))""" +
    """|(?<measure>(?<![\p{L}\d])$signed\s*(?:$unitTokens)(?![\p{L}\d]))""" +
    """|(?<percent>(?<![\p{L}\d])$signed\s*[%‰])""" +
    """|(?<power>(?<![\p{L}\d])(?:$signed|[A-Za-z])(?:\^[+−-]?\d+|[⁻⁺]?[⁰¹²³⁴⁵⁶⁷⁸⁹]+))""" +
    """|(?<root>√\s*$signed)""" +
    """|(?<fraction>(?<![\p{L}\d/])\d+/\d+(?![\p{L}\d/]))""" +
    """|(?<ordinal>\b(?i:ke)[-–]\d+\b)""" +
    """|(?<roman>\b(?i:bab|bagian|abad|jilid|volume|kelas|fase|seri|tingkat|pasal)\s+[IVXLCDM]+\b)""" +
    """|(?<range>(?<![\p{L}\d])$number\s*[–—]\s*$number|(?<=\b(?:tahun|antara) )$number\s*-\s*$number)""" +
    """|(?<identifier>\b(?:[A-Z]{1,6}\d+[A-Z\d]*|\d+[A-Z]{1,6}[A-Z\d]*)\b)""" +
    """|(?<operator>(?<=[\p{L}\d)²³])\s*[+−×*÷=≠<>≤≥-]\s*(?=(?:\d|[A-Za-z]\b)))""" +
    """|(?<number>(?<![\p{L}\d/])$signed(?![\p{L}\d/]))"""
)

private fun Int.underThousand(): String = when {
    this < 10 -> digits[this]
    this == 10 -> "sepuluh"
    this == 11 -> "sebelas"
    this < 20 -> "${digits[this - 10]} belas"
    this < 100 -> digits[this / 10] + " puluh" + if (this % 10 == 0) "" else " ${(this % 10).underThousand()}"
    else -> (if (this < 200) "seratus" else "${digits[this / 100]} ratus") +
        if (this % 100 == 0) "" else " ${(this % 100).underThousand()}"
}

private fun String.spellDigits(): String = map { if (it in '0'..'9') digits[it - '0'] else it.toString() }.joinToString(" ")

private fun String.spellInteger(): String {
    if (length > 19 || (length > 1 && startsWith('0'))) return spellDigits()
    if (all { it == '0' }) return "nol"
    return reversed().chunked(3).mapIndexedNotNull { group, characters ->
        val value = characters.reversed().toInt()
        when {
            value == 0 -> null
            group == 1 && value == 1 -> "seribu"
            group == 0 -> value.underThousand()
            else -> "${value.underThousand()} ${groups[group]}"
        }
    }.asReversed().joinToString(" ")
}

private fun String.spellNumber(englishDecimal: Boolean = false): String {
    val prefix = when (first()) { '-', '−' -> "minus "; '+' -> "plus "; else -> "" }
    val raw = removePrefix("-").removePrefix("−").removePrefix("+").replace("\u00a0", "").replace("\u202f", "")
    val separator = when {
        englishDecimal && englishGrouping.matches(raw) -> raw.indexOf('.')
        ',' in raw -> raw.indexOf(',')
        integerGrouping.matches(raw) -> -1
        else -> raw.indexOf('.')
    }
    val whole = (if (separator < 0) raw else raw.take(separator)).filter { it in '0'..'9' }
    val integer = if (separator < 0) whole else whole.trimStart('0').ifEmpty { "0" }
    val spoken = integer.spellInteger()
    return prefix + spoken + if (separator < 0) "" else " koma ${raw.substring(separator + 1).spellDigits()}"
}

private fun dateWords(text: String): String {
    val fields = text.split('/', '-', '.')
    val iso = fields.first().length == 4
    val (year, month, day) = if (iso) fields.map(String::toInt) else fields.map(String::toInt).reversed()
    return try {
        val date = LocalDate.of(year, month, day)
        "${date.dayOfMonth.underThousand()} ${months[date.monthValue - 1]} ${year.toString().spellInteger()}"
    } catch (_: DateTimeException) {
        fields.joinToString(" garis miring ") { it.spellInteger() }
    }
}

private fun clockWords(text: String): String {
    val fields = text.split(Regex("\\s+"))
    val prefix = fields.first().takeIf { fields.size > 1 }?.lowercase(Locale.ROOT) ?: "pukul"
    val parts = fields.last().split(':', '.').map(String::toInt)
    if (parts[0] > 23 || parts.drop(1).any { it > 59 }) return text
    return "$prefix ${parts[0].underThousand()}" + if (parts.size == 2 && parts[1] == 0) " tepat"
    else " ${parts[1].underThousand()}" + if (parts.size == 3) " ${parts[2].underThousand()} detik" else ""
}

private fun romanWords(text: String): String {
    val value = text.substringAfterLast(' ')
    if (!roman.matches(value)) return text
    val table = mapOf('I' to 1, 'V' to 5, 'X' to 10, 'L' to 50, 'C' to 100, 'D' to 500, 'M' to 1000)
    val numbers = value.map { table.getValue(it) }
    val total = numbers.indices.sumOf { if (numbers[it] < numbers.getOrElse(it + 1) { 0 }) -numbers[it] else numbers[it] }
    return text.substringBeforeLast(' ') + " " + total.toString().spellInteger()
}

internal fun indonesianNumberReplacements(text: String): Sequence<Pair<IntRange, String>> =
    numberTokens.findAll(text).mapNotNull { token ->
        val raw = token.value
        fun isType(name: String) = token.groups[name] != null
        val words = when {
            isType("protected") -> raw
            isType("date") -> dateWords(raw)
            isType("clock") -> clockWords(raw)
            isType("phone") -> (if (raw.startsWith('+')) "plus " else "") + raw.filter(Char::isDigit).spellDigits()
            isType("money") -> {
                val prefix = raw.takeWhile { !it.isDigit() && it !in "+-−" }.trim().trimEnd('.')
                val currency = when (prefix.uppercase(Locale.ROOT)) {
                    "RP", "IDR" -> "rupiah"; "€", "EUR" -> "euro"; "£", "GBP" -> "pound sterling"
                    "¥", "JPY" -> "yen"; else -> "dolar Amerika"
                }
                val value = raw.dropWhile { !it.isDigit() && it !in "+-−" }
                "${value.spellNumber(currency != "rupiah")} $currency"
            }
            isType("measure") -> {
                val unit = units.keys.filter { raw.endsWith(it) }.maxByOrNull(String::length) ?: return@mapNotNull null
                "${raw.dropLast(unit.length).trim().spellNumber()} ${units.getValue(unit)}"
            }
            isType("percent") -> raw.dropLast(1).trim().spellNumber() + if (raw.endsWith('%')) " persen" else " permil"
            isType("power") -> {
                val base = raw.takeWhile { it != '^' && it !in superDigits && it !in "⁺⁻" }
                val exponent = raw.drop(base.length).removePrefix("^")
                    .map { if (it in superDigits) ('0' + superDigits.indexOf(it)) else when (it) { '⁻' -> '-'; '⁺' -> '+'; else -> it } }
                    .joinToString("")
                (if (base.any(Char::isDigit)) base.spellNumber() else base) + " pangkat " + exponent.spellNumber()
            }
            isType("root") -> "akar " + raw.drop(1).trim().spellNumber()
            isType("fraction") -> {
                val numerator = raw.substringBefore('/')
                val denominator = raw.substringAfter('/')
                if (raw == "1/2") "setengah" else "${numerator.spellInteger()} per ${denominator.spellInteger()}"
            }
            isType("ordinal") -> when (val value = raw.substring(3)) {
                "1" -> "pertama"; "2" -> "kedua"; "3" -> "ketiga"; else -> "ke ${value.spellInteger()}"
            }
            isType("roman") -> romanWords(raw)
            isType("range") -> raw.split(rangeDash).joinToString(" sampai ") { it.trim().spellNumber() }
            isType("identifier") -> raw.map { if (it in 'A'..'Z') letters[it - 'A'] else digits[it - '0'] }.joinToString(" ")
            isType("operator") -> " ${symbols.getValue(raw.trim())} "
            else -> raw.spellNumber()
        }
        if (words == raw) null else token.range to words
    }
