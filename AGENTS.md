# AGENTS.md

## Project Overview

This repository contains a Kotlin Multiplatform sheet music rendering library and a sample app.

The project is split into three modules:

- `sheetmusic-core` contains the platform-independent score model, SMuFL glyph metrics, and layout engine. It has no Compose dependency.
- `sheetmusic-compose` renders laid-out scores with Compose Multiplatform and bundles the Bravura music font.
- `sample` demonstrates the library as a desktop app and in JS and Wasm browser targets.

The library currently renders a single treble-clef staff with basic note values, stems, ledger lines, and barlines. It does not currently parse MusicXML, play audio, edit scores, beam notes, or break scores across systems.

## Code Style and Conventions

- Follow [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html).
- Use four spaces for indentation, consistent with the existing files.
- Name test functions `testXxx` without backticks.
- Use descriptive variable and function names.
- Prefer functional patterns where they make the code clearer.
- Use type-safe builders and DSLs for configuration where appropriate.
- Document public APIs with KDoc comments.
- Do not suppress compiler warnings without a good reason.
- Focus on simple solutions first.
- Apply DRY where it helps; prefer small duplication when it keeps components simple and loosely coupled.
- Avoid wildcard imports.
- Keep score model and layout code platform-independent in `sheetmusic-core`; keep rendering and font loading in `sheetmusic-compose`.

## Branches

For tracked work, use branches in the format `lunar-score-${ISSUE_NUMBER}-${task-description}`, in lowercase.

For example, issue 170 about a TODO could use `lunar-score-170-complete-todo`.

Start work from a fresh branch based on the latest `origin/master` when a remote is configured. Do not invent a remote or branch target when none is configured.

Create feature branches without inheriting the base branch's upstream, for example:

```bash
git switch --no-track -c lunar-score-<issue-number>-<task-description> origin/master
```

A feature branch's upstream must be the remote feature branch with exactly the same name as the local branch. Never configure a feature branch to track `origin/master`, `origin/main`, or another differently named branch.

If the same-name remote feature branch already exists, configure it as the upstream with `git branch --set-upstream-to=origin/<feature-branch> <feature-branch>`. If it does not exist, leave the upstream unset until the human explicitly authorizes pushing. For that first authorized push, use `git push --set-upstream origin HEAD` to publish and track the same-name feature branch.

After creating or checking out a feature branch, verify its upstream with `git branch -vv`. Remove any inherited base-branch upstream with `git branch --unset-upstream` before continuing. Do not push directly to `master` or `main`; changes must go through a pull request.

## Commit Messages

Use `LUNAR-SCORE-${ISSUE_NUMBER}: <short description>` for issue-related commits. Add a blank line and more detail when useful.

`LUNAR-SCORE-00` is reserved for small changes without a corresponding issue and should be used sparingly.

Only commit or push when the human explicitly asks for it.

## Gradle Configuration and Dependencies

- Build scripts use Gradle Kotlin DSL (`*.gradle.kts`).
- Dependency and plugin versions are managed in `gradle/libs.versions.toml`.
- Prefer adding or updating versions and coordinates through the version catalog rather than hardcoding versions in module build files.
- Keep the Gradle wrapper and `kotlin-js-store/yarn.lock` in version control. Refresh the lockfiles after relevant web dependency changes.

## Tech Stack

- **Platforms:** Android, JVM desktop, iOS, JS, and Wasm, with target availability depending on the host.
- **Language:** Kotlin Multiplatform.
- **UI and rendering:** Compose Multiplatform.
- **Build system:** Gradle.
- **Dependency management:** `gradle/libs.versions.toml`.
- **Music glyphs:** Bravura, licensed under SIL OFL 1.1; retain its licence when changing the bundled font.

## Change Acceptance Requirements

Before considering a code change ready to merge:

1. The relevant project targets should compile successfully.
2. Relevant tests should pass.
3. Changes should preserve the separation between score modeling, layout, and rendering, and should avoid regressions across supported platform targets.

Use the Gradle wrapper and choose commands that match the change and the capabilities of the current host. For example:

```bash
./gradlew build
./gradlew allTests
```

Some targets have host requirements. In particular, linking iOS binaries and running iOS simulator tests require macOS with Xcode; web browser tests may require Chrome. Report unsupported or unverified targets clearly.

## Worktrees

When using a worktree, commit changes you make before finishing the task.

## Automated Actions

The implementation model should limit changes to the checked-out worktree. Do not create, update, or close GitHub issues or pull requests, publish artifacts, or perform other external actions unless the human explicitly asks.

For tracked work, use `lunar-score-<ticket-number>-<kebab-case-summary>` for the branch name; for example, `lunar-score-160-add-rest-layout`.

## Solutions

For a non-trivial bug fix that is likely to help future contributors, add a brief entry to `docs/SOLUTIONS.md`. Skip one-off fixes. Keep the note concise unless the issue was complex, and include external references when useful.

Use these headings:

**Symptom**:

**Cause**:

**Fix**:
