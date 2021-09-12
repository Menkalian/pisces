package de.menkalian.pisces.message

import net.dv8tion.jda.api.entities.Role
import java.nio.ByteBuffer
import java.time.OffsetDateTime
import java.time.temporal.TemporalAccessor

/**
 * Specification for a message. It will be transformed
 */
@Suppress("UNCHECKED_CAST") // T has to be the Subclass, so
abstract class MessageSpec<T> {

    protected abstract fun onUpdated()

    private var author: AuthorSpec = AuthorSpec("", "", "")
    private var title: String = ""
    private var color: Int = Role.DEFAULT_COLOR_RAW

    /**
     * Referred to as "description"
     */
    private var text: String = ""
    private val fields: MutableList<FieldSpec> = mutableListOf()
    private var timestamp: TemporalAccessor = OffsetDateTime.now()

    fun withAuthor(name: String? = null, url: String? = null, iconUrl: String? = null): T {
        author = AuthorSpec(name ?: author.name, url ?: author.url, iconUrl ?: author.iconUrl)
        return this as T
    }

    fun withTitle(title: String? = null): T {
        this.title = title ?: this.title
        return this as T
    }

    fun withText(text: String? = null): T {
        this.text = text ?: this.text
        return this as T
    }

    fun appendText(text: Any? = ""): T {
        this.text += text
        return this as T
    }

    fun withColor(red: Byte = 0, green: Byte = 0, blue: Byte = 0): T {
        color = ByteBuffer.allocate(4)
            .put(0x1F).put(red).put(green).put(blue)
            .getInt(0)
        return this as T
    }

    fun withTimestamp(timestamp: TemporalAccessor? = null): T {
        this.timestamp = timestamp ?: this.timestamp
        return this as T
    }

    init {
//        EmbedBuilder()
//            .addBlankField(bool)
//            .addField(name, value, inline)
//            .setFooter(text, icon)
//            .setImage(url)
//            .setThumbnail("")
//            .isValidLength
    }

}