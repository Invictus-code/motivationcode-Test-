package de.motivationcode.app.data.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class Quote(
    val id: String,
    val text: String,
    val author: String,
    val source: String? = null,
    /** true, wenn der deutsche Text eine Übersetzung ist (Original in anderer Sprache). */
    val translated: Boolean = false,
    val categories: List<String>,
    /** Selbst angelegt statt mitgeliefert; steht nie in quotes.json. */
    @Transient val isOwn: Boolean = false,
)
