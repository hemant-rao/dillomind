# DilloMind — Multi-Language Content Authoring Guide

DilloMind is an offline memory-training app. All practice content for a language lives in a
single JSON file under `app/src/main/assets/content/`. **Adding a language = adding one JSON file
+ one line in the manifest.** No code changes, no rebuild logic.

This guide gives you (1) the exact file format, (2) how to register a new language, and
(3) a ready-to-paste prompt you can give to any AI (ChatGPT, Gemini, Claude, etc.) to generate a
complete, year-long content file for any language. Generate the file elsewhere, paste it in here.

---

## 1. Where files go

```
app/src/main/assets/content/
├── manifest.json      # list of languages the app offers in Settings
├── en.json            # English fun content (stories / poems / jokes)
├── hi.json            # Hindi (full sample)
└── <code>.json        # one file per language you add
```

- `<code>` is a short language code: `en`, `hi`, `es`, `fr`, `ur`, `bn`, `ta`, `mr`, `de`, `ar`…
- English (`en`) ALSO has a large built-in core baked into the app, so `en.json` only needs the
  fun categories. **Every other language file must contain the full content** (words, sentences,
  paragraphs) *plus* the fun categories, because there is no built-in core for them.

## 2. Register the language (manifest.json)

Add one object to the `languages` array:

```json
{ "code": "es", "name": "Spanish", "nativeName": "Español", "localeTag": "es-ES" }
```

| Field        | Meaning                                                                 |
|--------------|-------------------------------------------------------------------------|
| `code`       | must match the file name (`es` → `es.json`)                             |
| `name`       | English name (fallback label)                                          |
| `nativeName` | how speakers write it — shown in the picker (`Español`, `हिन्दी`, `اردو`) |
| `localeTag`  | BCP-47 tag used for **voice recall** speech recognition (`es-ES`, `hi-IN`, `ur-PK`, `fr-FR`) |

## 3. Content file format

```json
{
  "entries": [
    { "type": "WORD",      "content": "…", "difficulty": "EASY",   "category": "Focus",   "chapter": 1 },
    { "type": "SENTENCE",  "content": "…", "difficulty": "MEDIUM", "category": "Science", "chapter": 3 },
    { "type": "PARAGRAPH", "content": "…", "difficulty": "HARD",   "category": "Story",   "chapter": 2 }
  ]
}
```

Allowed values (use these EXACT strings):

- `type`: `WORD` · `SENTENCE` · `PARAGRAPH`
- `difficulty`: `EASY` · `MEDIUM` · `HARD`
- `category`: `General` · `Focus` · `Wisdom` · `Science` · `Visual` · `Story` · `Poem` · `Humor`
- `chapter`: integer `1`–`10`

### How chapters & unlocking work (important)
- For each `difficulty`, the app shows **10 chapters**. A chapter unlocks when the user passes at
  least one item in the previous chapter. So content should fill chapters **1→10** within each
  difficulty to create a real year-long journey.
- Target shape per language (matches the English core): **4 items per chapter, per difficulty,
  per type.** That is 10 chapters × 4 = **40 items per (type, difficulty)** → 120 WORD + 120
  SENTENCE + 120 PARAGRAPH = **360 core items**, plus fun content on top.
- Put fun categories (`Story`, `Poem`, `Humor`) on `PARAGRAPH` (and short jokes on `SENTENCE`),
  spread across chapters so each chapter has something enjoyable.

### Content style by difficulty
- `EASY`: short, everyday, encouraging. Words = 1 simple word. Sentences ≤ 8 words. Paragraphs 2–3 short sentences.
- `MEDIUM`: practical learning-science. Sentences 1 line. Paragraphs 3 sentences.
- `HARD`: precise, rich vocabulary / neuroscience. Longer paragraphs (3–4 sentences).

### Rules
- Valid JSON, UTF-8, no comments, no trailing commas.
- Write naturally in the target language and script (Devanagari, Arabic, Latin, etc.). Do NOT
  transliterate into English letters unless the language is genuinely romanized (e.g. Hinglish).
- Keep recall practical: items are spoken back via voice, so avoid extremely long paragraphs.
- No duplicates.

---

## 4. READY-TO-PASTE GENERATION PROMPT

Copy everything below, replace `<<LANGUAGE>>` and `<<LOCALE>>`, and send it to any AI. Then save
its output as `app/src/main/assets/content/<code>.json` and add the manifest line.

> You are generating offline content for **DilloMind**, a memory-training app. Produce a SINGLE
> valid JSON object (UTF-8, no comments, no trailing commas) for the language **<<LANGUAGE>>**,
> written naturally in that language's native script.
>
> Output shape:
> `{ "entries": [ { "type": ..., "content": ..., "difficulty": ..., "category": ..., "chapter": ... }, ... ] }`
>
> Allowed values exactly:
> - type: WORD | SENTENCE | PARAGRAPH
> - difficulty: EASY | MEDIUM | HARD
> - category: General | Focus | Wisdom | Science | Visual | Story | Poem | Humor
> - chapter: integer 1–10
>
> Generate the FULL set:
> 1. **WORDS** — 120 items: 40 EASY, 40 MEDIUM, 40 HARD. Within each difficulty, exactly 4 words
>    per chapter for chapters 1–10. EASY = simple everyday words about mind, focus, learning;
>    MEDIUM = richer cognitive vocabulary; HARD = advanced memory/neuroscience terms. Spread the
>    categories General/Focus/Wisdom/Science/Visual across them.
> 2. **SENTENCES** — 120 items: 40 EASY, 40 MEDIUM, 40 HARD, 4 per chapter (1–10) per difficulty.
>    Encouraging, practical tips about memory, focus, habits, and learning science.
> 3. **PARAGRAPHS** — 120 items: 40 EASY, 40 MEDIUM, 40 HARD, 4 per chapter (1–10) per difficulty.
>    Friendly memory habits (EASY) → applied learning science (MEDIUM) → precise neuroscience (HARD).
> 4. **FUN CONTENT** on top of the above, spread across chapters 1–10 and EASY/MEDIUM/HARD:
>    - `Story` (type PARAGRAPH): ~20 short fables/moral stories, 2–4 sentences each.
>    - `Poem` (type PARAGRAPH): ~15 short rhyming poems (4 lines) about learning, patience, growth.
>    - `Humor` (type SENTENCE for one-liner jokes, PARAGRAPH for funny paragraphs): ~20 clean,
>      family-friendly jokes and funny paragraphs, ideally memory/learning themed.
>
> Requirements: everything in natural <<LANGUAGE>> (native script). No duplicates. Keep paragraphs
> short enough to speak aloud. Make it genuinely enjoyable and useful — this competes with the best
> memory apps. Output ONLY the JSON object, nothing else.

After saving the file, add to `manifest.json`:
`{ "code": "<code>", "name": "<English name>", "nativeName": "<native name>", "localeTag": "<<LOCALE>>" }`

---

## 5. Validate before shipping
- Paste the file into any JSON validator (or run `python -m json.tool <file>`).
- Confirm `code` in the manifest matches the file name.
- Reinstall the app (the content reseeds automatically when the item count changes).
