## ADDED Requirements

### Requirement: Repository-level agent guidance

The repository SHALL provide a root-level `AGENTS.md` that applies to the entire Reader project.

#### Scenario: Agent starts work in the repository

- **WHEN** an Agent begins work from the repository root
- **THEN** the Agent can read `AGENTS.md` to understand the project scope, collaboration rules, modification boundaries, and validation expectations

### Requirement: OpenSpec-first workflow for planned changes

The guidance SHALL instruct Agents to use OpenSpec changes for user-visible behavior changes, architecture constraints, or workflow rule changes unless the user explicitly asks for a direct small edit.

#### Scenario: User asks for a planned change

- **WHEN** the user asks for a plan or OpenSpec workflow
- **THEN** the Agent creates or updates an active OpenSpec change before modifying implementation code

### Requirement: Dirty worktree protection

The guidance SHALL require Agents to preserve existing user or unrelated changes in a dirty worktree.

#### Scenario: Existing unrelated changes are present

- **WHEN** the working tree contains changes outside the current task
- **THEN** the Agent only edits files required by the current task and does not revert, reformat, stage, or clean unrelated files

### Requirement: Android project conventions

The guidance SHALL describe the Reader project as a single-module Android/Kotlin app and document the expected UI, ViewModel, Repository, persistence, and network boundaries.

#### Scenario: Agent edits Android code later

- **WHEN** an Agent needs to change Android implementation files
- **THEN** the Agent can use `AGENTS.md` to choose the correct layer and avoid coupling UI code directly to persistence, network, or parsing details

### Requirement: Scope-based validation

The guidance SHALL define validation commands by change scope and require Agents to report checks that were not run.

#### Scenario: Documentation-only change

- **WHEN** a change only updates OpenSpec artifacts or `AGENTS.md`
- **THEN** the Agent runs OpenSpec validation and `git diff --check`, and explains why Gradle or device checks were not run

#### Scenario: Runtime code change

- **WHEN** a change touches Kotlin, Android resources, DI, Room, Manifest, or startup paths
- **THEN** the Agent selects relevant Gradle and device validation commands based on the touched risk area
