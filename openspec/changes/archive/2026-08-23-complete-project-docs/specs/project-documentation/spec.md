## ADDED Requirements

### Requirement: README.md provides current project onboarding

The repository SHALL maintain a root-level `README.md` that helps a new maintainer understand the Reader project scope, current Android module structure, local development entry points, validation commands, and maintenance boundaries.

#### Scenario: Developer opens the repository

- **WHEN** a developer opens the repository root
- **THEN** `README.md` describes Reader as a single-module Android/Kotlin app
- **AND** it identifies `./gradlew` as the Gradle entry point
- **AND** it lists the main project areas needed for ordinary maintenance, including app source, tests, screenshots/APK artifacts when present, OpenSpec artifacts, and project guidance files

#### Scenario: Developer wants to build or validate locally

- **WHEN** a developer reads `README.md` for local setup
- **THEN** it provides the expected environment and common commands for building or testing the project
- **AND** it distinguishes documentation-only validation from Android build, lint, unit test, and device validation

### Requirement: README.md preserves useful existing context while correcting stale claims

The repository SHALL update the existing README content instead of discarding useful project history, screenshots, download references, and upstream acknowledgements, while avoiding claims that cannot be verified from the repository.

#### Scenario: Existing README content is still useful

- **WHEN** existing README sections such as screenshots, reference projects, APK link, or project background remain relevant
- **THEN** the updated README keeps or reorganizes that content
- **AND** it avoids presenting unverifiable links or old statements as newly confirmed facts

#### Scenario: Existing APK link cannot be proven latest

- **WHEN** the README keeps an existing APK download link
- **AND** the repository evidence does not prove that link is the latest release
- **THEN** the README presents it as an existing APK download link or historical download reference
- **AND** it does not label the link as latest

#### Scenario: Existing README content conflicts with current repository evidence

- **WHEN** old README text conflicts with current Gradle configuration, source layout, OpenSpec artifacts, or project guidance
- **THEN** the updated README uses the current repository evidence
- **AND** it removes, rewrites, or qualifies the stale claim

### Requirement: CHANGELOG.md records verifiable project changes

The repository SHALL provide a root-level `CHANGELOG.md` that records project changes in a stable structure, beginning with an `Unreleased` section and only including historical version entries that can be supported by repository evidence.

#### Scenario: Maintainer prepares or reviews a change

- **WHEN** a maintainer needs to understand recent project changes
- **THEN** `CHANGELOG.md` includes an `Unreleased` section
- **AND** entries are grouped in readable categories such as added, changed, fixed, or docs when applicable

#### Scenario: Historical release evidence is incomplete

- **WHEN** a version date or release content cannot be confirmed from repository tags, files, commits, or existing documented artifacts
- **THEN** `CHANGELOG.md` does not invent the missing date or content
- **AND** it does not reconstruct release notes from feature-list guesses
- **AND** it may state that older history is not fully reconstructed

### Requirement: Documentation changes do not imply runtime behavior changes

README and CHANGELOG updates SHALL not require Android implementation changes and SHALL report validation according to the documentation-only scope unless code, resources, Gradle configuration, Room schema, Manifest, or startup paths are modified.

#### Scenario: Only README.md and CHANGELOG.md are changed

- **WHEN** the implementation phase only updates root documentation files
- **THEN** OpenSpec validation and whitespace checks are sufficient required checks
- **AND** Gradle, lint, and device checks may be skipped with an explicit explanation
- **AND** Android source, resources, Gradle configuration, Room schema, Manifest, and tests remain unchanged

#### Scenario: Documentation work reveals required code changes

- **WHEN** completing README or CHANGELOG would require changing Android source, resources, Gradle configuration, database schema, Manifest, or startup behavior
- **THEN** the implementation must stop and update the plan or create a separate scoped change before modifying those runtime files
