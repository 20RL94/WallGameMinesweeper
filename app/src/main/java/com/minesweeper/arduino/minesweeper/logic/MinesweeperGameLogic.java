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


    /**
     * Method to start the game
     * Generate the field --> set the Mines
     * Show the field on the device
     * The Default total value of mine is 10 , because the LED-WALL is small.
     * @param minesweeperGame
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
     * @param minesweeperGame
     * @param blockDimension
     * @param blockPadding
     * @param blocks
     * @param height
     * @param mineField
     * @param numberOfColumnsInMineField
     * @param numberOfRowsInMineField
     * @param width
     */
    public static void showMineField(MinesweeperGame minesweeperGame, int blockDimension, int blockPadding, Block[][] blocks, int height, TableLayout mineField, int numberOfColumnsInMineField, int numberOfRowsInMineField, int width)
    {
        //TODO send a signal to arduino to set all LED to default color

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
     * @param minesweeperGame
     * @param isGameOver
     * @param numberOfColumnsInMineField
     * @param numberOfRowsInMineField
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


                        if (!minesweeperGame.blocks[currentRow][currentColumn].isFlagged())
                        {
                            // open nearby blocks till we get numbered blocks
                            minesweeperGame.rippleUncover(currentRow, currentColumn);

                            // did we clicked a mine
                            if (minesweeperGame.blocks[currentRow][currentColumn].hasMine())
                            {
                                // Oops, game over
                                finishGame(minesweeperGame, minesweeperGame.blocks, minesweeperGame.btnSmile, minesweeperGame.numberOfColumnsInMineField, minesweeperGame.numberOfRowsInMineField, minesweeperGame.secondsPassed, currentRow, currentColumn);
                            }

                            // check if we win the game
                            if (checkGameWin(minesweeperGame.blocks, minesweeperGame.numberOfColumnsInMineField, minesweeperGame.numberOfRowsInMineField))
                            {
                                // mark game as win
                                winGame(minesweeperGame, minesweeperGame.blocks, minesweeperGame.btnSmile, minesweeperGame.numberOfColumnsInMineField, minesweeperGame.numberOfRowsInMineField, minesweeperGame.secondsPassed);
                            }
                        }
                    }
                });

                // add Long Click listener
                // this is treated as right mouse click listener
                minesweeperGame.blocks[row][column].setOnLongClickListener(new View.OnLongClickListener() {
                    public boolean onLongClick(View view) {

                        // open all surrounding blocks
                        if (!minesweeperGame.blocks[currentRow][currentColumn].isCovered() && (minesweeperGame.blocks[currentRow][currentColumn].getNumberOfMinesInSorrounding() > 0) && !isGameOver) {
                            int nearbyFlaggedBlocks = 0;
                            for (int previousRow = -1; previousRow < 2; previousRow++) {
                                for (int previousColumn = -1; previousColumn < 2; previousColumn++) {
                                    if (minesweeperGame.blocks[currentRow + previousRow][currentColumn + previousColumn].isFlagged()) {
                                        nearbyFlaggedBlocks++;
                                    }
                                }
                            }

                            // if flagged block count is equal to nearby mine count
                            // then open nearby blocks
                            if (nearbyFlaggedBlocks == minesweeperGame.blocks[currentRow][currentColumn].getNumberOfMinesInSorrounding()) {
                                for (int previousRow = -1; previousRow < 2; previousRow++) {
                                    for (int previousColumn = -1; previousColumn < 2; previousColumn++) {
                                        // don't open flagged blocks
                                        if (!minesweeperGame.blocks[currentRow + previousRow][currentColumn + previousColumn].isFlagged()) {
                                            // open blocks till we get numbered block
                                            minesweeperGame.rippleUncover(currentRow + previousRow, currentColumn + previousColumn);

                                            // did we clicked a mine
                                            if (minesweeperGame.blocks[currentRow + previousRow][currentColumn + previousColumn].hasMine()) {
                                                // oops game over
                                                finishGame
                                                        (minesweeperGame, minesweeperGame.blocks, minesweeperGame.btnSmile, minesweeperGame.numberOfColumnsInMineField, minesweeperGame.numberOfRowsInMineField, minesweeperGame.secondsPassed, currentRow + previousRow, currentColumn + previousColumn);
                                            }

                                            // did we win the game
                                            if (checkGameWin
                                                    (minesweeperGame.blocks, minesweeperGame.numberOfColumnsInMineField, minesweeperGame.numberOfRowsInMineField)) {
                                                // mark game as win
                                                winGame(minesweeperGame, minesweeperGame.blocks, minesweeperGame.btnSmile, minesweeperGame.numberOfColumnsInMineField, minesweeperGame.numberOfRowsInMineField, minesweeperGame.secondsPassed);
                                            }
                                        }
                                    }
                                }
                            }


                            return true;
                        }

                        // if clicked block is enabled, clickable or flagged
                        if (minesweeperGame.blocks[currentRow][currentColumn].isClickable() &&
                                (minesweeperGame.blocks[currentRow][currentColumn].isEnabled() || minesweeperGame.blocks[currentRow][currentColumn].isFlagged())) {

                            //MARKING FUNCTION: it will mark a  selected block will not open it.
                            // for long clicks set:
                            // 1. empty blocks to flagged
                            // 2. flagged to question mark
                            // 3. question mark to blank

                            // case 1. set blank block to flagged
                            if (!minesweeperGame.blocks[currentRow][currentColumn].isFlagged() && !minesweeperGame.blocks[currentRow][currentColumn].isQuestionMarked()) {
                                minesweeperGame.blocks[currentRow][currentColumn].setBlockAsDisabled(false);
                                minesweeperGame.blocks[currentRow][currentColumn].setFlagIcon(true);
                                minesweeperGame.blocks[currentRow][currentColumn].setFlagged(true);
                                minesweeperGame.minesToFind--; //reduce mine count
                                minesweeperGame.updateMineCountDisplay();
                                //TODO send a signal to put the selected LED in BLUE
                            }
                            // case 2. set flagged to question mark
                            else if (!minesweeperGame.blocks[currentRow][currentColumn].isQuestionMarked()) {
                                minesweeperGame.blocks[currentRow][currentColumn].setBlockAsDisabled(true);
                                minesweeperGame.blocks[currentRow][currentColumn].setQuestionMarkIcon(true);
                                minesweeperGame.blocks[currentRow][currentColumn].setFlagged(false);
                                minesweeperGame.blocks[currentRow][currentColumn].setQuestionMarked(true);
                                minesweeperGame.minesToFind++; // increase mine count
                                minesweeperGame.updateMineCountDisplay();
                                //TODO send a signal to put the selected LED in YELLOW
                            }
                            // case 3. change to blank square
                            else {
                                minesweeperGame.blocks[currentRow][currentColumn].setBlockAsDisabled(true);
                                minesweeperGame.blocks[currentRow][currentColumn].clearAllIcons();
                                minesweeperGame.blocks[currentRow][currentColumn].setQuestionMarked(false);
                                // if it is flagged then increment mine count
                                if (minesweeperGame.blocks[currentRow][currentColumn].isFlagged()) {
                                    minesweeperGame.minesToFind++; // increase mine count
                                    minesweeperGame.updateMineCountDisplay();
                                }
                                // remove flagged status
                                minesweeperGame.blocks[currentRow][currentColumn].setFlagged(false);
                                //TODO send a signal to put selected LED back to GREEN
                            }

                            minesweeperGame.updateMineCountDisplay(); // update mine display
                        }

                        return true;
                    }
                });
            }
        }
    }



    /**
     * Method to end game and reset the values.
     * @param minesweeperGame
     * @param btnSmile
     * @param mineField
     * @param txtMineCount
     * @param txtTimer
     */
    public static void endExistingGame(MinesweeperGame minesweeperGame, ImageButton btnSmile, TableLayout mineField, TextView txtMineCount, TextView txtTimer)
    {
        minesweeperGame.stopTimer(); // stop if timer is running
        txtTimer.setText("000"); // revert all text
        txtMineCount.setText("000"); // revert mines count
        btnSmile.setBackgroundResource(R.drawable.smile);

        // remove all rows from mineField TableLayout
        mineField.removeAllViews();

        // set all variables to support end of game
        minesweeperGame.isTimerStarted=false;
        minesweeperGame.areMinesSet=false;
        minesweeperGame.isGameOver=false;
        minesweeperGame.minesToFind=0;
        //TODO send a signal to arduino to reset the LED-WALL. set all LED to RED

    }

    /**
     * Method to check if the game is over -> WIN
     * @param blocks
     * @param numberOfColumnsInMineField
     * @param numberOfRowsInMineField
     * @return
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
     * @param minesweeperGame
     * @param blocks
     * @param btnSmile
     * @param numberOfColumnsInMineField
     * @param numberOfRowsInMineField
     * @param secondsPassed
     * @param currentRow
     * @param currentColumn
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

                // block has mine and is not flagged
                if (blocks[row][column].hasMine() && !blocks[row][column].isFlagged())
                {
                    // set mine icon
                    blocks[row][column].setMineIcon(false);
                    //TODO send signal RED Led
                }

                // block is flagged and doesn't not have mine
                if (!blocks[row][column].hasMine() && blocks[row][column].isFlagged())
                {
                    // set flag icon
                    blocks[row][column].setFlagIcon(false);
                    //TODO send signal YELLOW Led
                }

                // block is flagged
                if (blocks[row][column].isFlagged())
                {
                    // disable the block
                    blocks[row][column].setClickable(false);
                    //TODO turn off LED
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
     * @param minesweeperGame
     * @param blocks
     * @param btnSmile
     * @param numberOfColumnsInMineField
     * @param numberOfRowsInMineField
     * @param secondsPassed
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
        // set flagged all un-flagged blocks
        for (int row = 1; row < numberOfRowsInMineField + 1; row++)
        {
            for (int column = 1; column < numberOfColumnsInMineField + 1; column++)
            {
                blocks[row][column].setClickable(false);
                if (blocks[row][column].hasMine())
                {
                    blocks[row][column].setBlockAsDisabled(false);
                    blocks[row][column].setFlagIcon(true);
                }
            }
        }

        // show message
        minesweeperGame.showDialog("You won in " + Integer.toString(secondsPassed) + " seconds!", 1000, false, true);
    }




}
