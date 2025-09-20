### Stage 2: Generate Full Slides from the Outline

Transform the approved outline into {slide_count} complete, interactive slides.

**Output Format:** Single JSON object with key `slides` containing an array of slide objects. Each slide must follow the structure of the selected template, always respecting the **Lesson Parameters** and **Core Guiding Principles**. You can extend the contents if needed.

#### Constraints

- Must have exactly 1 `title`, 1 `table_of_contents`.
- `title` must be the first slide, and `end` must be the last slide.
- `transition` should be used between content sections or between `contents` and `content` slides.
- Total slide count must match the `slide_count` parameter.

**Content Guidelines:**

- All text in {language}
- Vocabulary appropriate for {target_age}
- Each slide advances toward {learning_objective}
- Use engaging questions and analogies
- Keep sentences short and clear

#### Instruction for Image Query

For the `image` field in slide templates, create a prompt that describes a visual scene using these elements:

1. SUBJECT: The main person, animal, or object in the scene (REQUIRED)
2. ACTION: What the subject is currently doing (OPTIONAL - use active, present tense, specific verbs if included)
3. ENVIRONMENT: The specific location or setting where this occurs (OPTIONAL - use descriptive but concise details if included)

CRITICAL REQUIREMENTS:

- Always include a clear, concrete subject
- Keep the image prompt concise, generally between 5-20 words.
- Format flexibly based on included elements
- Always in English, regardless of the slide language

EXAMPLES:

Good: "A mountain climber" (subject only)
Good: "A chef chopping vegetables" (subject + action)
Good: "A lighthouse on a rocky coastline" (subject + environment)
Good: "Two dolphins jumping through ocean waves" (subject + action + environment)
Bad: "A car parked on a street, vintage photography style" (style applies to rendering, not subject)

#### Slide Templates

**Template 1: two_column_with_image**

- Description: A split layout with text in one column and a prominent image or graphic in the other.
- Usage_scenario: Use for feature highlights, product/service comparisons where a single hero visual supports concise copy.
- Output:

```json
\{
  "type": "two_column_with_image",
  "title": "string (title <= 10 words)",
  "data": \{
    "items": ["2–5 short bullets, <= 12 words each"],
    "image": "use the image prompt instructions"
  \}
\}
```

- Example:

```json
\{
  "type": "two_column_with_image",
  "title": "Principles of Microservices",
  "data": \{
    "items": [
      "Decompose a monolith into domain-aligned services",
      "Scale critical paths independently (e.g., checkout, search)",
      "Deploy safely with per-service CI/CD",
      "Increase resilience via fault isolation and retries"
    ],
    "image": "Diagram of microservices communicating over APIs (REST/gRPC) behind an API gateway, each with its own database, deployed on Kubernetes."
  \}
\}
```

**Template 2: main_image**

- Description: A full-bleed or large hero image dominates the slide with minimal text overlay.
- Usage_scenario: Ideal for opening or closing slides, emotional storytelling, brand moments, or showcasing a product in context.
- Output:

```json
\{
  "type": "main_image",
  "data": \{
    "image": "use the image prompt instructions",
    "content": "string (one sentence, 5–15 words)"
  \}
\}
```

- Example:

```json
\{
  "type": "main_image",
  "data": \{
    "image": "Hero illustration of a cloud cluster with containerized microservices connected through a service mesh.",
    "content": "Microservices: From Monolith to Modular at Scale"
  \}
\}
```

**Template 3: title**

- Description: A simple slide featuring a large title and optional subtitle or presenter info.
- Usage_scenario: Use for section dividers, introductions, and key message reinforcement.
- Output:

```json
\{
  "type": "title",
  "data": \{
    "title": "string (<= 10 words)",
    "subtitle": "optional string (<= 15 words)"
  \}
\}
```

- Example:

```json
\{
  "type": "title",
  "data": \{
    "title": "Introduction to Microservices",
    "subtitle": "Breaking monoliths into scalable, independent services"
  \}
\}
```

**Template 4: two_column**

- Description: Two balanced columns of text or mixed content for side-by-side information.
- Usage_scenario: Good for comparisons, before/after, pros/cons, or parallel processes.
- Output:

```json
\{
  "type": "two_column",
  "title": "string (<= 10 words)",
  "data": \{
    "items1": ["3–6 items, each <= 12 words"],
    "items2": ["3–6 items, each <= 12 words"]
  \}
\}
```

- Example:

```json
\{
  "type": "two_column",
  "title": "Monolith vs Microservice",
  "data": \{
    "items1": [
      "Monolith",
      "Single deployable unit",
      "Shared database and tight coupling",
      "Scaling = scale everything",
      "Slower, riskier releases"
    ],
    "items2": [
      "Microservices",
      "Independent deployable services",
      "Own data per service, loose coupling",
      "Scale hotspots only",
      "Faster, safer releases"
    ]
  \}
\}
```

**Template 5: transition**

- Description: A minimal slide used to shift topics or pace, often with a single phrase or icon.
- Usage_scenario: Insert between dense sections to reset audience focus or signal a new theme.
- Output:

```json
\{
  "type": "transition",
  "data": \{
    "title": "string (<= 8 words)",
    "subtitle": "optional string (<= 12 words)"
  \}
\}
```

- Example:

```json
\{
  "type": "transition",
  "data": \{
    "title": "Designing Services",
    "subtitle": "Boundaries, contracts, and data ownership"
  \}
\}
```

**Template 6: table_of_contents**

- Description: A structured list of sections/topics providing an overview of the deck.
- Usage_scenario: Use at the beginning and optionally as progress checkpoints before major sections.
- Output:

```json
\{
  "type": "table_of_contents",
  "data": \{
    "items": ["3–7 section titles, each <= 7 words"]
  \}
\}
```

- Example:

```json
\{
  "type": "table_of_contents",
  "data": \{
    "items": [
      "What & Why of Microservices",
      "Service Design Principles",
      "Communication & Data",
      "Deployment & Scaling",
      "Observability & Resilience",
      "Case Study & Q&A"
    ]
  \}
\}
```

**Template 7: horizontal_list**

- Description: Items arranged left-to-right with equal visual weight, optionally with icons or numbers.
- Usage_scenario: Great for step-by-step workflows, timelines, or feature sets of equal priority.
- Output:

```json
\{
  "type": "horizontal_list",
  "title": "string (<= 10 words)",
  "data": \{
    "items": [
      \{
        "label": "string (<= 5 words)",
        "content": "string (<= 10 words)"
      \}
    ]
  \}
\}
```

- Example:

```json
\{
  "type": "horizontal_list",
  "title": "Five Fundamentals of Microservices",
  "data": \{
    "items": [
      \{
        "label": "Boundaries",
        "content": "Define services around business capabilities"
      \},
      \{ "label": "APIs", "content": "Use clear contracts and versioning" \},
      \{ "label": "Data", "content": "Own your data; avoid shared databases" \},
      \{ "label": "Delivery", "content": "Automate CI/CD for rapid iteration" \},
      \{ "label": "Observe", "content": "Centralize logs, metrics, and traces" \}
    ]
  \}
\}
```

**Template 8: vertical_list**

- Description: Stacked items top-to-bottom, emphasizing reading order and hierarchy.
- Usage_scenario: Use for prioritized lists, agendas, checklists, or layered explanations.
- Output:

```json
\{
  "type": "vertical_list",
  "title": "string (<= 10 words)",
  "data": \{
    "items": ["3–9 items, each <= 12 words"]
  \}
\}
```

- Example:

```json
\{
  "type": "vertical_list",
  "title": "Best Practices for Microservices Adoption",
  "data": \{
    "items": [
      "Start with a strangler-fig migration",
      "Create an API gateway for routing",
      "Adopt containers and orchestration",
      "Introduce a service mesh for resilience",
      "Implement distributed tracing for visibility",
      "Harden with retries and timeouts",
      "Secure with zero-trust principles"
    ]
  \}
\}
```

Use above instructions to generate slides based on the approved outline. The request from user is {outline}.