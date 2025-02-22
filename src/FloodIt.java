import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import javalib.impworld.World;
import javalib.impworld.WorldScene;
import javalib.worldimages.OutlineMode;
import javalib.worldimages.Posn;
import javalib.worldimages.RectangleImage;
import javalib.worldimages.TextImage;
import tester.Tester;

// Represents a single square of the game area
class Cell {
  int x;
  int y;
  Color color;
  boolean flooded;
  Cell left;
  Cell top;
  Cell right;
  Cell bottom;

  Cell(int x, int y, Color color, boolean flooded) {
    this.x = x;
    this.y = y;
    this.color = color;
    this.flooded = flooded;
  }
  
  /* TEMPLATE:
   * 
   * FIELDS:
   * ... this.x                      ... -- int
   * ... this.y                      ... -- int
   * ... this.color                  ... -- Color
   * ... this.flooded                ... -- boolean
   * ... this.left                   ... -- Cell
   * ... this.top                    ... -- Cell
   * ... this.right                  ... -- Cell
   * ... this.bottom                 ... -- Cell
   *
   * METHODS:
   * ... this.draw(WorldScene, int)  ... -- WorldScene
   */

  // Draws the cell on the given canvas
  WorldScene draw(WorldScene scene, int size) {
    scene.placeImageXY(new RectangleImage(size, size, OutlineMode.SOLID, this.color),
        this.x * size + size / 2, this.y * size + size / 2);
    return scene;
  }
}

// Main game world class
class FloodItWorld extends World {
  ArrayList<Cell> board;
  int cellSize;
  int boardSize;
  int numColors;
  int maxSteps = 40;
  int stepsUsed = 0;
  int tickCount = 0;
  Color currentFloodColor;
  boolean gameWon = false;
  String endMessage = "";
  Random rand = new Random();

  FloodItWorld(int boardSize, int numColors) {
    this.boardSize = boardSize;
    this.numColors = numColors;
    this.cellSize = 400 / boardSize;
    this.board = new ArrayList<>();
    initBoard();
  }
  
  /* TEMPLATE:
   * 
   * FIELDS:
   * ... this.board                  ... -- ArrayList<Cell>
   * ... this.cellSize               ... -- int
   * ... this.boardSize              ... -- int
   * ... this.numColors              ... -- int
   * ... this.maxSteps               ... -- int
   * ... this.stepsUsed              ... -- int
   * ... this.tickCount              ... -- int
   * ... this.currentFloodColor      ... -- Color
   * ... this.gameWon                ... -- boolean
   * ... this.endMessage             ... -- String
   * ... this.rand                   ... -- Random
   *
   * METHODS:
   * ... this.initBoard()            ... -- void
   * ... this.linkNeighbors()        ... -- void
   * ... this.onMouseClicked(Posn, String) ... -- void
   * ... this.onTick()               ... -- void
   * ... this.onKeyEvent(String)     ... -- void
   * ... this.resetGame()            ... -- void
   * ... this.changeFloodColor(Color) ... -- void
   * ... this.reFlood()              ... -- void
   * ... this.checkWin()             ... -- void
   * ... this.getMediaMessage()      ... -- String
   * ... this.endGame(String)        ... -- void
   * ... this.makeScene()            ... -- WorldScene
   */
  
  // Initializes the game board with cells
  private void initBoard() {
    for (int row = 0; row < this.boardSize; row++) {
      for (int col = 0; col < this.boardSize; col++) {
        Cell newCell = new Cell(col, row, new RandomColor(numColors).apply(rand), false);
        if (row == 0 && col == 0) {
          newCell.flooded = true;
          currentFloodColor = newCell.color;
        }
        this.board.add(newCell);
      }
    }
    linkNeighbors();
  }

  // Connects neighboring matching cells to each other
  void linkNeighbors() {
    for (int i = 0; i < board.size(); i++) {
      Cell cell = board.get(i);
      int x = cell.x;
      int y = cell.y;
      if (x > 0) {
        cell.left = board.get(y * boardSize + x - 1);
      }
      if (x < boardSize - 1) {
        cell.right = board.get(y * boardSize + x + 1);
      }
      if (y > 0) {
        cell.top = board.get((y - 1) * boardSize + x);
      }
      if (y < boardSize - 1) {
        cell.bottom = board.get((y + 1) * boardSize + x);
      }
    }
  }

  // Handles mouse click events
  public void onMouseClicked(Posn pos, String buttonName) {
    if (gameWon) {
      return;
    }
    int x = pos.x / this.cellSize;
    int y = pos.y / this.cellSize;
    int index = y * boardSize + x;
    if (index < 0 || index >= board.size()) {
      return;
    }

    Cell clickedCell = board.get(index);
    if (!clickedCell.color.equals(currentFloodColor)) {
      changeFloodColor(clickedCell.color);
      stepsUsed++;
      reFlood();
      checkWin();
      if (stepsUsed >= maxSteps) {
        if (!gameWon) { 
          endGame("Game Over Loser!");
        }
      }
    }
  }

  // Handles tick events
  // Used for timer
  public void onTick() {
    tickCount++;
  }

  // Handles key events
  public void onKeyEvent(String key) {
    if (key.equals("r")) {
      resetGame();
    }
  }

  // Resets the game to its initial state
  void resetGame() {
    this.stepsUsed = 0;
    this.tickCount = 0;
    this.gameWon = false;
    this.endMessage = "";
    this.board.clear();
    initBoard();
  }

  // Changes the flood color and updates the program
  public void changeFloodColor(Color newColor) {
    for (Cell cell : board) {
      if (cell.flooded) {
        cell.color = newColor;
      }
    }
    currentFloodColor = newColor;
    reFlood();
  }

  // Refloods the board based on the current flood color
  public void reFlood() {
    Queue<Cell> queue = new LinkedList<>();
    queue.add(board.get(0));
    ArrayList<Cell> visited = new ArrayList<>();

    while (!queue.isEmpty()) {
      Cell cell = queue.poll();
      if (!visited.contains(cell)) {
        visited.add(cell);
        cell.flooded = true;
        if (cell.left != null && cell.left.color.equals(currentFloodColor)
            && !visited.contains(cell.left)) {
          queue.add(cell.left);
        }
        if (cell.right != null && cell.right.color.equals(currentFloodColor)
            && !visited.contains(cell.right)) {
          queue.add(cell.right);
        }
        if (cell.top != null && cell.top.color.equals(currentFloodColor)
            && !visited.contains(cell.top)) {
          queue.add(cell.top);
        }
        if (cell.bottom != null && cell.bottom.color.equals(currentFloodColor)
            && !visited.contains(cell.bottom)) {
          queue.add(cell.bottom);
        }
      }
    }
  }

  // Checks if the user successfully beats the game
  public void checkWin() {
    boolean allFlooded = true;
    for (Cell cell : board) {
      if (!cell.flooded) {
        allFlooded = false;
        break;
      }
    }
    if (allFlooded) {
      gameWon = true;
      String medalMessage = getMedalMessage();
      endGame(medalMessage);
    } else if (stepsUsed >= maxSteps) {
      gameWon = false;
      endGame("Game Over! You lose. Press 'r' to play again.");
    }
  }
  
  // Returns the corresponding end message based on the time
  public String getMedalMessage() {
    if (tickCount <= 45) {
      return "GG GOLD MEDAL RECEIVED!";
    } else if (tickCount >= 45 && tickCount < 60) {
      return "GG SILVER MEDAL RECEIVED!";
    } else {
      return "GG BRONZE MEDAL RECEIVED!";
    }
  }

  // Ends the game with a message representing if the user wins or loses
  public void endGame(String message) {
    if (!message.contains("Press 'r' to play again.")) {
      message += " Press 'r' to play again.";
    }
    this.endMessage = message;
    this.gameWon = true;
  }
  
  // Creates the game scene
  public WorldScene makeScene() {
    WorldScene scene = new WorldScene(400, 500);
    for (Cell cell : board) {
      cell.draw(scene, cellSize);
    }
    RectangleImage blackBackground = new RectangleImage(400, 100, OutlineMode.SOLID, Color.BLACK);
    scene.placeImageXY(blackBackground, 200, 450);
    TextImage timer = new TextImage("Time: " + tickCount + "s", 20, Color.RED);
    scene.placeImageXY(timer, 200, 430);
    TextImage moveCounter = new TextImage("Moves: " + stepsUsed + "/" + maxSteps, 20, Color.RED);
    scene.placeImageXY(moveCounter, 200, 470);

    if (gameWon) {
      TextImage endText = new TextImage(endMessage, 15, Color.BLACK);
      scene.placeImageXY(endText, 200, 200);
    }
    return scene;
  }
}

// Helper class for generating random colors
class RandomColor {
  Color[] colors;

  /* TEMPLATE:
   * 
   * FIELDS:
   * ... this.colors                 ... -- Color[]
   *
   * METHODS:
   * ... this.apply(Random)          ... -- Color
   */
  
  // Makes a new randomColor object with specified number of colors
  public RandomColor(int numColors) {
    colors = new Color[] { Color.red, Color.blue, Color.green, Color.orange, Color.yellow,
        Color.cyan, Color.magenta, Color.pink };
    if (numColors < colors.length) {
      Color[] temp = new Color[numColors];
      System.arraycopy(colors, 0, temp, 0, numColors);
      colors = temp;
    }
  }

  // Returns a random color from the list of colors
  public Color apply(Random rand) {
    return colors[rand.nextInt(colors.length)];
  }
}

// Examples and tests for the program
class Examples {
  WorldScene baseScene;
  FloodItWorld sampleGame;
  FloodItWorld exampleGame;
  Cell cell1;
  Cell cell2;
  Cell cell3;
  Cell cell4;
  RandomColor rc8;
  RandomColor rc4;
  ArrayList<Cell> cellsList;

  /* TEMPLATE:
   * 
   * FIELDS:
   * ... this.baseScene              ... -- WorldScene
   * ... this.sampleGame             ... -- FloodItWorld
   * ... this.cell1                  ... -- Cell
   * ... this.cell2                  ... -- Cell
   * ... this.cell3                  ... -- Cell
   * ... this.cell4                  ... -- Cell
   * ... this.rc8                    ... -- RandomColor
   * ... this.rc4                    ... -- RandomColor
   * ... this.cellsList              ... -- ArrayList<Cell>
   * ... this.exampleGame            ... -- FloodItWorld
   *
   * METHODS:
   * ... this.init()                 ... -- void
   * ... this.testRandomColor(Tester) ... -- boolean
   * ... this.testApply(Tester)      ... -- void
   * ... this.testDraw(Tester) ... -- void
   * ... this.testMakeScene(Tester) ... -- void
   * ... this.testBigBang(Tester)    ... -- void
   * ... this.testInitBoard(Tester)  ... -- void
   * ... this.testLinkNeighbors(Tester) ... -- void
   * ... this.testChangeFloodColor(Tester) ... -- void
   * ... this.testReflood(Tester)    ... -- void
   * ... this.testCheckWin(Tester)   ... -- void
   * ... this.testSilverMedal        ... -- void
   * ... this.testBronzeMedal        ... -- void
   * ... this.testEndGame(Tester)    ... -- void
   * ... this.testResetGame(Tester)  ... -- void
   * ... this.testOnMouseClicked(Tester) ... - void
   * ... this.testOnTick(Tester)         ... - void
   * ... this.testOnKeyEvent(Tester)     ... - void
   */

  // Initializes example data
  void init() {
    baseScene = new WorldScene(100, 100);
    sampleGame = new FloodItWorld(10, 8);
    exampleGame = new FloodItWorld(10, 8);
    cell1 = new Cell(0, 0, Color.red, false);
    cell2 = new Cell(0, 1, Color.blue, false);
    cell3 = new Cell(1, 0, Color.green, false);
    cell4 = new Cell(1, 1, Color.orange, false);
    cellsList = new ArrayList<>(Arrays.asList(cell1, cell2, cell3, cell4));
    rc8 = new RandomColor(8);
    rc4 = new RandomColor(4);
  }

  // Test random color generator
  boolean testRandomColor(Tester t) {
    RandomColor colorPicker = new RandomColor(8);
    Random rand = new Random(123);
    Color actualFirstColor = colorPicker.apply(rand);
    Color expectedFirstColor = Color.cyan;
    boolean result = t.checkExpect(actualFirstColor, expectedFirstColor);
    rand = new Random(123);
    rand.nextInt();
    Color actualSecondColor = colorPicker.apply(rand);
    Color expectedSecondColor = Color.blue;
    result &= t.checkExpect(actualSecondColor, expectedSecondColor);
    return result;
  }
  
  // Tests for the apply method
  void testApply(Tester t) {
    init();
    Random rand = new Random(123);
    ArrayList<Color> expectedColors8 = 
        new ArrayList<>(Arrays.asList(Color.red, Color.blue,
            Color.green, Color.orange, Color.yellow, Color.cyan,
            Color.magenta, Color.pink));
    t.checkExpect(expectedColors8.contains(rc8.apply(rand)), true);
    t.checkExpect(expectedColors8.contains(rc8.apply(rand)), true);
    t.checkExpect(expectedColors8.contains(rc8.apply(rand)), true);
    t.checkExpect(expectedColors8.contains(rc8.apply(rand)), true);
    rand = new Random(123);
    ArrayList<Color> expectedColors4 = new ArrayList<>(Arrays.asList(
        Color.red, Color.blue, Color.green, Color.orange));
    t.checkExpect(expectedColors4.contains(rc4.apply(rand)), true);
    t.checkExpect(expectedColors4.contains(rc4.apply(rand)), true);
    t.checkExpect(expectedColors4.contains(rc4.apply(rand)), true);
    t.checkExpect(expectedColors4.contains(rc4.apply(rand)), true);
  }

  // Tests the draw method
  void testDraw(Tester t) {
    init();
    WorldScene initialScene = new WorldScene(20, 20);
    cell1.draw(initialScene, 10);
    WorldScene expectedScene = new WorldScene(20, 20);
    expectedScene.placeImageXY(new RectangleImage(10, 10, OutlineMode.SOLID, Color.red), 5, 5);
    t.checkExpect(initialScene, expectedScene);
  }

  // Tests the makeScene method
  void testMakeScene(Tester t) {
    init();
    WorldScene actualScene = exampleGame.makeScene();
    WorldScene expectedScene = new WorldScene(400, 500);
    for (Cell cell : exampleGame.board) {
      cell.draw(expectedScene, exampleGame.cellSize);
    }
    RectangleImage blackBackground = new RectangleImage(400, 100, OutlineMode.SOLID, Color.BLACK);
    expectedScene.placeImageXY(blackBackground, 200, 450);
    TextImage timer = new TextImage("Time: " + exampleGame.tickCount + "s", 20, Color.RED);
    expectedScene.placeImageXY(timer, 200, 430);
    TextImage moveCounter = new TextImage("Moves: " + exampleGame.stepsUsed
        + "/" + exampleGame.maxSteps, 20, Color.RED);
    expectedScene.placeImageXY(moveCounter, 200, 470);
    if (exampleGame.gameWon) {
      TextImage endText = new TextImage(exampleGame.endMessage, 18, Color.BLACK);
      expectedScene.placeImageXY(endText, 200, 200);
    }
    t.checkExpect(actualScene, expectedScene);
  }

  // Tests the main method to start the game
  void testBigBang(Tester t) {
    Main.main(new String[] {});
  }

  // Tests the initialization of the board
  void testInitBoard(Tester t) {
    init();
    t.checkExpect(sampleGame.board.size(), 100);
    t.checkExpect(sampleGame.board.get(0).flooded, true);
  }

  // Tests linking of neighboring cells
  void testLinkNeighbors(Tester t) {
    init();
    Cell cell = sampleGame.board.get(0);
    t.checkExpect(cell.right, sampleGame.board.get(1));
    t.checkExpect(cell.bottom, sampleGame.board.get(10));
  }

  // Tests the flood color changing
  void testChangeFloodColor(Tester t) {
    init();
    Color newColor = Color.yellow;
    sampleGame.changeFloodColor(newColor);
    t.checkExpect(sampleGame.currentFloodColor, newColor);
    for (Cell cell : sampleGame.board) {
      if (cell.flooded) {
        t.checkExpect(cell.color, newColor);
      }
    }
  }

  // Tests reflooding of the board
  void testReflood(Tester t) {
    init();
    sampleGame.changeFloodColor(Color.yellow);
    sampleGame.reFlood();
    for (Cell cell : sampleGame.board) {
      if (cell.flooded) {
        t.checkExpect(cell.color, Color.yellow);
      }
    }
  }

  // Tests the checkWin method
  void testCheckWin(Tester t) {
    init();
    for (Cell cell : sampleGame.board) {
      cell.flooded = true;
    }
    sampleGame.tickCount = 30;
    sampleGame.checkWin();
    t.checkExpect(sampleGame.gameWon, true);
    t.checkExpect(sampleGame.endMessage, "GG GOLD MEDAL RECEIVED! Press 'r' to play again.");
  }
  
  // Tests a silver medal case for the checkWin method
  void testSilverMedal(Tester t) {
    init();
    for (Cell cell : sampleGame.board) {
      cell.flooded = true;
    }
    sampleGame.tickCount = 55;
    sampleGame.checkWin();
    t.checkExpect(sampleGame.gameWon, true);
    t.checkExpect(sampleGame.endMessage, "GG SILVER MEDAL RECEIVED! Press 'r' to play again.");
  }
  
  // Tests a bronze medal case for the checkWin method
  void testBronzeMedal(Tester t) {
    init(); // Initialize the game setup
    for (Cell cell : sampleGame.board) {
      cell.flooded = true;
    }
    sampleGame.tickCount = 75;
    sampleGame.checkWin(); 
    t.checkExpect(sampleGame.gameWon, true);
    t.checkExpect(sampleGame.endMessage, "GG BRONZE MEDAL RECEIVED! Press 'r' to play again.");
  }

  // Tests ending the game with a correct message
  void testEndGame(Tester t) {
    init();
    sampleGame.endGame("Test message.");
    t.checkExpect(sampleGame.gameWon, true);
    t.checkExpect(sampleGame.endMessage, "Test message. Press 'r' to play again.");
  }

  // Tests resetting the game to initial state
  void testResetGame(Tester t) {
    init();
    sampleGame.stepsUsed = 40;
    sampleGame.tickCount = 100;
    sampleGame.gameWon = true;
    sampleGame.endMessage = "Test message.";
    sampleGame.resetGame();
    t.checkExpect(sampleGame.stepsUsed, 0);
    t.checkExpect(sampleGame.tickCount, 0);
    t.checkExpect(sampleGame.gameWon, false);
    t.checkExpect(sampleGame.endMessage, "");
    t.checkExpect(sampleGame.board.size(), 100);
  }
  
  // Tests handling mouse click events
  void testOnMouseClicked(Tester t) {
    init();
    Color initialColor = exampleGame.currentFloodColor;
    Cell targetCell = null;
    for (Cell cell : exampleGame.board) {
      if (!cell.color.equals(initialColor)) {
        targetCell = cell;
        break;
      }
    }
    Posn pos = new Posn(targetCell.x * exampleGame.cellSize + exampleGame.cellSize / 2,
                        targetCell.y * exampleGame.cellSize + exampleGame.cellSize / 2);
    Color clickedCellColor = targetCell.color;
    exampleGame.onMouseClicked(pos, "LeftButton");
    t.checkExpect(exampleGame.stepsUsed, 1);
    t.checkExpect(exampleGame.currentFloodColor, clickedCellColor);
  }
  
  // Tests for onTick method 
  void testOnTick(Tester t) {
    init();
    exampleGame.onTick();
    t.checkExpect(exampleGame.tickCount, 1);
  }
  
  // Tests for onKey method
  void testOnKeyEvent(Tester t) {
    init();
    exampleGame.onKeyEvent("r");
    t.checkExpect(exampleGame.stepsUsed, 0);
    t.checkExpect(exampleGame.tickCount, 0);
    t.checkExpect(exampleGame.gameWon, false);
    t.checkExpect(exampleGame.endMessage, "");
    t.checkExpect(exampleGame.board.size(), 100);
  }
}

// Main class to start the game
class Main {
  public static void main(String[] args) {
    FloodItWorld game = new FloodItWorld(20, 6);
    game.bigBang(400, 500, 1);
  }
}