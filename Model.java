package 2048;

import java.util.*;

public class Model {

    private static final int FIELD_WIDTH = 4;
    private Tile[][] gameTiles;
    int score=0;
    int maxTile=0;
    private Stack previousStates = new Stack();
    private Stack previousScores = new Stack();
    private boolean isSaveNeeded = true;


    public Model() {
        resetGameTiles();
    }

    public Tile[][] getGameTiles() {
        return gameTiles;
    }

    private void saveState(Tile[][] tile) {
        previousScores.push(score);

        Tile[][] temp = new Tile[tile.length][tile.length];
        for (int i = 0; i<tile.length; i++)
            for (int j = 0; j<tile[i].length; j++)
                temp[i][j] = new Tile(tile[i][j].value);

        previousStates.push(temp);
        isSaveNeeded = false;
    }

    public void rollback() {
        if (!previousStates.empty()&&!previousScores.empty()) {
            gameTiles = (Tile[][]) previousStates.pop();
            score = (int) previousScores.pop();
        }
    }

    public boolean hasBoardChanged() {

        Tile[][] previousTile = (Tile[][]) previousStates.peek();

        for (int i = 0; i < gameTiles.length; i++)
            for (int j = 0; j < gameTiles[i].length; j++)
                if (gameTiles[i][j].value != previousTile[i][j].value) return true;

        return false;

    }

    public MoveEfficiency getMoveEfficiency(Move move) {

        MoveEfficiency result;

        move.move();

        if (hasBoardChanged()) {
            result = new MoveEfficiency(getEmptyTiles().size(), score, move);
            rollback();
        } else
            result = new MoveEfficiency(-1, 0, move);

        return result;
    }

    public void autoMove() {

        PriorityQueue<MoveEfficiency> priorityQueue = new PriorityQueue(4, Collections.reverseOrder());
        priorityQueue.offer(getMoveEfficiency(new Move() {
            @Override
            public void move() {
                left();
            }
        }));

        priorityQueue.offer(getMoveEfficiency(new Move() {
            @Override
            public void move() {
                up();
            }
        }));

        priorityQueue.offer(getMoveEfficiency(new Move() {
            @Override
            public void move() {
                right();
            }
        }));

        priorityQueue.offer(getMoveEfficiency(new Move() {
            @Override
            public void move() {
                down();
            }
        }));

        priorityQueue.peek().getMove().move();

    }



    private List<Tile> getEmptyTiles() {

        List<Tile> emptyTiles = new ArrayList<>();

        for (Tile[] gameTile : gameTiles)
            for (Tile tile : gameTile)
                if (tile.isEmpty()) ((ArrayList) emptyTiles).add(tile);
        return emptyTiles;
    }

    private void addTile() {

        List<Tile> emptyTiles = getEmptyTiles();
        if (!emptyTiles.isEmpty())
            emptyTiles.get((int) (emptyTiles.size() * Math.random())).value = (Math.random() < 0.9 ? 2 : 4);
    }

    void resetGameTiles() {

        gameTiles = new Tile[FIELD_WIDTH][FIELD_WIDTH];
        for (int i = 0; i < gameTiles.length; i++)
            for (int j = 0; j < gameTiles.length; j++)
                gameTiles[i][j] = new Tile();

        score = 0;
        maxTile = 0;
        addTile();
        addTile();
    }

    private boolean compressTiles(Tile[] tiles) {
        boolean isChanged = false;
        for(int i = 0, j=0; i<tiles.length; i++) {
            if (tiles[i].value !=0) {
                tiles[j++] = tiles[i];
                if (i!=(j-1)) {
                    tiles[i] = new Tile();
                    isChanged = true;
                }
            }
        }
        return isChanged;
    }

    private boolean mergeTiles(Tile[] tiles) {
        boolean isChanged = false;
        for(int i = 0; i<tiles.length-1; i++) {
            if (tiles[i].value != 0 && tiles[i].value == tiles[i + 1].value) {
                tiles[i].value *=2;

                for (int j = i + 1; j < tiles.length-1; j++)
                    tiles[j].value = tiles[j + 1].value;

                tiles[tiles.length - 1].value = 0;

                maxTile = maxTile > tiles[i].value ? maxTile : tiles[i].value;
                score += tiles[i].value;
                isChanged = true;
            }
        }
            return isChanged;
    }

    private void rotateClockwise() {
        Tile[][] rotatedTiles = new Tile[gameTiles.length][gameTiles.length];
        for (int i = 0; i < gameTiles.length; i++){
            for (int j = 0; j < gameTiles[i].length; j++) {
                rotatedTiles[j][gameTiles[i].length-(i+1)]=gameTiles[i][j];
            }
        }
        gameTiles = rotatedTiles;
    }

    public boolean canMove() {

        for (int i = 0; i < gameTiles.length; i++) {
            for (int j = 0; j < gameTiles[i].length; j++) {
                if (gameTiles[i][j].value==0)
                    return true;
                if ((i+1)<gameTiles.length && gameTiles[i][j].value==gameTiles[i+1][j].value)
                    return true;
                if ((j+1)<gameTiles[i].length && gameTiles[i][j].value==gameTiles[i][j+1].value)
                    return true;
            }
        }

        return false;
    }


    void randomMove() {
        int n = ((int) (Math.random() * 100)) % 4;
        switch (n) {
            case 0:
                left();
                break;
            case 1:
                right();
                break;
            case 2:
                up();
                break;
            case 3:
                down();
                break;
        }
    }

    void left() {

        if (isSaveNeeded)
            saveState(gameTiles);

        boolean isChanged = false;

        for(Tile[] tiles : gameTiles) {
            isChanged|=compressTiles(tiles);
            isChanged|=mergeTiles(tiles);
        }

        if (isChanged)
            addTile();

        isSaveNeeded = true;
    }

    void right() {

        saveState(gameTiles);

        rotateClockwise();
        rotateClockwise();

        left();

        rotateClockwise();
        rotateClockwise();
    }

    void up() {

        saveState(gameTiles);

        rotateClockwise();
        rotateClockwise();
        rotateClockwise();

        left();

        rotateClockwise();

    }

    void down() {

        saveState(gameTiles);

        rotateClockwise();

        left();

        rotateClockwise();
        rotateClockwise();
        rotateClockwise();

    }


}
