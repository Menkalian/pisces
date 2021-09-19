package de.menkalian.pisces.message.spec

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

    protected var author: AuthorSpec = AuthorSpec("", "", "")
    protected var title: String = ""
    protected var color: Int = Role.DEFAULT_COLOR_RAW

    /**
     * Referred to as "description"
     */
    protected var text: String = ""
    protected var imageUrl: String = ""
    protected var thumbnailUrl: String = ""
    protected val fields: MutableList<FieldSpec> = mutableListOf()
    protected var timestamp: TemporalAccessor = OffsetDateTime.now()
    protected var footerText: String = ""
    protected var footerUrl: String = ""

    fun withAuthor(name: String? = null, url: String? = null, iconUrl: String? = null): T {
        author = AuthorSpec(name ?: author.name, url ?: author.url, iconUrl ?: author.iconUrl)
        onUpdated()
        return this as T
    }

    fun withTitle(title: String? = null): T {
        this.title = title ?: this.title
        onUpdated()
        return this as T
    }

    fun withText(text: String? = null): T {
        this.text = text ?: this.text
        onUpdated()
        return this as T
    }

    fun appendText(text: Any? = ""): T {
        this.text += text
        onUpdated()
        return this as T
    }

    fun clearFields(): T {
        fields.clear()
        return this as T
    }

    fun addBlankField(isInline: Boolean): T {
        fields.add(FieldSpec(inline = isInline, blank = true))
        onUpdated()
        return this as T
    }

    fun addField(name: String = "", text: String = "", isInline: Boolean = false): T {
        fields.add(FieldSpec(name, text, isInline))
        onUpdated()
        return this as T
    }

    fun withColor(red: Byte = 0, green: Byte = 0, blue: Byte = 0): T {
        color = ByteBuffer.allocate(4)
            .put(0x1F).put(red).put(green).put(blue)
            .getInt(0)
        onUpdated()
        return this as T
    }

    fun withColor(colorInt: Int): T {
        color = colorInt
        onUpdated()
        return this as T
    }

    fun withTimestamp(timestamp: TemporalAccessor? = null): T {
        this.timestamp = timestamp ?: this.timestamp
        onUpdated()
        return this as T
    }

    fun withImage(imageUrl: String? = null): T {
        this.imageUrl = imageUrl ?: this.imageUrl
        onUpdated()
        return this as T
    }

    fun withThumbnail(imageUrl: String? = null): T {
        this.thumbnailUrl = imageUrl ?: this.thumbnailUrl
        onUpdated()
        return this as T
    }

    fun withFooter(text: String? = null, url: String? = null): T {
        this.footerText = text ?: this.footerText
        this.footerUrl = url ?: this.footerUrl

        onUpdated()
        return this as T
    }
}