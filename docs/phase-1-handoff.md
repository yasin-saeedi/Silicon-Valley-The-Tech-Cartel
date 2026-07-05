# Phase 1 Handoff

## Completed

- Maven + JavaFX project shell
- Stable package layout
- Independent board graph structures (`Sector`, `Vertex`, `Edge`)
- Randomized balanced board factory with configurable map size
- Abstract `CompanyStructure` hierarchy with overridden `produce()` and `getVictoryPoints()`
- Player resource inventory, role assignment, dynamic base score
- Market state, dynamic purchase price increases, three-round inactivity decreases
- `Game` aggregate and `GameEngine` boundary
- Basic dice roll, resource production, market purchase, auditor move, and turn advance baseline
- Custom exception hierarchy
- Command + Memento infrastructure
- Async serialization-based save/load service
- JavaFX-safe controller threading boundary
- PlantUML and Mermaid class diagrams
- Model-only verification script

- Full initial placement snake order
- Distance-of-two placement validation
- Connected-partnership validation
- MVP and Partnership paid-build workflows
- MVP-to-Unicorn replacement workflow
- Complete regulatory crisis flow: discard choice, placement restrictions, UI prompts
- Longest-partnership award
- Victory detection and finished-game screen
- Player-to-player trading (`TradeWithPlayerCommand` & UI dialog)
- Full graphical board

## Remaining Future Enhancements

- AI Strategy candidate (`AiStrategy` interface for automated computer players)
- Advanced network synchronization / multiplayer server mode

## Low-conflict file ownership suggestion

| Track | Primary files |
|---|---|
| Board and spatial rules | `model/board/**`, `model/structure/**`, future spatial validators |
| Turn, player, market, persistence | `model/player/**`, `model/market/**`, `model/game/**`, `persistence/**` |
| Shared review and UI | `controller/**`, `view/**`, command classes, docs, integration tests |

Before merging a track, rebase on the shared scaffold and avoid changing public method signatures without agreement.
