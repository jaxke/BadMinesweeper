package sample;

import com.jconf.Jconf;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.ResourceBundle;

// Eclipse will warn about _everything_
@SuppressWarnings("restriction")
public class Controller implements Initializable {
    // Main "board"
    @FXML
    GridPane gridTiles = new GridPane();
    @FXML
    Button buttonNewGame = new Button();
    // Default values if settings can't be read.
    int dim[] = {20, 15};
    int minesCount = 20;
    String minesRemainingHint = "Mines remaining: ";
    @FXML
    Label labelMinesRemaining = new Label();
    // TODO same for "mines"
    @FXML
    TextField textFieldWidth = new TextField();
    @FXML
    TextField textFieldHeight = new TextField();
    @FXML
    TextField textFieldMines = new TextField();
    @FXML
    Label labelWarning = new Label();

    // List of (x,y) coordinates where mines are located
    private ArrayList<int[]> mineField = new ArrayList<>();
    // Remember tiles that have successfully been opened.
    private ArrayList<int[]> clickedTiles = new ArrayList<>();
    // List of currently flagged tiles.
    private ArrayList<int[]> flaggedTiles = new ArrayList<>();

    private String settingsFile = "settings";
    private Jconf conf;

    private String getConf(String category, String val) {
        try {
            conf = new Jconf(settingsFile);
        } catch (Exception e) {
            System.out.println("Cannot read value " + val + " from " + settingsFile + ". Using default values.");
            return null;
        }
        return (String) conf.getVal(category, val);
    }

    private boolean newGame() {
        labelWarning.setText("");
        labelMinesRemaining.setText(minesRemainingHint + Integer.toString(minesCount));
        //buttonNewGame.setDisable(true);
        mineField = putMines(dim);
        clickedTiles = new ArrayList<>();
        flaggedTiles = new ArrayList<>();
        drawBoard(dim);
        if (mineField == null) {
            labelWarning.setText("Can't fit " + minesCount + " mines into " + dim[0] + "x" + dim[1] + " field!");
            // When false is returned, new game will not be started(wait for player to input valid values).
            return false;
        }
        return true;
    }

    @FXML
    private void handleButtonAction(ActionEvent event) {
        if (event.getSource() == buttonNewGame) {
            if(textFieldWidth.getText().length() == 0) {
                labelWarning.setText("Enter width!");
                return;
            }
            else if(textFieldHeight.getText().length() == 0) {
                labelWarning.setText("Enter height!");
                return;
            }
            else if(textFieldMines.getText().length() == 0) {
                labelWarning.setText("Enter amount of mines!");
                return;
            }
            try {
                int newWidth = Integer.parseInt(textFieldWidth.getText().trim());
                int newHeight = Integer.parseInt(textFieldHeight.getText().trim());
                dim = new int[]{newWidth, newHeight};
                minesCount = Integer.parseInt(textFieldMines.getText().trim());
                conf.set("General", "Width", String.valueOf(newWidth));
                conf.set("General", "Height", String.valueOf(newHeight));
                conf.set("General", "Mines", String.valueOf(textFieldMines.getText().trim()));
            } catch (Exception e) {  // TODO Catch what
                labelWarning.setText("Error has occurred, please check your inputs.");
                e.printStackTrace();
                return;
            }
            newGame();
        }
    }

    // Might be bad design to forcefully reset conf each time it's corrupted but it's only 3 values for now
    private void resetConfToDefaults(){
    	// TODO write file completely because the category may be broken
        try {
            conf.set("General", "Width", "15");
            conf.set("General", "Height", "15");
            conf.set("General", "Mines", "15");
        } catch (Exception e){
            // TODO do something
        	e.printStackTrace();
        }
    }

    @SuppressWarnings("restriction")
	@Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
    	boolean cantRead = false;
        int w, h;
        try {
            w = Integer.parseInt(getConf("General", "Width"));
            h = Integer.parseInt(getConf("General", "Height"));
            dim = new int[]{w, h};
        } catch (Exception e) {  // TODO catch what
            //e.printStackTrace();
            System.out.println("Problem parsing width and height values to integers.");
            resetConfToDefaults();
            labelWarning.setText("Using default values, config file was corrupted.");
            String x = getConf("General", "Width");
            w = Integer.parseInt(getConf("General", "Width"));
            h = Integer.parseInt(getConf("General", "Height"));
            dim = new int[]{w, h};
        }
        // If above fails, mine count doesn't get ignored. Hence two trys.
        try {
            minesCount = Integer.parseInt(getConf("General", "Mines"));
        } catch (Exception e) {  // TODO catch what
            //e.printStackTrace();
            System.out.println("Problem parsing mines count value to integer.");
            resetConfToDefaults();
        }
        // Reset all runtime settings
        if(!newGame())
            return;

        // Paint all mines for testing purposes
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
                    if (minesNearby == -1) {
                        labelMinesRemaining.setText("You lost!");
                        revealBoard();
                    }
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
                    labelMinesRemaining.setText(minesRemainingHint + Integer.toString(minesCount - flaggedTiles.size()));

                    if (checkIfAllMinesFlagged(flaggedTiles, mineField)) {
                        labelMinesRemaining.setText("You won");
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
        if (flaggedTiles.size() == minesCount) {
            return flaggedTiles;
        }
        // Check if flag already exists in 'position' -> remove it
        if (valExistsInArrayList(position, flaggedTiles)) {
            for (int[] tile : flaggedTiles) {
                if (Arrays.equals(tile, position)) {
                    flaggedTiles.remove(tile);
                    break;
                }
            }
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
        int dimX = dim[0], dimY = dim[1];
        // This is a mine
        if (valExistsInArrayList(position, mineField))
            return -1;
        int positionInt = gridPosToInt(position);
        int minesNearby = 0;
        int dangerPos[];
        // These arrays show which tiles are near to the clicked tile. If clicked on top or bottom edge -> ignore tiles
        // that span to the other side because of the way the tiles are numbered(in single int form).
        int minesNearbyPos[] = {positionInt - dimY, positionInt - dimY - 1, positionInt - dimY + 1,
                positionInt + dimY, positionInt + dimY - 1, positionInt + dimY + 1,
                positionInt - 1, positionInt + 1};
        int minesNearbyPosOnTopEdge[] = {positionInt - dimY, positionInt - dimY + 1,
                positionInt + dimY, positionInt + dimY + 1,
                positionInt + 1};
        int minesNearbyPosOnBottomEdge[] = {positionInt - dimY, positionInt - dimY - 1,
                positionInt + dimY, positionInt + dimY - 1,
                positionInt - 1};
        // Determine whether clicked position is in the top edge, bottom edge or neither. Use above "danger" arrays accordingly.
        if (positionInt % dimY == 0) {
            dangerPos = minesNearbyPosOnTopEdge;
        } else if (positionInt % dimY == dimY - 1) {
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
    // TODO? redundancy but maybe not worth fixing? (left, right, up, down parts do the same things essentially)
    private ArrayList<int[]> clearNearbyField(int[] pos) {
        int dimX = dim[0], dimY = dim[1];
        // Store all tiles that are revealed in this method run.
        ArrayList<int[]> newlyRevealed = new ArrayList<>();

        // Go left (if not already leftmost)
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

        // Go right
        for (int i = pos[0] + 1; i <= dimX - 1; i++) {
            int[] nextPos = {i, pos[1]};
            int minesNearNextPos = revealTile(nextPos);
            if (minesNearNextPos >= 0)
                if (!valExistsInArrayList(nextPos, clickedTiles)) {
                    clickedTiles.add(nextPos);
                    drawRevealedBlocks(minesNearNextPos, nextPos);
                    if (minesNearNextPos == 0)
                        newlyRevealed.add(nextPos);
                }
            if (minesNearNextPos > 0)
                break;
        }

        // Go up
        for (int i = pos[1] - 1; i >= 0; i--) {
            int[] nextPos = {pos[0], i};
            int minesNearNextPos = revealTile(nextPos);
            if (minesNearNextPos >= 0)
                if (!valExistsInArrayList(nextPos, clickedTiles)) {
                    clickedTiles.add(nextPos);
                    drawRevealedBlocks(minesNearNextPos, nextPos);
                    if (minesNearNextPos == 0)
                        newlyRevealed.add(nextPos);
                }
            if (minesNearNextPos > 0)
                break;
        }

        // Go down
        for (int i = pos[1] + 1; i <= dimY - 1; i++) {
            int[] nextPos = {pos[0], i};
            int minesNearNextPos = revealTile(nextPos);
            if (minesNearNextPos >= 0)
                if (!valExistsInArrayList(nextPos, clickedTiles)) {
                    clickedTiles.add(nextPos);
                    drawRevealedBlocks(minesNearNextPos, nextPos);
                    if (minesNearNextPos == 0)
                        newlyRevealed.add(nextPos);
                }
            if (minesNearNextPos > 0)
                break;
        }

        if (newlyRevealed.size() > 0)
            return newlyRevealed;
            // The loop calling this method will be broken on null(when nothing new was revealed).
        else
            return null;
    }

    private void revealBoard() {
        for (int i = 0; i < dim[0]; i++) {
            for (int j = 0; j < dim[1]; j++) {
                int[] tile = {i, j};
                if (valExistsInArrayList(tile, mineField)) {
                    gridTiles.add(getTile("mine"), i, j);
                } else {
                    gridTiles.add(getTile(Integer.toString(revealTile(tile))), i, j);
                }
            }
        }
    }

    // Convert (x, y) into a single integer. Top to bottom; If "difficulty" is 10 first column is 0 through 10, second is 11 through 20 etc...
    private int gridPosToInt(int[] pos) {
        for (int x = 0; x < dim[0]; x++) {
            for (int y = 0; y < dim[1]; y++) {
                if (pos[0] == x && pos[1] == y) {
                    return x * dim[1] + y;
                }
            }
        }
        return 0; // Should not happen
    }

    // Return array of (x, y) coordinates where the mines are located. Randomly picked.
    private ArrayList<int[]> putMines(int[] dim) {
        if (dim[0] * dim[1] < minesCount) {
            System.out.println("Too many mines for these dimensions!");
            return null;
        }
        ArrayList<int[]> minefield = new ArrayList<>();
        // min and max col/row
        int min = 0, maxX = dim[0] - 1, maxY = dim[1] - 1;
        while (minefield.size() < minesCount) {
            int mineX = min + (int) (Math.random() * ((maxX - min) + 1));
            int mineY = min + (int) (Math.random() * ((maxY - min) + 1));
            int[] mineLocation = {mineX, mineY};
            // Make sure there's not two or more mines in the same block.
            if (!valExistsInArrayList(mineLocation, minefield))
                minefield.add(mineLocation);
        }
        return minefield;
    }

    private void drawBoard(int[] dim) {
        for (int i = 0; i < dim[0]; i++) {
            for (int j = 0; j < dim[1]; j++) {
                gridTiles.add(getTile("tile"), i, j);
            }
        }
    }

    // Return image of different types of blocks
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
