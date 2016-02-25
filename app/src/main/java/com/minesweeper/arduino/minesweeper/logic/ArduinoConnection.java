package com.minesweeper.arduino.minesweeper.logic;

import com.minesweeper.arduino.minesweeper.entity.Block;

/**
 * Created by Mosquera on 06.12.2015.
 */
public  class ArduinoConnection {


    public static void connect(String ip) {
        //TODO IMPORTANT connect to arduino

    }

    public static void sendCommand(int row ,int col,int LEDCOLOR) {
        //TODO IMPORTANT send command to arduino
        //TODO IMPORTANT convert to a cmd so the arduino can understand
    }

    /**
     * convert the x, y to one jsonObject
     * @param row
     * @param col
     */
    private static void convertPositionToJSONObject(int row, int col, int LEDCOLOR) {

    }


}
        