# Changelog

## 0.2.0 — Complete feature implementation and code optimization

- Implemented Player-to-Player Trading (`TradeWithPlayerCommand` & interactive JavaFX dialog).
- Centralized Victory Condition checking and tie-breaker logic in `StandardGameEngine`.
- Completed all spatial placement and structure construction workflows (MVPs, Partnerships, Unicorns, Auditor movement, Tax discard, Longest Network award).
- Optimized codebase by removing dead code (`phaseTwo()` stubs), eliminating redundant UI victory checking loops, and centralizing magic numbers into `GameConstants`.

## 0.1.0 — Phase 1 architecture baseline

- Added Maven JavaFX scaffold.
- Added stable package layout and domain model.
- Added randomized board factory and dynamic market baseline.
- Added exception hierarchy, game-engine contract, command/memento infrastructure, async save/load, and UI-thread controller boundary.
- Added UML diagrams and model smoke verification.
