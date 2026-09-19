---
name: knowledge-sync
description: >-
  Run this after EVERY code change, implementation, or configuration modification
  to propagate updates across the entire Wefit knowledge base — rules, skills,
  service docs, overview, changelog, and the project-orientation map. This skill
  ensures every "cell" of the project knows what changed. Never skip it.
---

# Knowledge Sync — Keep Every Cell Updated

> The Wefit project knowledge base is a living organism. Every time code changes,
> the knowledge must change with it. This skill tells you exactly what to update,
> in what order, and with what content, so no part of the project ever goes stale.

---

## When to Run This Skill

Run this skill automatically at the end of EVERY task that involves:

- Adding or modifying a Java class (entity, service, controller, repository, config, DTO)
- Adding, removing, or changing a REST endpoint
- Changing a database schema (new table, new column, new MongoDB field)
- Adding or modifying Kafka topics, producers, or consumers
- Changing configuration properties or environment variables
- Adding a new service or module
- Changing security, authentication, or routing
- Adding or modifying a rule (`.agents/rules/*.md`)
- Adding or modifying a skill (`.agents/skills/*/SKILL.md`)
- Any architectural or infrastructure change

---

## The Sync Checklist

Work through this checklist in order. Mark each item as you complete it.

### STEP 1 — Identify What Changed

Before updating anything, answer these questions:

```
1. Which service(s) were modified? (eureka / configServer / apiGateway / userService / activityService / aiService)
2. What TYPE of change was it?
   [ ] New class added
   [ ] Existing class modified
   [ ] Endpoint added/changed/removed
   [ ] Entity/DTO field added/changed/removed
   [ ] Database schema changed
   [ ] Kafka config changed
   [ ] Configuration property changed
   [ ] Environment variable added/changed
   [ ] New service added
   [ ] Security/auth changed
   [ ] Routing changed
   [ ] Rule file changed
   [ ] Skill file changed
3. Is this a cross-cutting concern (affects multiple services or the system as a whole)?
```

---

### STEP 2 — Update Per-Service Documentation

**Always update** the service doc(s) for every service whose code changed.

| Service changed   | Document to update                            |
|-------------------|-----------------------------------------------|
| `eureka`          | `c:\Wefit\project\services\eureka.md`         |
| `configServer`    | `c:\Wefit\project\services\config_server.md`  |
| `apiGateway`      | `c:\Wefit\project\services\api_gateway.md`    |
| `userService`     | `c:\Wefit\project\services\user_service.md`   |
| `activityService` | `c:\Wefit\project\services\activity_service.md` |
| `aiService`       | `c:\Wefit\project\services\ai_service.md`     |

**What to update in the service doc:**

- New endpoint? Add it to the API Endpoints table (method, path, description, request, response).
- Entity field added/removed? Update the database schema table.
- New class? Add a new subsection under the Class-by-Class section.
- Config property changed? Update the Configuration table.
- New env var? Add it to the relevant config table.
- Design decision made? Add it to the Design Decisions section.
- Bug fixed? Add a note explaining what the bug was and how it was fixed.

---

### STEP 3 — Update the Project Overview

Update `c:\Wefit\project\Wefit_Project_Overview.md` **only when** the change is cross-cutting:

| Change type                                | Update section in Overview                  |
|--------------------------------------------|---------------------------------------------|
| New service added                          | Service Index table, Architecture diagram   |
| New inter-service REST call added          | Inter-Service Communication (Synchronous)   |
| New Kafka topic or producer/consumer       | Inter-Service Communication (Asynchronous)  |
| New environment variable                   | Environment Variables table                 |
| Security or auth change                    | Security & Authentication section           |
| New route in API Gateway                   | Routing Configuration (in Gateway doc AND Overview) |
| Startup order changed                      | Startup Order section                       |
| New planned feature                        | Roadmap table                               |

---

### STEP 4 — Update the Relevant Rule Files

Check each rule file and update if the change affects it:

| Rule file                      | Update when...                                                        |
|-------------------------------|-----------------------------------------------------------------------|
| `architecture.md`             | Architecture, communication pattern, startup order, or ports changed |
| `api-reference.md`            | Any endpoint was added, changed, or removed                          |
| `database-schema.md`          | Any entity field or schema changed                                   |
| `workflows.md`                | Any sequence diagram or data flow changed                            |
| `coding-standards.md`         | A new coding pattern was established or an existing one changed      |
| `project-specific-patterns.md`| A new project-specific pattern was introduced                        |
| `security.md`                 | Any auth, JWT, or Keycloak config changed                            |
| `dependencies.md`             | A new dependency was added to a `pom.xml`                            |
| `troubleshooting.md`          | A bug was discovered and fixed — document it for future reference    |
| `wefit-complete-doc.md`       | Any class, method, or technical detail changed — update the spec     |
| `project-overview.md`         | Service list, ports, or project status changed                       |
| `changelog.md`                | ALWAYS — every change must be logged here                            |

**You do NOT need to update ALL rules every time. Only update the ones relevant to what changed.**

---

### STEP 5 — Update the Changelog (ALWAYS)

**Always** append an entry to `c:\Wefit\.agents\rules\changelog.md` in this format:

```markdown
## [YYYY-MM-DD] — [Ticket ID or "Manual"]

### Changed
- **[Service]**: [What changed — class, method, endpoint, field]

### Added
- **[Service]**: [What was added]

### Fixed
- **[Service]**: [Bug fixed and how]

### Documentation Updated
- `project/services/{service}.md` — [What was updated]
- `rules/{file}.md` — [What was updated]
```

---

### STEP 6 — Update the project-orientation Skill (if needed)

Update `c:\Wefit\.agents\skills\project-orientation\SKILL.md` **only when**:

- A new skill was added — add it to Section 3 (Skills map).
- A new rule file was added — add it to Section 2 (Rules map).
- A new service was added — add it to Section 1 (service table) AND Section 5 (Key source files).
- A new "gotcha" was discovered — add it to Section 7 (Common Gotchas table).
- A coding constraint changed — update Section 6.
- The environment setup requirements changed — update Section 9.

---

### STEP 7 — Verify Consistency

After all updates, do a final cross-check:

```
[ ] Service doc reflects the actual current code
[ ] API endpoint tables match what the controller actually exposes
[ ] Entity/DTO field tables match the actual Java classes
[ ] Changelog has an entry for today's changes
[ ] No rule file contains stale/outdated information
[ ] project-orientation skill still accurately maps everything
[ ] No hardcoded values in docs that have changed in config
```

---

## Rules for Writing Knowledge Updates

1. **Be specific, not vague.** "Added `GET /api/users/{id}` returning `UserResponseDto`" is correct. "Updated user endpoint" is wrong.
2. **Explain WHY, not just WHAT.** Every design decision needs a rationale. Future engineers need to understand the reasoning.
3. **Update tables precisely.** Don't describe table changes in prose — add/modify the actual row in the table.
4. **Cross-reference.** If a change in `activityService` affects how `aiService` works, mention it in both service docs.
5. **Don't paraphrase code — show the pattern.** For new patterns, include the actual code snippet or field name.
6. **Never delete existing gotchas or design decisions.** Only add and amend. Historical context is valuable.

---

## Quick Reference: Which Files to Update for Common Scenarios

### "I added a new REST endpoint"
- [ ] `project/services/{service}.md` — Add to API Endpoints table
- [ ] `.agents/rules/api-reference.md` — Add to endpoint reference
- [ ] `.agents/rules/changelog.md` — Log it

### "I added a new entity field / MongoDB field"
- [ ] `project/services/{service}.md` — Update schema table
- [ ] `.agents/rules/database-schema.md` — Update schema
- [ ] `.agents/rules/changelog.md` — Log it

### "I added a new service"
- [ ] Create `project/services/{new_service}.md` (full deep-dive doc)
- [ ] `project/Wefit_Project_Overview.md` — Add to Service Index and Architecture
- [ ] `.agents/rules/architecture.md` — Add to architecture
- [ ] `.agents/rules/project-overview.md` — Add to services summary
- [ ] `.agents/skills/project-orientation/SKILL.md` — Add to Section 1 and Section 5
- [ ] `.agents/rules/changelog.md` — Log it

### "I added a new environment variable"
- [ ] `project/Wefit_Project_Overview.md` — Add to Environment Variables table
- [ ] `project/services/{service}.md` — Add to Configuration table
- [ ] `.env.example` — Add with a clear comment
- [ ] `.agents/rules/changelog.md` — Log it

### "I fixed a bug"
- [ ] `project/services/{service}.md` — Note the fix in the relevant section
- [ ] `.agents/rules/troubleshooting.md` — Document symptom + fix for future reference
- [ ] `.agents/skills/project-orientation/SKILL.md` — Add to Common Gotchas if it's a recurring trap
- [ ] `.agents/rules/changelog.md` — Log it

### "I changed a Kafka topic or config"
- [ ] `project/services/{producer_service}.md` — Update Kafka config table
- [ ] `project/services/{consumer_service}.md` — Update Kafka config table
- [ ] `project/Wefit_Project_Overview.md` — Update async communication table
- [ ] `.agents/rules/architecture.md` — Update Kafka section
- [ ] `.agents/rules/changelog.md` — Log it

### "I added a new rule file or skill"
- [ ] `.agents/skills/project-orientation/SKILL.md` — Add to Rules map (Section 2) or Skills map (Section 3)
- [ ] `.agents/rules/changelog.md` — Log it

### "I changed security / auth config"
- [ ] `project/services/api_gateway.md` — Update relevant section
- [ ] `project/Wefit_Project_Overview.md` — Update Security section
- [ ] `.agents/rules/security.md` — Update rule
- [ ] `.agents/rules/changelog.md` — Log it
