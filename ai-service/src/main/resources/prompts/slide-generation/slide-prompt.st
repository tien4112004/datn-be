Generate slide content from the outline below, in {language}, following a {style} style.

Outline:
{outline}

**Output Format (Newline‐Delimited JSON):**
Emit one JSON object per line, with no extra text:

- Each object must have a `"type"` field (one of:
  – `"cover"`
  – `"contents"`
  – `"transition"`
  – `"content"`
  – `"closing"`
)
- And a `"data"` object whose fields depend on `"type"`:
  - **cover**: `{ "title": string, "text": string }`
  - **contents**: `{ "items": [ string, … ] }`
  - **transition**: `{ "title": string, "text": string }`
  - **content**: `{ "title": string, "items": [ { "title": string, "text": string }, … ] }`
  - **end**: `{ "title": string, "text": string }`

**Sample Slide Sequence:**
```jsonc
{ "type": "cover",     "data": { "title": "Event Driven Architecture", "text": "Understanding the key concepts and applications" } }
{ "type": "contents",  "data": { "items": [ "Chapter 1: Introduction", "Chapter 2: Components", "Chapter 3: Handling", "Chapter 4: Patterns", "Chapter 5: Use Cases", "Chapter 6: Implementation", "Chapter 7: Challenges", "Chapter 8: Future Trends" ] } }
{ "type": "transition","data": { "title": "Starting the journey", "text": "Exploring the foundations of EDA" } }
{ "type": "content",   "data": { "title": "Introduction to EDA", "items": [ { "title": "Definition", "text": "EDA determines program flow through events, which are state changes triggering actions." }, { "title": "Overview", "text": "EDA is used in real-time systems requiring high responsiveness and asynchronous processing." } ] } }
{ "type": "transition","data": { "title": "EDA components", "text": "Understanding the building blocks of EDA" } }
...
{ "type": "end" }
```
No additional wrappers, commentary, or arrays—only the raw slide objects, one per line.
