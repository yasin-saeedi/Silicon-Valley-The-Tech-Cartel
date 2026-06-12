# Architecture Notes — Phase 1

## Why this structure was chosen

The specification requires JavaFX, model/UI thread separation, custom exceptions, save/load, a `CompanyStructure` hierarchy, a dynamic market, and logical package separation. The scaffold therefore keeps the domain model independent of JavaFX and places all UI-thread transitions in `GameController`.

`Game` is the aggregate state. `GameEngine` is the gameplay boundary. Future rules are added to `StandardGameEngine` without allowing JavaFX classes to mutate model objects directly.

## Domain relationships

- A `Board` contains sectors, vertices, and edges as independent structures.
- A `Sector` produces one resource type unless it is a `REGULATORY_ZONE`.
- MVP and Unicorn structures occupy vertices. Partnerships occupy edges.
- A `Player` owns resources and structures. Base victory points are calculated from owned structures and the optional founder-role penalty.
- The `Market` owns visible dynamic prices and records whether each resource was bought during the current full round.
- `StandardGameEngine` orchestrates turns and will become the only place where rules are enforced.

## Patterns selected

### Command

Every user action should become a `GameCommand`. The controller submits commands to the game worker thread. This provides a single place for validation, logging, rollback, and later network synchronization.

Included examples:

- `RollDiceCommand`
- `BuyFromMarketCommand`
- `EndTurnCommand`

Later phases should add commands for setup placement, structure construction, auditor movement, tax discard, and player-to-player trading.

### Memento

`GameSnapshotCodec` serializes the `Game` aggregate into a `GameMemento`. `CommandManager` stores snapshots before successful commands and supports undo/redo. The same aggregate is also used by asynchronous save/load.

### Factory

`BoardFactory` isolates map generation. `RandomBoardFactory` currently creates a randomized but logically balanced map and keeps the model independent of the default 5×5 size.

### Strategy candidate for Phase 2

When the AI bonus feature begins, add an `AiStrategy` interface and keep AI decisions outside `GameEngine`.

## Concurrency boundary

`GameController` uses a dedicated single-thread executor for game commands and delegates save/load to an asynchronous persistence service. UI callbacks are passed to `Platform.runLater(...)`. No domain class imports JavaFX.

## Open interpretation points

The written rules contain a few points that should be confirmed before the final implementation:

1. Whether Hacker CEO should always pay exactly 3 Capital, or receive a one-unit discount from the visible dynamic price. The scaffold uses a one-unit discount so dynamic pricing remains meaningful.
2. Whether market inactivity means three complete table rounds or three individual player turns. The scaffold uses full table rounds.
3. The initial position of the auditor before the first roll of 7. The scaffold treats it as absent until first placement.
4. Whether the longest-partnership bonus counts the longest simple path or another network metric. Phase 2 should implement the longest simple connected chain and preserve the first-to-reach tie rule.
