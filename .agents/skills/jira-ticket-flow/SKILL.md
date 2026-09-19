---
name: jira-ticket-flow
description: >-
  Use this skill whenever you are assigned a new Jira ticket or the user asks you to start development on a Jira ticket. This skill enforces a strict Git + Jira workflow (branching, planning, user approval, testing, and PR creation).
---

# Jira Ticket Development Flow

This is the standard ticket execution workflow that you MUST follow for every ticket going forward.

**Overall Flow:**
Project Orientation → Jira Ticket → Branch → Plan (+ Confluence Page) → Approval → Development → Testing → Knowledge Sync → Commit → PR → Review → Done

### 0. Read Project Orientation FIRST (MANDATORY)
*   Before doing anything else, read the `project-orientation` skill located at `c:\Wefit\.agents\skills\project-orientation\SKILL.md`.
*   This gives you the complete map: all rules, all skills, all documentation, key source files, coding constraints, and common gotchas.
*   Do NOT skip this step — starting without orientation leads to duplicated work, missed conventions, and broken patterns.

### 1. Check Jira first
*   Read the ticket and latest comments.
*   Any new instruction from the user in Jira takes priority.
*   Before starting each major step, re-check the ticket for new comments.

### 2. Create branch immediately
*   Checkout the `developer` or base branch.
*   Create a dedicated branch for the ticket (e.g., `feature/<ticket-id>-<description>`).
*   **Never** develop directly on the developer branch.

### 3. Create Technical Document (Implementation Plan)
*   Understand the requirement deeply — read the ticket, all comments, and the relevant service doc in `c:\Wefit\project\services\`.
*   Create a detailed Technical Document (the `implementation_plan.md` artifact) with ALL of the following sections:

    **A. Objective** — What problem this ticket solves and why.

    **B. Proposed Approach** — The technical strategy. Include design decisions and why alternatives were rejected.

    **C. Complete Changes Manifest** — A precise, exhaustive list of EVERY change that will be made:
    ```
    FILES MODIFIED:
      - path/to/File.java — What changes: [add field X, modify method Y, new endpoint Z]
    FILES CREATED:
      - path/to/NewFile.java — Purpose: [...]
    FILES DELETED:
      - path/to/OldFile.java — Reason: [...]
    CONFIG CHANGES:
      - configServer/.../service.yml — Property X added/changed
    ENV VARIABLE CHANGES:
      - NEW_VAR_NAME — Purpose, default value
    DATABASE CHANGES:
      - Table/Collection: field X added (type, nullable)
    ENDPOINT CHANGES:
      - NEW: POST /api/path — Request: {...}, Response: {...}
      - MODIFIED: GET /api/path — What changed
    KAFKA CHANGES:
      - Topic: X, Producer: Y, Consumer: Z — What changed
    ```

    **D. API / Database Changes** — Full request/response shapes, schema diffs.

    **E. Edge Cases & Risk** — What could go wrong and how it is handled.

    **F. Testing Strategy** — How the change will be verified.

    **G. Documentation Impact** — Which rule files, service docs, and skills will need to be updated after implementation.

*   **MANDATORY — Present in IDE:** Show the artifact to the user for approval before any code is written.

*   **MANDATORY — Create Confluence Page:** After the user approves (Step 4), use the `createConfluencePage` MCP tool to create a Confluence page in the team's space with:
    *   Title: `[TICKET-ID] Technical Design — <ticket summary>`
    *   Content: The full implementation plan in Confluence storage format
    *   Then use `addCommentToJiraIssue` to add a comment on the Jira ticket with the link to the Confluence page, so the design doc is always attached to the ticket.
    *   Example comment: `Technical Design Document created: [Confluence Page URL] — Implementation plan approved and development starting.`

### 4. WAIT FOR APPROVAL
*   **No development starts** until the user explicitly approves the Technical Document in the chat.
*   The user's approval is the strict checkpoint. Do not write any code before receiving it.
*   Once approved in the chat, you MUST add a comment to the Jira ticket stating that the implementation plan was approved in the chat and development is beginning.

### 5. Development
*   Start implementation **only after approval**.
*   Keep all work isolated to the ticket branch.
*   Check Jira comments before/while proceeding.

### 6. Testing Phase
*   Run appropriate unit/integration/manual tests.
*   Validate existing functionality for regressions.
*   Document the testing performed and results.

### 7. Run Knowledge Sync (MANDATORY)
*   Before committing, you MUST execute the full `knowledge-sync` skill: `c:\Wefit\.agents\skills\knowledge-sync\SKILL.md`.
*   This is not optional. It propagates every change you made across the entire knowledge base:
    *   Per-service docs in `project/services/`
    *   Project overview in `project/Wefit_Project_Overview.md`
    *   Relevant rule files in `.agents/rules/`
    *   Changelog entry in `.agents/rules/changelog.md`
    *   `project-orientation` skill if new skills, rules, or services were added
*   Follow the Quick Reference section in the knowledge-sync skill to identify exactly which files need updating for the type of change made.
*   **Update the Confluence page** created in Step 3 (using `updateConfluencePage`) to reflect any changes made during development that diverged from the original plan. The Confluence page must always match the final implementation.
*   After all docs are updated, add a final Jira comment listing what documentation was updated.

### 8. Commit
*   Create a clean, meaningful commit (e.g., `git commit -m "[<TICKET-ID>] <Message>"`).
*   Push the ticket branch to the remote repository.

### 9. Raise PR
*   Raise a Pull Request targeting the `developer` or base branch.
*   Include in the PR:
    *   What changed
    *   Why
    *   Testing performed
    *   Relevant Jira ticket
    *   Any known limitations
*   **Transition the Jira ticket** status to "In Review" (or equivalent, e.g., Transition ID 31).
*   **MANDATORY:** Once the PR is raised, you MUST add a comment to the Jira ticket with a link to the PR and a brief summary of the changes.

### 10. Under Review & Final Approval
*   Once the PR is raised, the Jira ticket stays "In Review".
*   It **does not** move to "Done" just because development is complete.
*   The user's review/approval is the final gate.
*   Only after the user's approval and PR merge should the ticket be transitioned to "Done" (Transition ID 41).

### Communication Rule
*   Jira is the source of truth. Treat communication about the ticket as happening through Jira comments or explicitly documented checkpoints.
*   Operating principle: Read → Plan → Get Approval → Build → Test → Commit → PR → Review → Done.
