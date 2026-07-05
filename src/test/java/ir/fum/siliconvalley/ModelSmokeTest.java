package ir.fum.siliconvalley;

import ir.fum.siliconvalley.exception.InvalidPlacementException;
import ir.fum.siliconvalley.model.board.*;
import ir.fum.siliconvalley.model.enums.*;
import ir.fum.siliconvalley.model.game.*;
import ir.fum.siliconvalley.model.market.Market;
import ir.fum.siliconvalley.model.player.Player;
import ir.fum.siliconvalley.model.resource.ResourceBundle;
import ir.fum.siliconvalley.model.structure.CompanyStructure;
import ir.fum.siliconvalley.model.structure.MVP;
import ir.fum.siliconvalley.model.structure.VertexLocation;
import ir.fum.siliconvalley.pattern.memento.GameMemento;
import ir.fum.siliconvalley.pattern.memento.GameSnapshotCodec;

import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

public final class ModelSmokeTest {
    private ModelSmokeTest() {
    }

    public static void main(String[] args) throws Exception {
        System.out.println("Starting comprehensive Model Integration Test...");

        Board board = new RandomBoardFactory().create(5, new Random(42));
        require(board.getSectors().size() == 25, "Expected 25 sectors");
        require(board.getVertices().size() == 36, "Expected 36 vertices");
        require(board.getEdges().size() == 60, "Expected 60 edges");

        Player ada = new Player("Ada", PlayerColor.BLUE);
        Player linus = new Player("Linus", PlayerColor.RED);
        Player grace = new Player("Grace", PlayerColor.GREEN);

        // Assign Roles
        ada.assignFounderRole(FounderRole.VC_FUNDED); // Starts with +2 Capital
        linus.assignFounderRole(FounderRole.TECH_GURU_CTO);
        grace.assignFounderRole(FounderRole.HACKER_CEO);

        require(ada.getResourceCount(ResourceType.CAPITAL) == 2, "VC_FUNDED starting bonus failed");

        Game game = new Game(board, new Market(), List.of(ada, linus, grace));
        
        // Custom deterministic random: first roll is 6, second roll is 7
        Random mockRandom = new Random() {
            private int callCount = 0;
            @Override
            public int nextInt(int bound) {
                callCount++;
                if (callCount == 1 || callCount == 2) {
                    return 2; // 2 + 1 = 3. Two rolls of 3 = 6.
                } else if (callCount == 3 || callCount == 4) {
                    return callCount == 3 ? 2 : 3; // 3 + 4 = 7.
                }
                return 1;
            }
        };

        StandardGameEngine engine = new StandardGameEngine(game, mockRandom);

        // Verify Setup Phase
        require(game.getPhase() == GamePhase.SETUP, "Game should start in SETUP phase");
        require(game.getTurnStage() == TurnStage.SETUP_PLACEMENT, "Stage should be SETUP_PLACEMENT");
        require(game.getCurrentPlayerIndex() == 0, "First player should be active");

        // Vertex & Edge coordinates for initial setups
        VertexPosition v0 = new VertexPosition(0, 0);
        EdgePosition e0 = new EdgePosition(v0, new VertexPosition(0, 1));

        VertexPosition v1 = new VertexPosition(1, 1);
        EdgePosition e1 = new EdgePosition(v1, new VertexPosition(1, 2));

        VertexPosition v2 = new VertexPosition(2, 2);
        EdgePosition e2 = new EdgePosition(v2, new VertexPosition(2, 3));

        VertexPosition v2_second = new VertexPosition(3, 3);
        EdgePosition e2_second = new EdgePosition(v2_second, new VertexPosition(3, 4));

        VertexPosition v1_second = new VertexPosition(4, 4);
        EdgePosition e1_second = new EdgePosition(v1_second, new VertexPosition(4, 5));

        VertexPosition v0_second = new VertexPosition(5, 5);
        EdgePosition e0_second = new EdgePosition(v0_second, new VertexPosition(5, 4));

        // 1. Placement 1: Ada (Index 0)
        engine.placeInitialMvpAndPartnership(ada.getId(), v0, e0);
        require(game.getSetupPlacementCount() == 1, "Setup count should be 1");
        require(game.getCurrentPlayerIndex() == 1, "Linus should be active next");

        // 2. Placement 2: Linus (Index 1)
        engine.placeInitialMvpAndPartnership(linus.getId(), v1, e1);
        require(game.getSetupPlacementCount() == 2, "Setup count should be 2");
        require(game.getCurrentPlayerIndex() == 2, "Grace should be active next");

        // 3. Placement 3: Grace (Index 2)
        engine.placeInitialMvpAndPartnership(grace.getId(), v2, e2);
        require(game.getSetupPlacementCount() == 3, "Setup count should be 3");
        require(game.getCurrentPlayerIndex() == 2, "Grace should be active again (snake order)");

        // 4. Placement 4: Grace (Index 2, second placement)
        engine.placeInitialMvpAndPartnership(grace.getId(), v2_second, e2_second);
        require(game.getSetupPlacementCount() == 4, "Setup count should be 4");
        require(game.getCurrentPlayerIndex() == 1, "Linus should be active next");

        // 5. Placement 5: Linus (Index 1, second placement)
        engine.placeInitialMvpAndPartnership(linus.getId(), v1_second, e1_second);
        require(game.getSetupPlacementCount() == 5, "Setup count should be 5");
        require(game.getCurrentPlayerIndex() == 0, "Ada should be active next");

        // 6. Placement 6: Ada (Index 0, second placement)
        engine.placeInitialMvpAndPartnership(ada.getId(), v0_second, e0_second);

        // Verify phase transition after setup
        require(game.getPhase() == GamePhase.MAIN_TURN, "Phase should be MAIN_TURN");
        require(game.getTurnStage() == TurnStage.ROLL_DICE, "Stage should be ROLL_DICE");
        require(game.getCurrentPlayerIndex() == 0, "Active player should be reset to index 0");

        // Verify initial resources received by Ada (from sectors adjacent to second MVP at (5,5))
        int adaResCount = ada.getTotalResourceCards();
        System.out.println("Ada initial resources count: " + adaResCount);
        require(adaResCount > 2, "Ada should have received resources adjacent to (5,5) plus starting VC bonus");

        // Give Ada resource to test paid building and validations
        ada.addResources(ResourceBundle.builder()
                .add(ResourceType.CAPITAL, 10)
                .add(ResourceType.TALENT, 10)
                .add(ResourceType.CLOUD, 10)
                .add(ResourceType.DATA, 10)
                .add(ResourceType.PATENT, 10)
                .build());

        // We are in MAIN_TURN phase and ROLL_DICE stage. Roll first to go to ACTIONS stage.
        int roll = engine.rollDice();
        System.out.println("Rolled value: " + roll + ", stage: " + game.getTurnStage());
        require(game.getTurnStage() == TurnStage.ACTIONS, "Stage should be ACTIONS after rolling");

        // Verify Distance-of-Two validation
        boolean distOfTwoThrew = false;
        try {
            // Vertex (0,1) is adjacent to Ada's MVP at (0,0) (distance = 1 edge).
            // Trying to place a company there should fail.
            engine.buildMvp(ada.getId(), new VertexPosition(0, 1));
        } catch (InvalidPlacementException e) {
            distOfTwoThrew = true;
            System.out.println("Distance-of-two violation correctly prevented: " + e.getMessage());
        }
        require(distOfTwoThrew, "Distance-of-two validation should have failed");

        // Verify Connected Network validation
        boolean connectionThrew = false;
        try {
            // Edge (5,0) to (5,1) is not connected to Ada's network.
            engine.buildPartnership(ada.getId(), new EdgePosition(new VertexPosition(5, 0), new VertexPosition(5, 1)));
        } catch (InvalidPlacementException e) {
            connectionThrew = true;
            System.out.println("Unconnected partnership correctly prevented: " + e.getMessage());
        }
        require(connectionThrew, "Connectivity validation should have failed");

        // Build Partnership first to extend network to distance 2
        EdgePosition extendEdge = new EdgePosition(new VertexPosition(0, 1), new VertexPosition(0, 2));
        engine.buildPartnership(ada.getId(), extendEdge);

        // Now build MVP at (0,2) which is connected and 2 edges away from (0,0)
        VertexPosition newMvpPos = new VertexPosition(0, 2);
        int oldMvpCount = ada.getStructures().size();
        engine.buildMvp(ada.getId(), newMvpPos);
        require(ada.getStructures().size() == oldMvpCount + 1, "Paid MVP building failed");
        require(board.getVertex(newMvpPos).getCompanyStructureId().isPresent(), "MVP not placed on board");

        // Find the MVP structure ID to test upgrade
        UUID mvpId = board.getVertex(newMvpPos).getCompanyStructureId().orElseThrow();

        // Upgrade MVP to Unicorn for Linus? No, Linus is the CTO but he has to upgrade his own structure.
        // Let's upgrade Ada's MVP. (Ada is VC_FUNDED, so she pays standard cost: 2 Cloud, 3 Data)
        int oldCloud = ada.getResourceCount(ResourceType.CLOUD);
        int oldData = ada.getResourceCount(ResourceType.DATA);
        engine.upgradeMvpToUnicorn(ada.getId(), mvpId);
        require(ada.getResourceCount(ResourceType.CLOUD) == oldCloud - 2, "Upgrade did not deduct correct Cloud");
        require(ada.getResourceCount(ResourceType.DATA) == oldData - 3, "Upgrade did not deduct correct Data");
        
        UUID newUnicornId = board.getVertex(newMvpPos).getCompanyStructureId().orElseThrow();
        require(game.findStructure(newUnicornId).orElseThrow().getVictoryPoints() == 2, "Unicorn should have 2 VPs");

        // Recalculate Longest Network test
        // Let's build a chain of partnerships for Ada.
        // Already has Partnership at (5,5)-(5,4) (which is 1).
        // Let's build (5,4)-(5,3) and (5,3)-(5,2). That's a chain of 3.
        EdgePosition edge2 = new EdgePosition(new VertexPosition(5, 4), new VertexPosition(5, 3));
        EdgePosition edge3 = new EdgePosition(new VertexPosition(5, 3), new VertexPosition(5, 2));
        
        engine.buildPartnership(ada.getId(), edge2);
        engine.buildPartnership(ada.getId(), edge3);
        
        require(game.getLongestNetworkOwnerId().isPresent(), "Longest network award should be assigned");
        require(game.getLongestNetworkOwnerId().orElseThrow().equals(ada.getId()), "Ada should own longest network");
        require(game.getLongestNetworkLength() == 3, "Ada longest network length should be 3");

        // Verify Regulatory Crisis and Tax Discard
        // Give Linus 10 resources (limit is 7).
        linus.addResources(ResourceBundle.builder()
                .add(ResourceType.CAPITAL, 4)
                .add(ResourceType.TALENT, 3)
                .add(ResourceType.CLOUD, 3)
                .build());
        
        int linusTotal = linus.getTotalResourceCards();
        require(linusTotal >= 10, "Linus cards should be at least 10");

        // Advance turn to Linus
        engine.finishTurn();
        require(game.getCurrentPlayerIndex() == 1, "Now Linus's turn");
        
        // Roll the dice (which mockRandom guarantees will roll exactly 7)
        int roll7 = engine.rollDice();
        require(roll7 == 7, "Roll should be exactly 7");
        
        // Verify tax discard for Linus
        int requiredDiscard = linusTotal / 2;
        ResourceBundle.Builder discardBuilder = ResourceBundle.builder();
        int discardedCount = 0;
        for (ResourceType type : ResourceType.values()) {
            int owned = linus.getResourceCount(type);
            int toDiscard = Math.min(owned, requiredDiscard - discardedCount);
            if (toDiscard > 0) {
                discardBuilder.add(type, toDiscard);
                discardedCount += toDiscard;
            }
        }
        ResourceBundle discard = discardBuilder.build();
        engine.discardTaxes(linus.getId(), discard);
        require(linus.getTotalResourceCards() == linusTotal - requiredDiscard, "Linus should have discarded correctly");

        // Verify tax discard for Ada (since she has way more than 9 cards)
        int adaTotal = ada.getTotalResourceCards();
        if (adaTotal > ada.getTaxHandLimit()) {
            int adaRequiredDiscard = adaTotal / 2;
            ResourceBundle.Builder adaDiscardBuilder = ResourceBundle.builder();
            int adaDiscardedCount = 0;
            for (ResourceType type : ResourceType.values()) {
                int owned = ada.getResourceCount(type);
                int toDiscard = Math.min(owned, adaRequiredDiscard - adaDiscardedCount);
                if (toDiscard > 0) {
                    adaDiscardBuilder.add(type, toDiscard);
                    adaDiscardedCount += toDiscard;
                }
            }
            engine.discardTaxes(ada.getId(), adaDiscardBuilder.build());
            require(ada.getTotalResourceCards() == adaTotal - adaRequiredDiscard, "Ada should have discarded correctly");
        }

        for (Player p : game.getPlayers()) {
            System.out.println("Player " + p.getName() + " total cards: " + p.getTotalResourceCards() + ", limit: " + p.getTaxHandLimit());
        }
        require(game.getTurnStage() == TurnStage.AUDITOR_MOVE, "Should transition to AUDITOR_MOVE stage");

        // Auditor Placement Adjacency Restriction
        boolean auditorRestrictedThrew = false;
        try {
            // Sector (0,4) has no adjacent company structures.
            // Move auditor there should fail.
            engine.moveAuditor(linus.getId(), new SectorPosition(0, 4));
        } catch (InvalidPlacementException e) {
            auditorRestrictedThrew = true;
            System.out.println("Auditor placement restriction correctly prevented: " + e.getMessage());
        }
        require(auditorRestrictedThrew, "Auditor placement should have failed");

        // Move auditor to (0,0) (adjacent to Ada's MVP). This should succeed.
        engine.moveAuditor(linus.getId(), new SectorPosition(0, 0));
        require(board.getSector(new SectorPosition(0, 0)).isAudited(), "Sector (0,0) should be audited");

        // Test Memento captures and restores
        GameSnapshotCodec codec = new GameSnapshotCodec();
        GameMemento memento = codec.capture(game);
        Game restored = codec.restore(memento);
        require(restored.getBoard().getSector(new SectorPosition(0, 0)).isAudited(), "Memento recovery failed");

        // Test Player-to-Player Trading (Linus trading with Ada)
        int linusCapBefore = linus.getResourceCount(ResourceType.CAPITAL);
        int linusTalBefore = linus.getResourceCount(ResourceType.TALENT);
        int adaCapBefore = ada.getResourceCount(ResourceType.CAPITAL);
        int adaTalBefore = ada.getResourceCount(ResourceType.TALENT);

        if (linusCapBefore == 0) {
            linus.addResources(ResourceBundle.single(ResourceType.CAPITAL, 2));
            linusCapBefore += 2;
        }
        if (adaTalBefore == 0) {
            ada.addResources(ResourceBundle.single(ResourceType.TALENT, 2));
            adaTalBefore += 2;
        }

        ResourceBundle linusOffer = ResourceBundle.single(ResourceType.CAPITAL, 1);
        ResourceBundle adaRequest = ResourceBundle.single(ResourceType.TALENT, 1);
        engine.tradeWithPlayer(linus.getId(), ada.getId(), linusOffer, adaRequest);

        require(linus.getResourceCount(ResourceType.CAPITAL) == linusCapBefore - 1, "Linus should lose 1 Capital");
        require(linus.getResourceCount(ResourceType.TALENT) == linusTalBefore + 1, "Linus should gain 1 Talent");
        require(ada.getResourceCount(ResourceType.CAPITAL) == adaCapBefore + 1, "Ada should gain 1 Capital");
        require(ada.getResourceCount(ResourceType.TALENT) == adaTalBefore - 1, "Ada should lose 1 Talent");
        System.out.println("Player-to-player trade verified successfully!");

        // Test Victory Condition Detection in Engine
        for (int i = 0; i < 9; i++) {
            linus.addStructure(new MVP(linus.getId(), new VertexLocation(new VertexPosition(0, 5)), 100 + i));
        }
        require(linus.calculateBaseVictoryPoints() >= 10, "Linus should have >= 10 VPs");

        engine.finishTurn();
        require(game.getPhase() == GamePhase.FINISHED, "Game should transition to FINISHED phase upon reaching 10 VPs");
        require(game.getTurnStage() == TurnStage.FINISHED, "Stage should transition to FINISHED upon victory");
        System.out.println("Engine victory detection verified successfully!");

        System.out.println("Model smoke test passed successfully!");
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
