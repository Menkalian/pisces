package de.menkalian.pisces.message.spec

import net.dv8tion.jda.api.entities.Role
import java.nio.ByteBuffer
import java.time.OffsetDateTime
import java.time.temporal.TemporalAccessor

/**
 * Datenklasse zur Speicherung aller relevanten Informationen zu einer Nachricht.
 *
 * @param T Klasse, die von den Builder-Methoden zurückgegeben werden soll.
 *          Üblicherweise handelt es sich hierbei um die Unterklasse, die von [MessageSpec] erbt, allerdings erlaubt Kotlin (noch) keine Selbstreferenzen in Generics.
 *
 * @property author Informationen zum Autor der Nachricht
 * @property title Überschrift der Nachricht
 * @property color Farbe der Seitenleiste der Nachricht
 * @property text Text der Nachricht außerhalb von Feldern. Wird von Discord als `description` bezeichnet.
 * @property imageUrl Url des Bildes, das neben der Nachricht angezeigt wird (für den Unterschied zwischen Thumbnail und Image bitte die Dokumentation zu Discord Embedded Messages anschauen)
 * @property thumbnailUrl Url des Thumbnails, das neben der Nachricht angezeigt wird (für den Unterschied zwischen Thumbnail und Image bitte die Dokumentation zu Discord Embedded Messages anschauen)
 * @property fields Zusätzliche Textfelder dieser Nachricht. Es wird empfohlen verschiedene Abschnitte der Nachricht in eigene Felder zu packen, damit das Rendern mehrerer Seiten der Nachricht einfacher ist.
 * @property timestamp Zeitpunkt, der unter der Nachricht angezeigt werden soll (Dies ist **nicht** das gleiche wie der Sendezeitpunkt der Nachricht laut Discord)
 * @property footerText Zusatztext, der unter der Nachricht angezeigt wird
 * @property footerIconUrl URL, die erreicht werden kann, wenn man den Footer der Nachricht anklickt.
 */
@Suppress("UNCHECKED_CAST") // T has to be the Subclass, so
abstract class MessageSpec<T> {

    /**
     * Methode, die aufgerufen wird, nachdem sich die Werte von Feldern geändert haben.
     */
    protected abstract fun onUpdated()

    protected var author: AuthorSpec = AuthorSpec("", "", "")
    protected var title: String = ""
    protected var color: Int = Role.DEFAULT_COLOR_RAW
    protected var text: String = ""
    protected var imageUrl: String = ""
    protected var thumbnailUrl: String = ""
    protected val fields: MutableList<FieldSpec> = mutableListOf()
    protected var timestamp: TemporalAccessor = OffsetDateTime.now()
    protected var footerText: String = ""
    protected var footerIconUrl: String = ""

    /**
     * Setzt neue Autorendaten.
     * Ein Parameterwert `null` bewirkt, dass der bisherige Wert erhalten bleibt.
     *
     * @see AuthorSpec
     *
     * @param name Angezeigter Autorenname
     * @param url Verlinkung, die hinter dem Autorennamen hinterlegt wird
     * @param iconUrl Url des Bildes, das für den Autor angezeigt wird
     */
    fun withAuthor(name: String? = null, url: String? = null, iconUrl: String? = null): T {
        author = AuthorSpec(name ?: author.name, url ?: author.url, iconUrl ?: author.iconUrl)
        onUpdated()
        return this as T
    }

    /**
     * Setzt einen neuen Titel der Nachricht.
     * Ein Parameterwert `null` bewirkt, dass der bisherige Wert erhalten bleibt.
     *
     * @param title neuer Titel der Nachricht
     */
    fun withTitle(title: String? = null): T {
        this.title = title ?: this.title
        onUpdated()
        return this as T
    }

    /**
     * Setzt einen neuen Text der Nachricht.
     * Ein Parameterwert `null` bewirkt, dass der bisherige Wert erhalten bleibt.
     *
     * @param text Neuer Text der Nachricht
     */
    fun withText(text: String? = null): T {
        this.text = text ?: this.text
        onUpdated()
        return this as T
    }

    /**
     * Fügt Text zur bestehenden Nachricht hinzu.
     * Bei einem Wert `null` wird explizit der String "null" an die Nachricht angehängt.
     *
     * @param text Objekt, dessen String-Darstellung an die Nachricht angehängt werden soll.
     */
    fun appendText(text: Any? = ""): T {
        this.text += text
        onUpdated()
        return this as T
    }

    /**
     * Entfernt alle Felder von dieser Nachricht.
     */
    fun clearFields(): T {
        fields.clear()
        return this as T
    }

    /**
     * Fügt ein leeres (Platzhalter) Feld zu dieser Nachricht hinzu.
     *
     * @param isInline Ob das Feld inline dargestellt werden soll.
     */
    fun addBlankField(isInline: Boolean): T {
        fields.add(FieldSpec(inline = isInline, blank = true))
        onUpdated()
        return this as T
    }

    /**
     * Fügt ein Feld mit dem angegebenen Inhalt zur Nachricht hinzu.
     *
     * @param name Überschrift des Textfeldes
     * @param text Inhalt des Textfeldes
     * @param isInline Ob das Feld inline dargestellt werden soll.
     */
    fun addField(name: String = "", text: String = "", isInline: Boolean = false): T {
        fields.add(FieldSpec(name, text, isInline))
        onUpdated()
        return this as T
    }

    /**
     * Setzt eine neue Farbe der Nachricht.
     *
     * @param red Rote Komponente der Farbe in RGB-Darstellung
     * @param green Grüne Komponente der Farbe in RGB-Darstellung
     * @param blue Blaue Komponente der Farbe in RGB-Darstellung
     */
    fun withColor(red: Byte = 0, green: Byte = 0, blue: Byte = 0): T {
        color = ByteBuffer.allocate(4)
            .put(0x1F).put(red).put(green).put(blue)
            .getInt(0)
        onUpdated()
        return this as T
    }

    /**
     * Setzt eine neue Farbe der Nachricht.
     *
     * Die Farbe setzt sich folgendermaßen zu einem Integer zusammen:
     * ```txt
     * Byte:        | 00        | 01 | 02 | 02 |
     * Bedeutung:   | 1F od. FF | R  | G  | B  |
     * Beispiel:    | 1F        | 79 | AA | F7 |
     * ```
     */
    fun withColor(colorInt: Int): T {
        color = colorInt
        onUpdated()
        return this as T
    }

    /**
     * Setzt einen neuen Timestamp der Nachricht.
     * Ein Parameterwert `null` bewirkt, dass der bisherige Wert erhalten bleibt.
     *
     * @param timestamp neuer Timestamp der Nachricht
     */
    fun withTimestamp(timestamp: TemporalAccessor? = null): T {
        this.timestamp = timestamp ?: this.timestamp
        onUpdated()
        return this as T
    }

    /**
     * Setzt ein neues Bild für die Nachricht.
     * Ein Parameterwert `null` bewirkt, dass der bisherige Wert erhalten bleibt.
     *
     * (für den Unterschied zwischen Thumbnail und Image bitte die Dokumentation zu Discord Embedded Messages anschauen)
     * @see withThumbnail
     *
     * @param imageUrl Url des neuen Bildes
     */
    fun withImage(imageUrl: String? = null): T {
        this.imageUrl = imageUrl ?: this.imageUrl
        onUpdated()
        return this as T
    }

    /**
     * Setzt ein neues Thumbnail für die Nachricht.
     * Ein Parameterwert `null` bewirkt, dass der bisherige Wert erhalten bleibt.
     *
     * (für den Unterschied zwischen Thumbnail und Image bitte die Dokumentation zu Discord Embedded Messages anschauen)
     * @see withImage
     *
     * @param imageUrl Url des neuen Bildes
     */
    fun withThumbnail(imageUrl: String? = null): T {
        this.thumbnailUrl = imageUrl ?: this.thumbnailUrl
        onUpdated()
        return this as T
    }

    /**
     * Setzt neue Footer-Informationen für die Nachricht.
     * Ein Parameterwert `null` bewirkt, dass der bisherige Wert erhalten bleibt.
     *
     * @param text neuer Text des Footers
     * @param imageUrl neues Verlinkungsziel des Footers
     */
    fun withFooter(text: String? = null, imageUrl: String? = null): T {
        this.footerText = text ?: this.footerText
        this.footerIconUrl = imageUrl ?: this.footerIconUrl

        onUpdated()
        return this as T
    }
}