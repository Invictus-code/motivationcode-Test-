package de.motivationcode.app.data.model

enum class Category(val id: String, val title: String) {
    MOTIVATION("motivation", "Motivation"),
    MUT("mut", "Mut"),
    DURCHHALTEN("durchhalten", "Schwere Zeiten"),
    GELASSENHEIT("gelassenheit", "Gelassenheit"),
    GLUECK("glueck", "Glück"),
    SELBSTVERTRAUEN("selbstvertrauen", "Selbstvertrauen"),
    LIEBE("liebe", "Liebe & Freundschaft"),
    WEISHEIT("weisheit", "Weisheit");

    companion object {
        fun fromId(id: String): Category? = entries.firstOrNull { it.id == id }
    }
}
