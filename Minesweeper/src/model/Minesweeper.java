package model;



import org.junit.rules.Stopwatch;

import java.time.Duration;
import java.time.Instant;
import java.util.Random;
import java.util.Timer;


public class Minesweeper extends AbstractMineSweeper{


    private int row;
    private int col;
    private int bomb;
    private AbstractTile[][] board;
    private int flagCounter;
    private int maxFlag;
    private boolean firstClick;
    private boolean first;
    private int currentNumOfMines;
    private Instant starts;
    private Difficulty level;
    private boolean win;
    private Stopwatch stopwatch;
    private boolean stopTimer;





    public Minesweeper(){



    }


    @Override
    public int getWidth() {
        return col;
    }

    @Override
    public int getHeight() {
        return row;
    }

    public Difficulty getLevel()
    {
        return level;
    }

    @Override
    public void reset()
    {
        startNewGame(getLevel());
    }


    @Override
    public void startNewGame(Difficulty level) {
        this.viewNotifier.notifyTimeElapsedChanged(Duration.ZERO);
        //this.viewNotifier.notifyFlagCountChanged(0);

        if (level == Difficulty.EASY) {
            this.level = Difficulty.EASY;
            startNewGame(8, 8, 10);
            //this.maxFlag = 10;
            this.flagCounter = 10;
            this.viewNotifier.notifyNewGame(8,8);

        }

        if (level == Difficulty.MEDIUM) {
            startNewGame(16, 16, 40);
            this.level = Difficulty.MEDIUM;
            //this.maxFlag = 40;
            this.flagCounter = 40;
            this.viewNotifier.notifyNewGame(16,16);

        }

        if (level == Difficulty.HARD) {
            startNewGame(16, 30, 99);
            this.level = Difficulty.HARD;
            //this.maxFlag = 99;
            this.flagCounter = 99;
            this.viewNotifier.notifyNewGame(16,30);
        }
        this.viewNotifier.notifyFlagCountChanged(flagCounter);
        /***to display remaining bomb***/
        this.viewNotifier.notifyBombCountChanged(bomb);
    }

    public boolean isValid(int x, int y)
    {
        if((x >= 0) && (y >= 0)  && (y < row )  &&  (x < col) ){
            return true;
        }
        else{
            return false;
        }
    }








    @Override
    public void startNewGame(int row, int col, int explosionCount) {
        this.firstClick = true;
        this.first = true;
        this.stopTimer = false;
        this.row = row;
        this.col = col;
        this.bomb = explosionCount;
        board = new AbstractTile[row][col];
        currentNumOfMines = 0;
        Random random = new Random();

        while (currentNumOfMines < bomb ) {

            int x = random.nextInt(col);
            int y = random.nextInt(row);


            while(getTile(x,y) != null)
            {
                x = random.nextInt(col);
                y = random.nextInt(row);
            }
            Tile t = new Tile(true, x,y);
            board[y][x] = t;
            currentNumOfMines = currentNumOfMines+ 1;



        }
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < col; j++) {
                if(board[i][j] ==null)
                {

                        Tile t = new Tile(false, j, i);
                        board[i][j] = t;


                }
            }
            }



            this.viewNotifier.notifyNewGame(row,col);

            }

    @Override
    public void toggleFlag(int x, int y) {
        if(!getTile(x,y).isFlagged() && flagCounter>0)
        {
            flag(x,y);




        }

        else if (getTile(x,y).isFlagged()){
            unflag(x,y);

        }
        this.viewNotifier.notifyFlagCountChanged(flagCounter);

        }




    @Override
    public AbstractTile getTile(int x, int y) {
        if((x >= 0) && (y >= 0)  && (y < board.length )  &&  (x < board[0].length ) ){
            return board[y][x];
        }


        return null;
    }

    @Override
    public void setWorld(AbstractTile[][] world) {
       board = world;


    }

    public void checkIsWinning()
    {
        win = true;
        for(int r=0; r < getHeight(); r++)
        {
            for(int c=0; c < getWidth(); c++)
            {
                if(getTile(c,r).isExplosive() && !getTile(c,r).isFlagged())
                {
                    win = false;
                }
                else if(!getTile(c,r).isExplosive() && !getTile(c,r).isOpened())
                {
                    win = false;
                }
                else if(getTile(c,r).isExplosive() && getTile(c,r).isOpened())
                {
                    win= false;
                }
            }
        }
    }

    @Override
    public void open(int x, int y) {
        if(firstClick)
        {
            starts = Instant.now();

        }
        this.viewNotifier.notifyTimeElapsedChanged(Duration.between(starts, Instant.now()));
        if (x < 0 || x >= col || y < 0 || y >= row) {

        }
        else {

            checkIsWinning();
            if (!getTile(x,y).isExplosive() && !getTile(x,y).isFlagged()) {

                int bombs = bombCount(x,y);
                if(bombs == 0)
                {
                    getTile(x,y).open();
                    this.viewNotifier.notifyOpened(x, y, 0);
                    for (int r = - 1; r < 2; r++) {
                        for (int c =  - 1; c < 2; c++) {
                            if (isValid(x + c,y + r) && !getTile(x + c,y + r).isExplosive() && !getTile(x + c,y + r).isOpened())
                            {
                                    open(x +c,y + r);
                                }
                            }
                            }
                        }
                else {
                    getTile(x,y).open();
                    deactivateFirstTileRule();
                    this.viewNotifier.notifyOpened(x, y, bombs);

                }

            }
            else if (getTile(x,y).isExplosive() && getTile(x,y).isFlagged() )
            {
                deactivateFirstTileRule();
            }


            else if(getTile(x,y).isExplosive() && !getTile(x,y).isFlagged() && this.firstClick ) {
                replaceMine(x, y);
                board[y][x] = generateEmptyTile();
                getTile(x,y).open();
                deactivateFirstTileRule();

            }
             else if (getTile(x,y).isExplosive() && !getTile(x,y).isFlagged() && !this.firstClick ) {
                getTile(x, y).open();
                openBoard();
                stopTimer = false;
                this.viewNotifier.notifyExploded(x, y);

                //this.viewNotifier.notifyTimeElapsedChanged(Duration.between(starts, Instant.now()));
                this.viewNotifier.notifyGameLost();

            }

             else
            {
                if(win == true) {

                    this.viewNotifier.notifyTimeElapsedChanged(Duration.between(starts, Instant.now()));
                    stopTimer = false;
                    this.viewNotifier.notifyGameWon();
                }
            }
        }


        }







        public void replaceMine(int x, int y)
        {
            Random random = new Random();

            while (currentNumOfMines < bomb ) {

                int i = random.nextInt(col);
                int j = random.nextInt(row);


                if(i!= x && j!= y && !board[j][i].isExplosive())
                {
                    board[j][i] = generateExplosiveTile();
                }

                currentNumOfMines = currentNumOfMines+ 1;

        }



            }




    @Override
    public void flag(int x, int y) {
        this.viewNotifier.notifyTimeElapsedChanged(Duration.between(starts, Instant.now()));
        if (!getTile(x,y).isOpened()) {
            getTile(x,y).flag();
            this.viewNotifier.notifyFlagged(x, y);
            /***this is what desired from toggle test, counter++ when we put flag***/
            //flagCounter = flagCounter + 1;
            /***to display remaining bomb***/
            if(getTile(x,y).isExplosive())
            {
                bomb--;
                this.viewNotifier.notifyBombCountChanged(bomb);
            }
            /***this is how counter flag supposed to in real game***/
            flagCounter = flagCounter -1;
        }


    }

    @Override
    public void unflag(int x, int y) {
       this.viewNotifier.notifyTimeElapsedChanged(Duration.between(starts, Instant.now()));
        if (getTile(x,y).isFlagged()) {
            getTile(x,y).unflag();
            this.viewNotifier.notifyUnflagged(x, y);
            /***this is what desired from toggle test, counter-- when we put unflag***/
            //flagCounter = flagCounter - 1;
            /***to toggle bomb so we can test if we win the game Jpanle for winning is working***/
            if(getTile(x,y).isExplosive())
            {
                bomb++;
                this.viewNotifier.notifyBombCountChanged(bomb);
            }
            /***this is how counter flag supposed to in real game***/
            flagCounter = flagCounter+1;

        }


    }

    @Override
    public void deactivateFirstTileRule() {
        firstClick = false;



    }

    @Override
    public AbstractTile generateEmptyTile()
    {
        Tile t = new Tile(false);
        return t;

    }

    @Override
    public AbstractTile generateExplosiveTile() {
        Tile t = new Tile(true);
        return t;
    }
public void openBoard()
{
    for(int x = 0 ; x < col ; x++)
    {
        for(int y = 0 ; y < row;y++) {
            if (getTile(x,y).isExplosive()) {
                getTile(x,y).open();
                this.viewNotifier.notifyExploded(x,y);
            } else {
                getTile(x,y).open();

                this.viewNotifier.notifyOpened(x, y, bombCount(x,  y));
            }
        }
    }
}

public int bombCount(int x, int y)
{
    int bombCount = 0;

    for (int r = - 1; r < 2; r++) {
        for (int c =  - 1; c < 2; c++) {
            if (isValid(x + c,y + r)) {
                if(getTile(x + c,y + r).isExplosive())
                {
                    bombCount++;
                }
            }
        }
    }
    return( bombCount);

}
public void runTimer()
{
    starts = Instant.now();
    while(stopTimer == false)
    {
        this.viewNotifier.notifyTimeElapsedChanged(Duration.between(starts, Instant.now()));
    }
}
}
