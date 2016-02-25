package com.minesweeper.arduino.minesweeper.gui;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.minesweeper.arduino.minesweeper.R;
import com.minesweeper.arduino.minesweeper.entity.Block;
import com.minesweeper.arduino.minesweeper.logic.ArduinoConnection;
import com.minesweeper.arduino.minesweeper.logic.MinesweeperGameLogic;

import java.util.Random;

public class MinesweeperGame extends Activity
{
    /*
    * FIELDS are in public, too lazy to do getter and setter.
    * Getter and setter will make this file too long.
    * */
    public TextView txtMineCount;
    public TextView txtTimer;
    public ImageButton btnSmile;

    public TableLayout mineField; // table layout to add mines to

    public Block blocks[][]; // blocks for mine field
    public int blockDimension = 20; // width of each block
    public int blockPadding = 2; // padding between blocks

    //LED Colors, arduino code: setColor(red,green,blue)
    public final int RED = 1; //arduino code: setColor(255,0,0)
    public final int BLUE = 2; //arduino code: setColor(0,0,255)
    public final int GREEN = 3; //arduino code: setColor(0,255,0)


    // Don't touch the default values!
    // LED-WALL 7x14
    public int numberOfRowsInMineField = 7; //LED-WALL ROWS
    public int numberOfColumnsInMineField = 14; // LED-WALL COLUMNSs
    public int totalNumberOfMines = 10; //DEFAUL VALUE (RECOMMENDED)
    //Block size
    public int height = 45;
    public int width = 2;


    // timer to keep track of time elapsed
    public Handler timer = new Handler();
    public int secondsPassed = 0;

    public boolean isTimerStarted; // check if timer already started or not
    public boolean areMinesSet; // check if mines are planted in blocks
    public boolean isGameOver; // check if game is over
    public int minesToFind; // number of mines yet to be discovered


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // reference the Android Content(Textview,Imagebutton...)
        txtMineCount = (TextView) findViewById(R.id.MineCount);
        txtTimer = (TextView) findViewById(R.id.Timer);
        btnSmile = (ImageButton) findViewById(R.id.Smiley);
        // Set an event when a button is press
        btnSmile.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                endGame();
                startGame();
                //endExistingGame(MinesweeperGame.this, btnSmile, mineField, txtMineCount, txtTimer);
                //startNewGame(MinesweeperGame.this);
            }
        });
        //This is the table on the android device where the Blocks will be put
        mineField = (TableLayout)findViewById(R.id.MineField);
        //Short info how to start the game
        showDialog("Press smiley to start New Game", 2000, true, false);
    }


    /**
     * Call the start method in logic
     */
    private void startGame()
    {
        MinesweeperGameLogic.startNewGame(MinesweeperGame.this);
    }

    /**
     * Call the end method in logic
     * End the game xD
     * Reset the values on fields
     * send signal to reset LED-WALL
     */
    private void endGame()
    {
        MinesweeperGameLogic.endExistingGame(MinesweeperGame.this, btnSmile, mineField, txtMineCount, txtTimer);
    }

    /**
     *Show the remaining mines (not used in project)
     */
    public void updateMineCountDisplay()
    {
        if (minesToFind < 0)
        {
            txtMineCount.setText(Integer.toString(minesToFind));
        }
        else if (minesToFind < 10)
        {
            txtMineCount.setText("00" + Integer.toString(minesToFind));
        }
        else if (minesToFind < 100)
        {
            txtMineCount.setText("0" + Integer.toString(minesToFind));
        }
        else
        {
            txtMineCount.setText(Integer.toString(minesToFind));
        }
    }


    /**
     * set mines excluding the location where user clicked
     * @param currentRow
     * @param currentColumn
     */
    public void setMines(int currentRow, int currentColumn)
    {
        Random rand = new Random();
        int mineRow, mineColumn;

        for (int row = 0; row < totalNumberOfMines; row++)
        {
            mineRow = rand.nextInt(numberOfColumnsInMineField);
            mineColumn = rand.nextInt(numberOfRowsInMineField);
            if ((mineRow + 1 != currentColumn) || (mineColumn + 1 != currentRow))
            {
                if (blocks[mineColumn + 1][mineRow + 1].hasMine())
                {
                    row--; // mine is already there, don't repeat for same block
                }
                // plant mine at this location
                blocks[mineColumn + 1][mineRow + 1].plantMine();
            }
            // exclude the user clicked location
            else
            {
                row--;
            }
        }

        int nearByMineCount;

        // count number of mines in surrounding blocks
        for (int row = 0; row < numberOfRowsInMineField + 2; row++)
        {
            for (int column = 0; column < numberOfColumnsInMineField + 2; column++)
            {
                // for each block find nearby mine count
                nearByMineCount = 0;
                if ((row != 0) && (row != (numberOfRowsInMineField + 1)) && (column != 0) && (column != (numberOfColumnsInMineField + 1)))
                {
                    // check in all nearby blocks
                    for (int previousRow = -1; previousRow < 2; previousRow++)
                    {
                        for (int previousColumn = -1; previousColumn < 2; previousColumn++)
                        {
                            if (blocks[row + previousRow][column + previousColumn].hasMine())
                            {
                                // a mine was found so increment the counter
                                nearByMineCount++;
                            }
                        }
                    }

                    blocks[row][column].setNumberOfMinesInSurrounding(nearByMineCount);
                }
                // for side rows (0th and last row/column)
                // set count as 9 and mark it as opened
                else
                {
                    blocks[row][column].setNumberOfMinesInSurrounding(9);
                    blocks[row][column].OpenBlock();
                }
            }
        }
    }

    /**
     * Show empty block near the clicked block
     * @param rowClicked
     * @param columnClicked
     */
    public void rippleUncover(int rowClicked, int columnClicked)
    {
        // don't open  mined rows
        if (blocks[rowClicked][columnClicked].hasMine())
        {
            return;
        }

        // open clicked block
        blocks[rowClicked][columnClicked].OpenBlock();
        //TODO this sendCommand
        ArduinoConnection.sendCommand(rowClicked,columnClicked,GREEN);


        // if clicked block have nearby mines then don't open further
        if (blocks[rowClicked][columnClicked].getNumberOfMinesInSorrounding() != 0 )
        {
            return;
        }

        // open next 3 rows and 3 columns recursively
        for (int row = 0; row < 3; row++)
        {
            for (int column = 0; column < 3; column++)
            {
                // check all the above checked conditions
                // if met then open subsequent blocks
                if (blocks[rowClicked + row - 1][columnClicked + column - 1].isCovered()
                        && (rowClicked + row - 1 > 0) && (columnClicked + column - 1 > 0)
                        && (rowClicked + row - 1 < numberOfRowsInMineField + 1) && (columnClicked + column - 1 < numberOfColumnsInMineField + 1))
                {
                    rippleUncover(rowClicked + row - 1, columnClicked + column - 1 );
                    //TODO Turn off selected block on LED-WALL
                }
            }
        }
        return;
    }
//region TIMER
    public void startTimer()
    {
        if (secondsPassed == 0)
        {
            timer.removeCallbacks(updateTimeElasped);
            // tell timer to run call back after 1 second
            timer.postDelayed(updateTimeElasped, 1000);
        }
    }

    public void stopTimer()
    {
        // disable call backs
        timer.removeCallbacks(updateTimeElasped);
    }

    // timer call back when timer is ticked
    private Runnable updateTimeElasped = new Runnable()
    {
        public void run()
        {
            long currentMilliseconds = System.currentTimeMillis();
            ++secondsPassed;

            if (secondsPassed < 10)
            {
                txtTimer.setText("00" + Integer.toString(secondsPassed));
            }
            else if (secondsPassed < 100)
            {
                txtTimer.setText("0" + Integer.toString(secondsPassed));
            }
            else
            {
                txtTimer.setText(Integer.toString(secondsPassed));
            }

            // add notification
            timer.postAtTime(this, currentMilliseconds);
            // notify to call back after 1 seconds
            // basically to remain in the timer loop
            timer.postDelayed(updateTimeElasped, 1000);
        }
    };
    //endregion

    public void showDialog(String message, int milliseconds, boolean useSmileImage, boolean useCoolImage)
    {
        // show message
        Toast dialog = Toast.makeText(
                getApplicationContext(),
                message,
                Toast.LENGTH_LONG);

        dialog.setGravity(Gravity.CENTER, 0, 0);
        LinearLayout dialogView = (LinearLayout) dialog.getView();
        ImageView coolImage = new ImageView(getApplicationContext());
        if (useSmileImage)
        {
            coolImage.setImageResource(R.drawable.smile);
        }
        else if (useCoolImage)
        {
            coolImage.setImageResource(R.drawable.cool);
        }
        else
        {
            coolImage.setImageResource(R.drawable.sad);
        }
        dialogView.addView(coolImage, 0);
        dialog.setDuration(milliseconds);
        dialog.show();
    }
}