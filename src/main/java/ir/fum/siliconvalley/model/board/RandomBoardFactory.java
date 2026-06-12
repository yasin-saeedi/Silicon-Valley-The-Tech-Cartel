package ir.fum.siliconvalley.model.board;

import ir.fum.siliconvalley.model.enums.SectorType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Random;

/** Creates a randomized map while keeping resource classes reasonably balanced. */
public final class RandomBoardFactory implements BoardFactory {
    private static final List<Integer> WEIGHTED_ACTIVATION_NUMBERS = List.of(
            2, 3, 3, 4, 4, 5, 5, 6, 6,
            8, 8, 9, 9, 10, 10, 11, 11, 12
    );

    private static final List<SectorType> PRODUCTIVE_TYPES = List.of(
            SectorType.AI_HUB,
            SectorType.FINTECH_DISTRICT,
            SectorType.CLOUD_CAMPUS,
            SectorType.IP_QUARTER,
            SectorType.DATA_VALLEY
    );

    @Override
    public Board create(int boardSize, Random random) {
        Objects.requireNonNull(random, "random");
        if (boardSize < 2) {
            throw new IllegalArgumentException("Board size must be at least 2");
        }

        int cellCount = boardSize * boardSize;
        int regulatoryCount = Math.max(1, Math.round(cellCount * 0.20f));
        int productiveCount = cellCount - regulatoryCount;

        List<SectorType> types = createBalancedTypes(productiveCount, regulatoryCount);
        List<Integer> activationNumbers = createActivationNumbers(productiveCount, random);
        Collections.shuffle(types, random);

        List<Sector> sectors = new ArrayList<>(cellCount);
        int productiveIndex = 0;
        for (int index = 0; index < cellCount; index++) {
            int row = index / boardSize;
            int column = index % boardSize;
            SectorType type = types.get(index);
            Integer activationNumber = null;
            if (type.producesResource()) {
                activationNumber = activationNumbers.get(productiveIndex++);
            }
            sectors.add(new Sector(new SectorPosition(row, column), type, activationNumber));
        }
        return new Board(boardSize, sectors);
    }

    private List<SectorType> createBalancedTypes(int productiveCount, int regulatoryCount) {
        List<SectorType> types = new ArrayList<>(productiveCount + regulatoryCount);
        for (int index = 0; index < productiveCount; index++) {
            types.add(PRODUCTIVE_TYPES.get(index % PRODUCTIVE_TYPES.size()));
        }
        for (int index = 0; index < regulatoryCount; index++) {
            types.add(SectorType.REGULATORY_ZONE);
        }
        return types;
    }

    private List<Integer> createActivationNumbers(int count, Random random) {
        List<Integer> numbers = new ArrayList<>(count);
        int cursor = 0;
        while (numbers.size() < count) {
            numbers.add(WEIGHTED_ACTIVATION_NUMBERS.get(cursor % WEIGHTED_ACTIVATION_NUMBERS.size()));
            cursor++;
        }
        Collections.shuffle(numbers, random);
        return numbers;
    }
}
