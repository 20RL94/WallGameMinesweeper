package com.minesweeper.arduino.minesweeper.logic;

import com.minesweeper.arduino.minesweeper.entity.Block;

import org.json.JSONException;
import org.json.JSONObject;

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
        try {
            JSONObject messageToArduino = convertPositionToJSONObject(row, col, LEDCOLOR);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * convert the x, y to one jsonObject
     * @param row x-pos
     * @param col y-pos
     * @param LEDCOLOR color of LED
     */
    private static   JSONObject convertPositionToJSONObject(int row, int col, int LEDCOLOR) throws JSONException
    {
        // arduino needs the 3 color (r,g,b) to change the color of a LED
        JSONObject obj = new JSONObject();
        if (LEDCOLOR==1)//red
        {
            obj.put("row", row);
            obj.put("col", col);
            obj.put("red", 255);
            obj.put("green", 0);
            obj.put("blue", 0);
        }

        if (LEDCOLOR == 2)//blue
        {
            obj.put("row", row);
            obj.put("col", col);
            obj.put("red", 0);
            obj.put("green", 0);
            obj.put("blue", 255);
        }
        if (LEDCOLOR == 3)//green
        {
            obj.put("row", row);
            obj.put("col", col);
            obj.put("red", 0);
            obj.put("green", 255);
            obj.put("blue", 0);
        }
        else //turn off LED
        {
            obj.put("row", row);
            obj.put("col", col);
            obj.put("red", 0);
            obj.put("green", 0);
            obj.put("blue", 0);
        }
        return obj;

    }


}
        