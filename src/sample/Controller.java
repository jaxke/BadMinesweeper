package sample;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.ResourceBundle;


public class Controller implements Initializable {
    // Main "board"
    @FXML
    GridPane gridTiles = new GridPane();

    //TODO next two vars inside method?
    // Square dimension of board
    int difficulty = 15;
    int minesCount = 1;

    // List of (x,y) coordinates where mines are located
    ArrayList<int[]> mineField = new ArrayList<>();
    // Remember tiles that have successfully been opened.
    ArrayList<int[]> clickedTiles = new ArrayList<>();
    // List of currently flagged tiles.
    ArrayList<int[]> flaggedTiles = new ArrayList<>();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        mineField = putMines(difficulty, minesCount);
        drawBoard();

        // TODO For testing purposes
        for (int[] mine : mineField) {
            //gridTiles.add(getTile("mine"), mine[0], mine[1]);

        }
        gridTiles.addEventFilter(MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>() {
            int clickedPosition[] = null;

            @Override
            public void handle(MouseEvent mouseEvent) {
                // Listen for a click in the grid pane, store the selected tile in (x, y) coordinates.
                for (Node n : gridTiles.getChildren()) {
                    if (n.getBoundsInParent().contains(mouseEvent.getX(), mouseEvent.getY())) {
                        clickedPosition = new int[]{GridPane.getColumnIndex(n), GridPane.getRowIndex(n)};
                    }
                }
                // If left-click
                if (mouseEvent.getButton() == MouseButton.PRIMARY) {
                    int minesNearby = revealTile(clickedPosition);
                    // Clicked on a mine
                    if (minesNearby == -1)
                        // TODO implement
                        revealBoard();
                    if (minesNearby == 0) {
                        ArrayList<int[]> newlyRevealed = clearNearbyField(clickedPosition);
                        while (newlyRevealed != null) {
                            ArrayList<int[]> nextIter = new ArrayList<>();
                            for (int[] tile : newlyRevealed) {
                                ArrayList<int[]> newly2 = clearNearbyField(tile);
                                if (newly2 != null)
                                    nextIter.addAll(newly2);
                            }
                            if (nextIter.size() > 0)
                                newlyRevealed = nextIter;
                            else
                                newlyRevealed = null;
                        }
                    }
                    clickedTiles.add(clickedPosition);
                    drawRevealedBlocks(minesNearby, clickedPosition);

                    // If right-click
                } else if (mouseEvent.getButton() == MouseButton.SECONDARY) {
                    // Can't flag opened tile
                    if (valExistsInArrayList(clickedPosition, clickedTiles))
                        return;
                    flaggedTiles = markTile(clickedPosition, flaggedTiles);
                    if (checkIfAllMinesFlagged(flaggedTiles, mineField)) {
                        revealBoard();
                    }
                }
            }
        });
    }

    // Check if all flags are on top of mines(every mine has to be flagged)
    private boolean checkIfAllMinesFlagged(ArrayList<int[]> flaggedTiles, ArrayList<int[]> mineField) {
        if (flaggedTiles.size() != mineField.size()) {
            return false;
        }
        for (int[] flaggedTile : flaggedTiles) {
            // use boolean instead of breaking out of a nested loop...
            boolean flaggedCorrectly = false;
            for (int[] mine : mineField) {
                if (Arrays.equals(flaggedTile, mine)) {
                    flaggedCorrectly = true;
                }
            }
            if (!flaggedCorrectly)
                return false;
        }
        return true;
    }


    // Cycle flag on/flag off on right clicking a tile.
    private ArrayList<int[]> markTile(int[] position, ArrayList<int[]> flaggedTiles) {
        // Check if flag already exists in 'position' -> remove it
        if (valExistsInArrayList(position, flaggedTiles)) {
            flaggedTiles.remove(position);
            // Draw empty tile on top of previously flagged tile
            gridTiles.add(getTile("tile"), position[0], position[1]);
            return flaggedTiles;
        }
        // Otherwise add a flag
        flaggedTiles.add(position);
        gridTiles.add(getTile("flag"), position[0], position[1]);
        return flaggedTiles;
    }

    // Parameter: clicked block on grid (x, y)
    // Return: Amount of mines in blocks that are near the clicked block(diagonally or directly next to clicked block).
    // OR -1 on mine.
    private int revealTile(int[] position) {
        System.out.println("testing pos: " + Arrays.toString(position));
        // This is a mine
        if (valExistsInArrayList(position, mineField))
            return -1;
        int positionInt = gridPosToInt(position);
        int minesNearby = 0;
        int dangerPos[];
        // These arrays show which tiles are near to the clicked tile. If clicked on top or bottom edge -> ignore tiles
        // that span to the other side because of the way the tiles are numbered(in single int form).
        int minesNearbyPos[] = {positionInt - difficulty, positionInt - difficulty - 1, positionInt - difficulty + 1,
                positionInt + difficulty, positionInt + difficulty - 1, positionInt + difficulty + 1,
                positionInt - 1, positionInt + 1};
        int minesNearbyPosOnTopEdge[] = {positionInt - difficulty, positionInt - difficulty + 1,
                positionInt + difficulty, positionInt + difficulty + 1,
                positionInt + 1};
        int minesNearbyPosOnBottomEdge[] = {positionInt - difficulty, positionInt - difficulty - 1,
                positionInt + difficulty, positionInt + difficulty - 1,
                positionInt - 1};
        // Determine whether clicked position is in the top edge, bottom edge or neither. Use above "danger" arrays accordingly.
        if (positionInt % difficulty == 0) {
            dangerPos = minesNearbyPosOnTopEdge;
        } else if (positionInt % difficulty == difficulty - 1) {
            dangerPos = minesNearbyPosOnBottomEdge;
        } else {
            dangerPos = minesNearbyPos;
        }
        for (int[] mine : mineField) {
            int mineArr[] = {mine[0], mine[1]};
            int minePosInt = gridPosToInt(mineArr);
            for (int pos : dangerPos) {
                if (minePosInt == pos)
                    minesNearby++;
            }
        }
        return minesNearby;
    }

    private boolean valExistsInArrayList(int[] val, ArrayList<int[]> AL) {
        for (int[] item : AL) {
            if (Arrays.equals(item, val))
                return true;
        }
        return false;
    }

    private void drawRevealedBlocks(int value, int[] pos) {
        gridTiles.add(getTile(Integer.toString(value)), pos[0], pos[1]);
    }

    // This method "auto reveals" blocks near a "0 block". Is used recursively so when 0 blocks are encountered, same will be done to each of them.
    // This will only be called on clicking "0 blocks".
    // Parameter: block whose neighbours shall be revealed.
    // Return: blocks that were revealed during method run.
    private ArrayList<int[]> clearNearbyField(int[] pos) {
        // Store all tiles that are revealed in this method run.
        ArrayList<int[]> newlyRevealed = new ArrayList<>();
        String positionV, positionH;
        int positionInt = gridPosToInt(pos);
        if (positionInt % difficulty == 0) {
            positionV = "top";
        } else if (positionInt % difficulty == difficulty - 1) {
            positionV = "bottom";
        } else {
            positionV = "middle";
        }

        if (positionInt <= difficulty) {
            positionH = "left";
        } else if (positionInt >= difficulty * difficulty) {
            positionH = "right";
        } else {
            positionH = "middle";
        }
        // Go left (if not already leftmost)
        // TODO Should this not work without positionH and positionV because of the for-loop conditions??
        if (!positionH.equals("left")) {
            for (int i = pos[0] - 1; i >= 0; i--) {
                // "Goes one to the left"
                int[] nextPos = {i, pos[1]};
                int minesNearNextPos = revealTile(nextPos);
                // If not a mine(mine == -1)
                if (minesNearNextPos >= 0) {
                    if (!valExistsInArrayList(nextPos, clickedTiles)) {
                        clickedTiles.add(nextPos);
                        drawRevealedBlocks(minesNearNextPos, nextPos);
                        if (minesNearNextPos == 0)
                            newlyRevealed.add(nextPos);
                        // Ignore rest if the block was already revealed one way or another.
                    } else {
                        break;
                    }
                }
                // Do not reveal anything beyond a numbered block just like the original Minesweeper.
                if (minesNearNextPos > 0)
                    break;
            }
        }
        // Go right
        if (!positionH.equals("right")) {

            for (int i = pos[0] + 1; i <= difficulty - 1; i++) {
                int[] nextPos = {i, pos[1]};
                int minesNearNextPos = revealTile(nextPos);
                if (minesNearNextPos >= 0)
                    if (!valExistsInArrayList(nextPos, clickedTiles)) {
                        clickedTiles.add(nextPos);
                        drawRevealedBlocks(minesNearNextPos, nextPos);
                        //gridTiles.add(new Label(" " + Integer.toString(minesNearNextPos)), nextPos[0], nextPos[1]);
                        if (minesNearNextPos == 0)
                            newlyRevealed.add(nextPos);
                    }
                if (minesNearNextPos > 0)
                    break;
            }
        }
        // Go up
        if (!positionV.equals("top")) {

            for (int i = pos[1] - 1; i >= 0; i--) {
                int[] nextPos = {pos[0], i};
                int minesNearNextPos = revealTile(nextPos);
                if (minesNearNextPos >= 0)
                    if (!valExistsInArrayList(nextPos, clickedTiles)) {
                        clickedTiles.add(nextPos);
                        drawRevealedBlocks(minesNearNextPos, nextPos);
                        //gridTiles.add(new Label(" " + Integer.toString(minesNearNextPos)), nextPos[0], nextPos[1]);
                        if (minesNearNextPos == 0)
                            newlyRevealed.add(nextPos);
                    }
                if (minesNearNextPos > 0)
                    break;
            }
        }
        // Go down
        if (!positionV.equals("bottom")) {

            for (int i = pos[1] + 1; i <= difficulty - 1; i++) {
                int[] nextPos = {pos[0], i};
                int minesNearNextPos = revealTile(nextPos);
                if (minesNearNextPos >= 0)
                    if (!valExistsInArrayList(nextPos, clickedTiles)) {
                        clickedTiles.add(nextPos);
                        drawRevealedBlocks(minesNearNextPos, nextPos);
                        //gridTiles.add(new Label(" " + Integer.toString(minesNearNextPos)), nextPos[0], nextPos[1]);
                        if (minesNearNextPos == 0)
                            newlyRevealed.add(nextPos);
                    }
                // Do not go further if there's a mine nearby to a newly revealed block.
                if (minesNearNextPos > 0)
                    break;
            }
        }

        if (newlyRevealed.size() > 0)
            return newlyRevealed;
            // The loop calling this method will be broken on null(when nothing new was revealed).
        else
            return null;
    }

    private void revealBoard() {
        System.exit(0);
        // TODO
    }

    // Convert (x, y) into a single integer. Top to bottom; If "difficulty" is 10 first column is 0 through 10, second is 11 through 20 etc...
    private int gridPosToInt(int[] pos) {
        for (int x = 0; x < difficulty; x++) {
            for (int y = 0; y < difficulty; y++) {
                if (pos[0] == x && pos[1] == y) {
                    return x * difficulty + y;
                }
            }
        }
        return 0; // Should not happen
    }

    // Return array of (x, y) coordinates where the mines are located. Randomly picked.
    private ArrayList<int[]> putMines(int dim, int minesCount) {
        ArrayList<int[]> minefield = new ArrayList<>();
        // min and max col/row
        int min = 0, max = dim - 1;
        while (minefield.size() < minesCount) {
            int mineX = min + (int) (Math.random() * ((max - min) + 1));
            int mineY = min + (int) (Math.random() * ((max - min) + 1));
            int[] mineLocation = {mineX, mineY};
            // Make sure there's not two or more mines in the same block.
            if (!hasThisCoordinate(minefield, mineLocation))
                minefield.add(mineLocation);
        }
        return minefield;
    }

    // Check if the minefield already has this randomly generated mine.
    private boolean hasThisCoordinate(ArrayList<int[]> AL, int[] c) {
        for (int[] item : AL) {
            if (Arrays.equals(item, c))
                return true;
        }
        return false;
    }

    private void drawBoard() {
        for (int i = 0; i < difficulty; i++) {
            for (int j = 0; j < difficulty; j++) {
                gridTiles.add(getTile("tile"), i, j);
            }
        }
    }

    private ImageView getTile(String val) {
        HashMap<String, String> tiles = new HashMap<>();
        tiles.put("1", "1.png");
        tiles.put("2", "2.png");
        tiles.put("3", "3.png");
        tiles.put("4", "4.png");
        tiles.put("5", "5.png");
        tiles.put("6", "6.png");
        tiles.put("7", "7.png");
        tiles.put("8", "8.png");
        tiles.put("9", "9.png");
        tiles.put("0", "0.png");
        tiles.put("tile", "tile.png");
        tiles.put("flag", "flag.png");
        tiles.put("mine", "mine.png");
        File tileImg = new File("assets/" + tiles.get(val));
        Image tile = new Image(tileImg.toURI().toString());
        ImageView tileView = new ImageView();
        tileView.setImage(tile);
        return tileView;
    }

}
