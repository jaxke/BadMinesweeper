package sample;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ResourceBundle;


public class Controller implements Initializable {
    // Main "board"
    @FXML
    GridPane gridTiles = new GridPane();
    @FXML
    ImageView tile1 = new ImageView();
    @FXML
    ImageView tile2 = new ImageView();

    // TODO this should be renamed to difficulty, and not use two variables for the same thing.
    int difficulty = 10;

    File tileImg = new File("assets/tile.png");
    @FXML
    ImageView imageView = new ImageView(tileImg.toURI().toString());
    ArrayList<int[]> mineField = new ArrayList<>();
    // Remember tiles that have successfully been opened.
    ArrayList<int[]> clickedTiles = new ArrayList<>();
    // List of currently flagged tiles.
    ArrayList<int[]> flaggedTiles = new ArrayList<>();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        mineField = putMines(difficulty);
        drawBoard(difficulty, mineField);

        // TODO For testing purposes
        for (int[] mine : mineField) {
            gridTiles.add(new Label(" x"), mine[0], mine[1]);

        }
        gridTiles.addEventFilter(MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>() {
            int clickedPosititon[] = null;

            @Override
            public void handle(MouseEvent mouseEvent) {
                // Listen for a click in the grid pane, store the selected tile in (x, y) coordinates.
                for (Node n : gridTiles.getChildren()) {
                    if (n.getBoundsInParent().contains(mouseEvent.getX(), mouseEvent.getY())) {
                        clickedPosititon = new int[]{GridPane.getColumnIndex(n), GridPane.getRowIndex(n)};
                    }
                }
                // If left-click
                if (mouseEvent.getButton() == MouseButton.PRIMARY) {
                    int minesNearby;
                    minesNearby = revealTile(clickedPosititon);
                    // Clicked on a mine
                    if (minesNearby == -1)
                        revealBoard();
                    if (minesNearby == 0) {
                        clearNearbyField(clickedPosititon);
                    }
                    gridTiles.add(new Label(" " + Integer.toString(minesNearby)), clickedPosititon[0], clickedPosititon[1]);
                    clickedTiles.add(clickedPosititon);

                    // If right-click
                } else if (mouseEvent.getButton() == MouseButton.SECONDARY) {
                    // If clicked tile in clickedTiles
                    for (int[] pos : clickedTiles) {
                        // Do not allow flagging of tiles that have been opened.
                        if (Arrays.equals(pos, clickedPosititon)) {
                            return;
                        }
                    }
                    flaggedTiles = markTile(clickedPosititon, flaggedTiles);
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
        for (int[] pos : flaggedTiles) {
            if (Arrays.equals(position, pos)) {
                flaggedTiles.remove(pos);
                // Draw empty tile on top of previously flagged tile
                gridTiles.add(getEmptyTile(), position[0], position[1]);
                return flaggedTiles;
            }
        }
        // Otherwise add a flag
        flaggedTiles.add(position);
        gridTiles.add(new Label(" *"), position[0], position[1]);
        return flaggedTiles;
    }

    // TODO use this as check to whether there are mines under 'position', useful for revealing the board
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
        // TODO Reveal all blocks with 0 in them after clicked on a nearby non-mine
        // TODO special block for "0"
        //gridTiles.add(new Label(" " + Integer.toString(minesNearby)), position[0], position[1]);
        return minesNearby;
    }

    private boolean valExistsInArrayList(int[] val, ArrayList<int[]> AL) {
        for (int[] item : AL) {
            if (Arrays.equals(item, val))
                return true;
        }
        return false;
    }


    /*
    0 0 0 1 1 1
    0 0 0 1 x 1
    0 0 0 1 1 1
    0 c 0 0 0 0

    * Click on a 0
    * Go horizontally and reveal all 0's and if number encountered, reveal it and break
    * ... vertically ...
    * Do the same for each revealed 0

     */

    private void clearNearbyField(int[] pos) {
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
        int loopCounter = 0;
        // Go left
        if (!positionH.equals("left")) {
            for (int i = pos[0] - 1; i >= 0; i--) {
                int[] nextPos = {i, pos[1]};
                int minesNearNextPos = revealTile(nextPos);
                if (minesNearNextPos >= 0)
                    gridTiles.add(new Label(" " + Integer.toString(minesNearNextPos)), nextPos[0], nextPos[1]);
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
                    gridTiles.add(new Label(" " + Integer.toString(minesNearNextPos)), nextPos[0], nextPos[1]);
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
                    gridTiles.add(new Label(" " + Integer.toString(minesNearNextPos)), nextPos[0], nextPos[1]);
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
                    gridTiles.add(new Label(" " + Integer.toString(minesNearNextPos)), nextPos[0], nextPos[1]);
                if (minesNearNextPos > 0)
                    break;
            }
        }

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
    private ArrayList<int[]> putMines(int dim) {
        ArrayList<int[]> minefield = new ArrayList<>();
        int minesCount = dim;
        // min and max col/row
        int min = 0, max = dim - 1;
        while (minefield.size() < dim) {
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

    // Return 1 on death, 0 otherwise
    private int clickAction(int[] position, ArrayList<int[]> mineField) {
        for (int i = 0; i < mineField.size(); i++) {
            // Click on a mine
            if (mineField.get(i)[0] == position[0] && mineField.get(i)[1] == position[1]) {
                return 1;
            }
        }
        return 0;
    }

    // TODO Parameters not used
    private void drawBoard(int dim, ArrayList<int[]> mineField) {
        for (int i = 0; i < difficulty; i++) {
            for (int j = 0; j < difficulty; j++) {
                ImageView tileView = getEmptyTile();
                gridTiles.add(tileView, i, j);
            }
        }
    }

    // Return empty "tile", used for drawing the entire board or resetting a single tile(on removing flags).
    private ImageView getEmptyTile() {
        Image tile = new Image(tileImg.toURI().toString());
        ImageView tileView = new ImageView();
        tileView.setImage(tile);
        return tileView;
    }

}
