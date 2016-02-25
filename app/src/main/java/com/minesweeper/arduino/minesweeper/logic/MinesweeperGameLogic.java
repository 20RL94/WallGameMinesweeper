package com.minesweeper.arduino.minesweeper.logic;

import android.view.View;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.minesweeper.arduino.minesweeper.R;
import com.minesweeper.arduino.minesweeper.entity.Block;
import com.minesweeper.arduino.minesweeper.gui.MinesweeperGame;

/**
 * Created by Mosquera on 10.12.2015.
 */
public class MinesweeperGameLogic {

    private static String setTextDefaultValue = "000";

    /**
     * Method to start the game
     * Generate the field --> set the Mines
     * Show the field on the device
     * The Default total value of mine is 10 , because the LED-WALL is small.
     * @param minesweeperGame reference to the class object
     */
    public static void startNewGame(MinesweeperGame minesweeperGame)
    {
        // plant mines and do rest of the calculations
        createMineField
                (minesweeperGame, minesweeperGame.isGameOver, minesweeperGame.numberOfColumnsInMineField, minesweeperGame.numberOfRowsInMineField);
        // display all blocks in UI
        showMineField
                (minesweeperGame, minesweeperGame.blockDimension, minesweeperGame.blockPadding, minesweeperGame.blocks, minesweeperGame.height, minesweeperGame.mineField, minesweeperGame.numberOfColumnsInMineField, minesweeperGame.numberOfRowsInMineField, minesweeperGame.width);

        minesweeperGame.minesToFind = minesweeperGame.totalNumberOfMines;
        minesweeperGame.isGameOver = false;
        minesweeperGame.secondsPassed = 0;
    }
    /**
     * Method to show the field on the device
     * @param minesweeperGame the reference to class object
     * @param blockDimension block dimension
     * @param blockPadding block pad
     * @param blocks the reference to class object
     * @param height block height
     * @param mineField reference to the table layout
     * @param numberOfColumnsInMineField amount of cols in table layout
     * @param numberOfRowsInMineField amount of rows in table layout
     * @param width block width
     */
    public static void showMineField(MinesweeperGame minesweeperGame, int blockDimension, int blockPadding, Block[][] blocks, int height, TableLayout mineField, int numberOfColumnsInMineField, int numberOfRowsInMineField, int width)
    {
        // 0th and last Row and Columns
        // are used for calculation purposes only
        for (int row = 1; row < numberOfRowsInMineField + 1; row++)
        {
            TableRow tableRow = new TableRow(minesweeperGame);
            tableRow.setLayoutParams(new TableRow.LayoutParams((blockDimension + 2 * blockPadding) * numberOfColumnsInMineField, blockDimension + 2 * blockPadding));

            for (int column = 1; column < numberOfColumnsInMineField + 1; column++)
            {
                blocks[row][column].setLayoutParams(new TableRow.LayoutParams(
                        blockDimension + width * blockPadding,
                        blockDimension + height * blockPadding));
                blocks[row][column].setPadding(blockPadding, blockPadding, blockPadding, blockPadding);
                tableRow.addView(blocks[row][column]);
            }
            mineField.addView(tableRow, new TableLayout.LayoutParams(
                    (blockDimension + 2 * blockPadding) * numberOfColumnsInMineField, blockDimension + 2 * blockPadding));
        }
    }

    /**
     * Method to  initialize the Minefield
     * set the listener in each block
     * set the mines on each block on first click
     * @param minesweeperGame reference to class object
     * @param isGameOver boolean to know if the game is over
     * @param numberOfColumnsInMineField´amount of cols in table layout
     * @param numberOfRowsInMineField amount of rows in table layout
     */
    public static void createMineField(final MinesweeperGame minesweeperGame, final boolean isGameOver, int numberOfColumnsInMineField, int numberOfRowsInMineField)
    {

        //Initialize the field
        minesweeperGame.blocks = new Block[numberOfRowsInMineField + 2][numberOfColumnsInMineField + 2];

        for (int row = 0; row < numberOfRowsInMineField + 2; row++)
        {
            for (int column = 0; column < numberOfColumnsInMineField + 2; column++)
            {
                minesweeperGame.blocks[row][column] = new Block(minesweeperGame);
                //Set the default value . Default values are in Block Class
                minesweeperGame.blocks[row][column].setDefaults();

                //Set the listener on the current Block and
                // set the random mines
                final int currentRow = row;
                final int currentColumn = column;

                // add Click Listener
                // this is treated as Left Mouse click
                minesweeperGame.blocks[row][column].setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        // start timer on first click
                        if (!minesweeperGame.isTimerStarted)
                        {
                            minesweeperGame.startTimer();
                            minesweeperGame.isTimerStarted = true;
                        }

                        // set mines on first click
                        if (!minesweeperGame.areMinesSet)
                        {
                            minesweeperGame.areMinesSet = true;
                            minesweeperGame.setMines(currentRow, currentColumn);
                        }

                        // open nearby blocks till we get numbered blocks
                        minesweeperGame.rippleUncover(currentRow, currentColumn);

                        // did we clicked a mine
                        if (minesweeperGame.blocks[currentRow][currentColumn].hasMine())
                        {
                            // Oops, game over
                            //TODO send a signal to put all LED to RED
                            finishGame(minesweeperGame, minesweeperGame.blocks, minesweeperGame.btnSmile, minesweeperGame.numberOfColumnsInMineField, minesweeperGame.numberOfRowsInMineField, minesweeperGame.secondsPassed, currentRow, currentColumn);
                        }

                        // check if we win the game
                        if (checkGameWin(minesweeperGame.blocks, minesweeperGame.numberOfColumnsInMineField, minesweeperGame.numberOfRowsInMineField))
                        {
                            // mark game as win
                            winGame(minesweeperGame, minesweeperGame.blocks, minesweeperGame.btnSmile, minesweeperGame.numberOfColumnsInMineField, minesweeperGame.numberOfRowsInMineField, minesweeperGame.secondsPassed);
                        }

                    }
                });
            }
        }
    }

    /**
     * Method to end game and reset the values.
     * @param minesweeperGame reference to the class object
     * @param btnSmile reference to button
     * @param mineField reference to the table layout
     * @param txtMineCount reference to the textview
     * @param txtTimer reference to the textview
     */
    public static void endExistingGame(MinesweeperGame minesweeperGame, ImageButton btnSmile, TableLayout mineField, TextView txtMineCount, TextView txtTimer)
    {
        minesweeperGame.stopTimer(); // stop if timer is running
        txtTimer.setText(setTextDefaultValue); // revert all text
        txtMineCount.setText(setTextDefaultValue); // revert mines count
        btnSmile.setBackgroundResource(R.drawable.smile);

        // remove all rows from mineField TableLayout
        mineField.removeAllViews();

        // set all variables to support end of game
        minesweeperGame.isTimerStarted=false;
        minesweeperGame.areMinesSet=false;
        minesweeperGame.isGameOver=false;
        minesweeperGame.minesToFind=0;


    }

    /**
     * Method to check if the game is over -> WIN
     * @param blocks reference to class object
     * @param numberOfColumnsInMineField amount of cols in table layout
     * @param numberOfRowsInMineField amount of rows in table layout
     * @return true win, false not over yet
     */
    public static boolean checkGameWin(Block[][] blocks, int numberOfColumnsInMineField, int numberOfRowsInMineField)
    {
        for (int row = 1; row < numberOfRowsInMineField + 1; row++)
        {
            for (int column = 1; column < numberOfColumnsInMineField + 1; column++)
            {
                if (!blocks[row][column].hasMine() && blocks[row][column].isCovered())
                {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Method to end the current game and reset certain variables to default value
     * Show all mines and disable touch function
     * @param minesweeperGame reference to the class object
     * @param blocks reference to the class object
     * @param btnSmile reference to ImageButton
     * @param numberOfColumnsInMineField amount of cols in tablelayout
     * @param numberOfRowsInMineField amount of rows in tablelayout
     * @param secondsPassed time used
     * @param currentRow current x-pos
     * @param currentColumn current y-pos
     */
    public static void finishGame(MinesweeperGame minesweeperGame, Block[][] blocks, ImageButton btnSmile, int numberOfColumnsInMineField, int numberOfRowsInMineField, int secondsPassed, int currentRow, int currentColumn)
    {
        minesweeperGame.isGameOver = true; // mark game as over
        minesweeperGame.stopTimer(); // stop timer
        minesweeperGame.isTimerStarted = false;
        btnSmile.setBackgroundResource(R.drawable.sad);

        // show all mines
        // disable all blocks
        //so the user can't interact after game over
        for (int row = 1; row < numberOfRowsInMineField + 1; row++)
        {
            for (int column = 1; column < numberOfColumnsInMineField + 1; column++)
            {
                // disable block
                blocks[row][column].setBlockAsDisabled(false);
                //TODO send signal TURN OFF led
                ArduinoConnection.sendCommand(row, column, 0);

                // block has mine
                if (blocks[row][column].hasMine() )
                {
                    // set mine icon
                    blocks[row][column].setMineIcon(false);
                    //TODO send signal RED Led
                    ArduinoConnection.sendCommand(row, column, MinesweeperGame.RED);
                }

            }
        }

        // trigger mine
        blocks[currentRow][currentColumn].triggerMine();

        // show message
        minesweeperGame.showDialog("You tried for " + Integer.toString(secondsPassed) + " seconds!", 1000, false, false);
    }

    /**
     * Method the same like finish game.
     * Just with
     * @param minesweeperGame reference to the class object
     * @param blocks reference to the class object
     * @param btnSmile reference to the imagebutton
     * @param numberOfColumnsInMineField amount of cols in tablelayout
     * @param numberOfRowsInMineField amount of rows in tablelayout
     * @param secondsPassed time used
     */
    public static void winGame(MinesweeperGame minesweeperGame, Block[][] blocks, ImageButton btnSmile, int numberOfColumnsInMineField, int numberOfRowsInMineField, int secondsPassed)
    {
        minesweeperGame.stopTimer();
        minesweeperGame.isTimerStarted = false;
        minesweeperGame.isGameOver = true;
        minesweeperGame.minesToFind = 0; //set mine count to 0

        //set icon to skull dude
        btnSmile.setBackgroundResource(R.drawable.cool);

        minesweeperGame.updateMineCountDisplay(); // update mine count

        // disable all buttons
        for (int row = 1; row < numberOfRowsInMineField + 1; row++)
        {
            for (int column = 1; column < numberOfColumnsInMineField + 1; column++)
            {
                blocks[row][column].setClickable(false);
                if (blocks[row][column].hasMine())
                {
                    blocks[row][column].setBlockAsDisabled(false);
                }
            }
        }
        // show message
        minesweeperGame.showDialog("You won in " + Integer.toString(secondsPassed) + " seconds!", 1000, false, true);
    }

}
