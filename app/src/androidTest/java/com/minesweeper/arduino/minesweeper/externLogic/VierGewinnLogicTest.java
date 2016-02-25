package com.minesweeper.arduino.minesweeper.externLogic;

import junit.framework.TestCase;

/**
 * Created by Mosquera on 21.01.2016.
 */
public class VierGewinnLogicTest extends TestCase {

    public void testgetPlayer() {
        VierGewinnLogic logic = new VierGewinnLogic();
        logic.initializeField();
        // player 1 set a token
        logic.setPlayerToken(1, 1);
        //it should be player 2 now -> if PLAYER_1 is set with value 1
        assertEquals(1,logic.PLAYER_1);

    }

}