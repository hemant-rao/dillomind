package com.example.data

import com.squareup.moshi.JsonClass

/**
 * Schema for the offline, asset-bundled content library.
 *
 * Content lives in `assets/content/` as plain JSON so that adding a whole new
 * language is just dropping in one file — no Kotlin changes, no rebuild logic.
 *
 *   assets/content/manifest.json   -> list of available languages
 *   assets/content/en.json         -> content entries for English
 *   assets/content/hi.json         -> content entries for Hindi
 *   assets/content/<code>.json     -> ...and so on
 *
 * See CONTENT_AUTHORING_PROMPT.md at the repo root for the exact format and a
 * ready-to-paste prompt you can hand to any AI to generate a new language file.
 */

@JsonClass(generateAdapter = true)
data class ContentManifest(
    val languages: List<LanguageMeta> = emptyList()
)

@JsonClass(generateAdapter = true)
data class LanguageMeta(
    val code: String,            // "en", "hi", "es", "fr", "ur"
    val name: String,            // English name shown as a fallback ("Hindi")
    val nativeName: String,      // Endonym shown in the picker ("हिन्दी")
    val localeTag: String? = null // BCP-47 tag for speech recognition ("hi-IN"); defaults to code
)

@JsonClass(generateAdapter = true)
data class LanguageContent(
    val entries: List<ContentEntry> = emptyList()
)

@JsonClass(generateAdapter = true)
data class ContentEntry(
    val type: String,       // "WORD" | "SENTENCE" | "PARAGRAPH"
    val content: String,
    val difficulty: String, // "EASY" | "MEDIUM" | "HARD"
    val category: String,   // "General" | "Focus" | "Wisdom" | "Science" | "Visual" | "Story" | "Poem" | "Humor"
    val chapter: Int        // 1..10
)

/** Lightweight option used by the language picker UI. */
data class LanguageOption(
    val code: String,
    val name: String,
    val nativeName: String,
    val localeTag: String
)
