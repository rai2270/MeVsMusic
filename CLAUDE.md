# Claude Code Guidelines

## Project Overview
MeVsMusic is a single-module Android game from 2012 (min SDK 16, target SDK 35) — you fly a ship against the spectrum of your own music. Pure Java codebase (the Kotlin plugin is not applied) rendering with OpenGL ES 2.0 via a vendored copy of the Rajawali engine, with audio analysis through the native BASS library.

## Code Style
- The fewer lines of code, the better. Keep changes minimal and concise.
- Prefer editing existing files over creating new ones.
- All code is Java — do not add Kotlin files.

## Architecture
- **Game code**: `app/src/main/java/mvm/` — `MeVsMusicActivity` (launcher/song list), `FlyingActivity` + `FlyingRenderer` (game), plus `settings/`, `particle/`, `material/`, `diplaylist/`
- **Vendored Rajawali 3D engine**: `app/src/main/java/r/` and `net/rbgrn/` — third-party engine source; prefer fixing game code over modifying engine code
- **BASS audio library**: Java wrapper in `com/un4seen/bass/`, prebuilt `libbass.so` binaries in `app/src/main/jniLibs/` — binary-only, cannot be rebuilt or modified
- The root `MeVsMusic/` directory is stale, untracked build output — ignore it; the real module is `app/`

## Working Directory
- Always work directly in the main project repository at its root. Never use git worktrees or isolated copies. Changes must be visible in the user's working branch immediately.
- Do NOT spawn the Agent tool with `isolation: "worktree"`. Do NOT call `EnterWorktree`. If you need a subagent, spawn it without isolation.
- If you find yourself running inside `.claude/worktrees/` (check `pwd`), stop, return to the main repo path, and tell the user — do not continue work in the worktree.
- If a previous session left a worktree behind under `.claude/worktrees/`, do not silently clean it up; flag it to the user, since it may contain uncommitted work.

## Process
- Take your time to investigate issues thoroughly. Recheck your work before presenting a solution.
- Read existing related code before making changes to understand patterns already in use.
- After finishing changes, compile the project to verify it builds: `./gradlew assembleDebug`
- There are no tests in this project.

## Don'ts
- Don't add Kotlin files.
- Don't use deprecated Android APIs when modern alternatives exist.
- Don't add new dependencies without asking first.
- Don't touch the prebuilt `libbass.so` binaries or the `com/un4seen/bass` wrapper.
