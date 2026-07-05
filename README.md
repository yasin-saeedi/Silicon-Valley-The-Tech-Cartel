# Silicon Valley: The Tech Cartel

Advanced Programming final project at Ferdowsi University of Mashhad.

## Requirements

- JDK 17 or newer (Required for compiling and executing the JavaFX application).
- Maven (Project build tool and dependency management system).

## Run

`mvn clean javafx:run` (Compiles source code, resolves dependencies, and launches the interactive JavaFX GUI).
To run integration and verification tests: `mvn test` (Executes automated model and smoke test suites).

## Project Structure

- `model`: game entities and rules
- `controller`: connection between game logic and JavaFX
- `view`: JavaFX user interface
- `pattern`: Command and Memento patterns
- `persistence`: save and load services
- `exception`: custom exceptions
- `docs/uml`: UML diagrams

### Core Files & Modules

- `App.java`: JavaFX application entry point that initializes the primary stage and root scene.
- `Launcher.java`: Workaround launcher class required for executing JavaFX from JARs or IDEs.
- `controller/GameController.java`: Manages thread separation between JavaFX UI and asynchronous background game logic.
- `model/game/Game.java`: Root aggregate class holding board state, players, market, and turn progression.
- `model/game/StandardGameEngine.java`: Core gameplay engine enforcing all placement rules, trading, and victory detection.
- `model/board/Board.java`: Manages the graph network of sectors, vertices, edges, and company structures.
- `model/player/Player.java`: Maintains player resources, role abilities, structure inventories, and victory points.
- `model/market/Market.java`: Controls dynamic market pricing and round-based inactivity price decay.
- `model/resource/ResourceBundle.java`: Immutable data structure representing quantities of game resources and costs.
- `model/structure/CompanyStructure.java`: Abstract base class for all board structures (MVPs, Partnerships, and Unicorns).
- `view/MainView.java`: Main interactive JavaFX dashboard, sidebars, dialogs, and trading interfaces.
- `view/board/BoardView.java`: Renders the visual hexagonal board, vertices, edges, and player tokens.
- `pattern/command/CommandManager.java`: Coordinates command execution, memento snapshots, and undo/redo history.
- `pattern/command/TradeWithPlayerCommand.java`: Encapsulates player-to-player resource trading actions and validation.
- `pattern/memento/GameSnapshotCodec.java`: Serializes and deserializes game state snapshots for memento recovery.
- `persistence/JavaSerializationGameSaveService.java`: Handles asynchronous file saving and loading of game states.
- `util/GameConstants.java`: Central repository for game configuration, resource costs, and victory point rules.
- `ModelSmokeTest.java`: Comprehensive integration verification script testing core gameplay and trade rules.

## Current Status

The project includes complete implementation of all core and advanced gameplay features: spatial placement and building workflows (MVPs, Partnerships, Unicorns), dynamic market, regulatory crises, longest network award, player-to-player trading, centralized victory detection, Command/Memento architecture with Undo/Redo, asynchronous save/load services, and an interactive JavaFX user interface.
