Generate a structured presentation outline based on the user’s topic "{topic}" in {language}.)
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


**Output Format (Markdown):**
```
# Main Title
## Section Heading
- Key point
```
• Use a single `#` for the title.  
• Use `##` for top-level sections.  
• Use `-` for individual bullet points.  
• Do not include any extra commentary or explanatory text outside the Markdown structure.