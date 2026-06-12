package ir.fum.siliconvalley.model.board;

import java.util.Random;

public interface BoardFactory {
    Board create(int boardSize, Random random);
}
