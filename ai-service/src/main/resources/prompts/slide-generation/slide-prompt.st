### Stage 2: Generate Full Slides from the Outline

Transform the approved outline into {slide_count} complete, interactive slides.

**Output Format:** Return each slide as a separate JSON object, with slides separated by "---" delimiter. Each slide must follow the structure of the selected template, always respecting the **Lesson Parameters** and **Core Guiding Principles**. You can extend the contents if needed.

Example format:
\{
  "type": "cover",
  "title": "Title here",
  "text": "Text here"
\}
---
\{
  "type": "content", 
  "title": "Title here",
  "items": [...]
\}
---
\{
  "type": "transition",
  "title": "Title here", 
  "text": "Text here"
\}

#### Constraints

- Must have exactly 1 `cover`, 1 `contents`, and 1 `end` slide.
- `cover` must be the first slide, and `end` must be the last slide.
- `transition` should be used between content sections or between `contents` and `content` slides.
- Total slide count must match the `slide_count` parameter.

#### Slide Templates

**Template 1: cover**
Use for: Opening/title slide


\{
  "type": "cover",
  "data": \{
    "title": "Main presentation title in {language}",
    "text": "Engaging subtitle or teaser text for primary students in {target_age} years old"
  \}
\}


**Template 2: contents**
Use for: Table of contents/agenda


\{
  "type": "contents",
  "data": \{
    "items": [
      "First main section title",
      "Second main section title",
      "Third main section title"
    ]
  \}
\}


**Template 3: transition**
Use for: Section dividers between main topics


\{
  "type": "transition",
  "data": \{
    "title": "New section title in {language}",
    "text": "Brief description of what's coming next"
  \}
\}


**Template 4: content**
Use for: Main educational content


\{
  "type": "content",
  "data": \{
    "title": "Slide topic in {language}",
    "items": [
      \{
        "title": "First key point",
        "text": "Detailed explanation with **markdown** for primary students in {target_age} years old"
      \},
      \{
        "title": "Second key point",
        "text": "Another explanation appropriate for primary students in {target_age} years old"
      \}
    ]
  \}
\}


**Template 5: end**
Use for: This is the signal to end the presentation.


\{
  "type": "end"
\}


**Content Guidelines:**

- All text in {language}
- Vocabulary appropriate for {target_age}
- Each slide advances toward {learning_objective}
- Use engaging questions and analogies
- Keep sentences short and clear

Use above instructions to generate slides based on the approved outline. The request from user is {outline}.