package de.menkalian.pisces

import java.io.File

fun main() {
    val path = "C:\\Users\\kilia\\Downloads\\emoji_v12_table_data.html"
    val file = File(path)
    var singleLineText = file.readText().replace("\n", "")
    var i = 0
    var lastCategory: String = ""
    val emojis = mutableMapOf<String, String>()

    while (singleLineText.contains("<tr>")) {
        try {
            val startIdx = singleLineText.indexOf("<tr>") + 4
            val endIdx = singleLineText.indexOf("</tr>")
            val currentLine = singleLineText.substring(startIdx, endIdx)

            if (currentLine.contains("class=\"mediumhead\"")) {
                val regexMatcher = ".+>(.+)<\\/a.+".toRegex().toPattern().matcher(currentLine)
                if (regexMatcher.matches())
                    lastCategory = regexMatcher.group(1).toString()
            } else if (currentLine.contains("</td>")) {
                do {
                    val firstRegex = "<td class=\"code\">.+>U\\+([a-fA-Z\\d]{4,5})<\\/a>".toRegex().toPattern().matcher(currentLine)
                    val codepoint = if (firstRegex.find()) firstRegex.group(1).toInt(16) else continue
                    val secondRegex = "<td class=\"name\">(.+)<\\/td>".toRegex().toPattern().matcher(currentLine)
                    val emojiName = if (secondRegex.find()) secondRegex.group(1) else continue

                    val emojiString =
                        if (codepoint >= 0x10000) {
                            val high = ((codepoint - 0x10000) / 0x400) + 0xD800
                            val low = ((codepoint - 0x10000) % 0x400) + 0xDC00
                            "\\u${high.toString(16)}\\u${low.toString(16)}"
                        } else {
                            "\\u$codepoint"
                        }
                    val fieldName = (lastCategory + "_" + emojiName)
                        .uppercase()
                        .replace("\\s".toRegex(), "")
                        .replace("-", "_")
                        .replace(":", "_")
                        .replace("⊛", "")
                    emojis[fieldName] = emojiString
                } while (false)
            }

            singleLineText = singleLineText.substring(endIdx + 5)
            println(++i)
        } catch (ex: Exception) {
            singleLineText = singleLineText.substring(1)
        }
    }
    val ymlOut = File("Emoji.yml")
    ymlOut.writeText("emoji:")
    emojis.forEach {
        ymlOut.appendText(
            "  - name: ${it.key}\n" +
                    "    value: ${it.value}\n"
        )
    }

    val out = File("EmojiConstants.kt")
    out.writeText("package de.menkalian.pisces.util\n\n")
    emojis.forEach { name, unicode ->
        out.appendText(
            """
                /**
                 * Constant for Emoji $name
                 * *AUTO GENERATED*
                 */
                const val $name = "$unicode"
            """.trimIndent() + "\n"
        )
    }
}