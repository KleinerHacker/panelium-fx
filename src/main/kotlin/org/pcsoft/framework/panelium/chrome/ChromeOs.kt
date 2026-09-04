package org.pcsoft.framework.panelium.chrome

/**
 * The host operating system as far as the window chrome cares: it decides the caption button side,
 * their order and which native look (`chrome-caption-buttons.css`) is applied. Detected once from
 * `os.name`; overridable per [ChromePane] (see [ChromePane.captionOsProperty]) for tests and demos.
 */
enum class ChromeOs {
    WINDOWS,
    MAC,
    LINUX,
    OTHER,
    ;

    /** Lower-case style-class suffix used both on the button box and inside the stylesheet. */
    internal val styleClass: String
        get() = name.lowercase()

    companion object {

        /** Resolves the current OS from the `os.name` system property, [OTHER] when unrecognised. */
        fun detect(): ChromeOs = fromOsName(System.getProperty("os.name").orEmpty())

        internal fun fromOsName(osName: String): ChromeOs {
            val name = osName.lowercase()
            return when {
                name.contains("win") -> WINDOWS
                name.contains("mac") || name.contains("darwin") -> MAC
                name.contains("nux") || name.contains("nix") || name.contains("aix") -> LINUX
                else -> OTHER
            }
        }
    }
}
