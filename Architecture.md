# Architecture Exploration and Reflection

### Architecture Style
From analyzing the project structure, the system follows a layered architecture with MVC-like characteristics.

- The View layer (```View``` package) handles the graphical user interface and user interactions
- The Model layer (```Model``` package) represents the Sudoku board and game logic
- The Data Access layer (```Dao```, ```JdbcDao``` packages) handles persistence and database operations

While the project is not a strict MVC implementation, it clearly separates concerns between UI, business logic, and data access, which is consistent with a layered design.


### How Responsibilities Are Divided Across Packages and Classes
Responsibilities are divided reasonably clearly across the codebase.

1. **View layer**  
The ```View``` module is responsible for GUI setup, scene switching, event handling, and connecting UI controls to the underlying Sudoku logic.

    **Examples:**  
   - ```View/src/main/java/sudoku/view/App.java```  
   Starts the JavaFX application, loads ```MainMenu.fxml```, and sets the initial stage title and scene.
   - ```View/src/main/java/sudoku/view/MainMenuController.java```  
   Handles difficulty selection, language changes, loading games from file/database, and transitioning to the gameplay scene.
   - ```View/src/main/java/sudoku/view/GameController.java```  
   Builds the Sudoku grid in JavaFX, connects each TextField to a SudokuField, handles save actions, and checks whether the game has ended.

    So the View layer is doing more than simple rendering: it also contains a lot of interaction and coordination logic.


2. **Model layer**  
The Model module contains the core Sudoku representation and rules.

    **Examples:**
   - ```Model/src/main/java/sudoku/model/models/SudokuBoard.java```  
   Represents the whole board, gives access to rows/columns/boxes, validates the board, checks end-game state, and uses a solver.
   - ```Model/src/main/java/sudoku/model/models/SudokuField.java```  
   Represents a single cell and stores its value.
   - ```SudokuRow.java```, ```SudokuColumn.java```, ```SudokuBox.java```  
   Represent board substructures used for Sudoku validation.
   - ```BacktrackingSudokuSolver.java```  
   Solves the board and is used when creating a new game.

    The Model layer therefore contains the real game state and the rules that define whether the Sudoku board is valid.


3. **Persistence layer**  
Persistence is split into two parts:
   - ```Dao``` contains abstractions/factory logic
   - ```JdbcDao``` contains the database-specific implementation

    **Examples:**
   - ```Dao/src/main/java/sudoku/dao/factories/SudokuBoardDaoFactory.java```  
   Creates either a file-based DAO or a JDBC-based DAO.
   - ```Dao/src/main/java/sudoku/dao/models/FileSudokuBoardDao.java```  
   Handles file persistence.
   - ```JdbcDao/src/main/java/sudoku/jdbcdao/JdbcSudokuBoardDao.java```  
   Handles SQLite persistence using JOOQ.

    This separation is useful because it distinguishes general persistence intent from concrete database implementation.


### Is There Separation Between UI and Logic?

There is some separation between UI and logic, but it is only partial.

At a package level, the project clearly separates UI classes into **View** and domain classes into **Model**. That is a positive architectural choice. However, at the class level, the separation is not very strong because the UI directly manipulates domain objects.

A clear example appears in:
- ```View/src/main/java/sudoku/view/GameController.java```
- ```Model/src/main/java/sudoku/model/models/SudokuBoard.java```
- ```Model/src/main/java/sudoku/model/models/SudokuField.java```

In ```GameController.initialize()```, the controller creates a new ```SudokuBoard``` directly:  
```
sudokuBoard = new SudokuBoard(new BacktrackingSudokuSolver());
sudokuBoard.solveGame();
gameDifficulty.clearSudokuFieldsFromSudokuBoardBasedOnDifficulty(sudokuBoard);
```

Then, inside ```initSudokuBoardGridPane()```, the UI retrieves each field directly from the model:
```SudokuField sudokuField = sudokuBoard.getField(j, i);```

Then, inside ```createTextField(...)```, the UI updates the domain object directly when the user types:  
```
sudokuField.setValue(newValue.isEmpty() ? 0 : Integer.parseInt(newValue));
if (sudokuBoard.checkEndGame()) {
    endGame();
}
```

That call chain is:  
User clicks “load from DB” → ```MainMenuController.loadSavedSudokuGameFromDB()``` → ```SudokuBoardDaoFactory.createJdbcSudokuBoardDao(...)``` → ```JdbcSudokuBoardDao.read(...)``` → ```GameController``` receives loaded ```SudokuBoard```

So yes, there is package-level separation, but the View layer still directly coordinates model and persistence operations.


### Where Is Coupling High?
A major area of high coupling is between the View layer and the Model layer.  

**Example 1**: `GameController` ↔ `SudokuBoard` / `SudokuField`

File evidence:

- `View/src/main/java/sudoku/view/GameController.java`
- `Model/src/main/java/sudoku/model/models/SudokuBoard.java`
- `Model/src/main/java/sudoku/model/models/SudokuField.java`

`GameController` depends directly on:
- `SudokuBoard`
- `SudokuField`
- `BacktrackingSudokuSolver`

This means the controller knows:
- how the board is created
- how the solver is attached
- how fields are accessed
- how field values are updated
- how end-game is checked

That is strong coupling because if the internal model design changes, the controller will likely need changes too. For example, if board access changed from `getField(x, y)` to another representation, or if field updates required validation through a service object, `GameController` would have to be rewritten.


**Example 2**: `MainMenuController` ↔ persistence layer

File evidence:
- `View/src/main/java/sudoku/view/MainMenuController.java`
- `Dao/src/main/java/sudoku/dao/factories/SudokuBoardDaoFactory.java`
- `JdbcDao/src/main/java/sudoku/jdbcdao/JdbcSudokuBoardDao.java`

`MainMenuController` directly creates DAOs using `SudokuBoardDaoFactory`, builds file/database paths, and handles loaded `SudokuBoard` objects itself.

It even contains database-path construction logic:
```
String jdbcDaoProjectPath = Paths.get("..", "JdbcDao", "sudoku.db").toString();
String databaseFilePath = Paths.get(jdbcDaoProjectPath).toAbsolutePath().toString();
```

This means UI code knows details about where the database file is located and how persistence is configured. That is a maintenance risk because storage configuration changes would affect the UI controller.

**Example 3: JDBC implementation tightly coupled to SQLite/JOOQ**

File evidence:
- `JdbcDao/src/main/java/sudoku/jdbcdao/JdbcSudokuBoardDao.java`

`JdbcSudokuBoardDao` is tightly coupled to:
- SQLite connection strings (`jdbc:sqlite:`)
- JOOQ-generated tables (`SUDOKU_BOARDS`, `SUDOKU_FIELDS`)
- SQL transaction behavior

For example:
```
connection = DriverManager.getConnection(url);
dsl = DSL.using(connection, SQLDialect.SQLITE);
```
and
```
dsl.insertInto(SUDOKU_BOARDS) ...
dsl.insertInto(SUDOKU_FIELDS) ...
```
This makes the persistence implementation very specific to its current technology stack. Replacing SQLite or JOOQ would require significant DAO changes.


### Where Is Cohesion Strong or Weak?
### Strong cohesion

A good example of strong cohesion is `SudokuBoard.java`.

File:
- `Model/src/main/java/sudoku/model/models/SudokuBoard.java`

This class is strongly cohesive because most of its methods are focused on one responsibility: representing and validating the board state.

Examples of related responsibilities inside `SudokuBoard`:

- `getField(int x, int y)`
- `setField(int x, int y, int value)`
- `isValidSudoku()`
- `checkEndGame()`
- `solveGame()`

These all relate directly to board state and board validity. Even though the class is not tiny, its methods mostly contribute to one central purpose.

Another reasonably cohesive part is `SudokuField.java`, which focuses on storing a single value and notifying listeners when that value changes.


### Weak cohesion

A weaker cohesion example is MainMenuController.java.

File:
- `View/src/main/java/sudoku/view/MainMenuController.java`

This class handles many different responsibilities:
- difficulty selection
- language selection
- scene switching
- loading from file
- loading from database
- database path setup
- author display

These responsibilities are all related to the main menu broadly, but they are still quite mixed. The class acts as:
- a UI event handler
- a navigation coordinator
- a persistence access point
- a configuration handler

That weakens cohesion because the class is doing too many kinds of work.

`GameController.java` also has mixed responsibilities. It:
- builds the grid UI
- creates a new board
- invokes the solver
- updates model objects from text input
- checks end game
- triggers save strategies

So while it is functional, it also shows weaker cohesion than an ideal controller would.


### Does the Architecture Make Maintenance Easier or Harder?

Overall, the architecture makes maintenance somewhat easier at a high level, but harder in detail when real changes are needed.

**What makes maintenance easier**  

**1. Clear package separation**  
The codebase is easier to read because UI, model, and persistence are placed in separate modules:
- `View`
- `Model`
- `Dao`
- `JdbcDao`


**2. Domain logic is not buried inside FXML files**  
Core Sudoku concepts such as board, row, column, box, and field are represented as separate classes in the Model layer.  

**3. Persistence is at least isolated into DAO-related modules**  
The existence of `SudokuBoardDaoFactory`, `FileSudokuBoardDao`, and `JdbcSudokuBoardDao` is better than placing raw SQL directly inside UI classes.

**What makes maintenance harder**  
**1. Controllers depend directly on too many layers**
MainMenuController talks directly to persistence logic, and GameController talks directly to domain logic.
  
**2. No service/application layer**  
There is no intermediate layer for actions like:
- creating a game
- loading a board
- updating a move
- validating completion

Because of this, UI controllers become the place where coordination logic accumulates.

**3. Concrete technology details leak upward**  
Database file paths and DAO creation logic appear in the UI controller. That means infrastructure changes can affect presentation code.  

**4. Changes may cascade**  
For example, if the representation of the board changes, `GameController` may need updates because it directly accesses fields with `getField(j, i)` and directly sets values through `SudokuField.setValue(...)`.

A concrete maintenance-risk call chain is:  

`MainMenuController.loadSavedSudokuGameFromDB()` → `SudokuBoardDaoFactory.createJdbcSudokuBoardDao(...)` → `JdbcSudokuBoardDao.read(...)` → `new GameController(..., sudokuBoard)`

If the persistence mechanism changes, this flow may break in the controller itself because there is no abstraction shielding the UI.

Similarly, another maintenance-risk flow is:

JavaFX `TextField` input → `GameController.createTextField()` listener → `SudokuField.setValue(...)` → `SudokuBoard.checkEndGame()`

If the model later requires stricter move validation, undo history, or immutable updates, the current direct manipulation style will be harder to adapt safely.

### Conclusion

The system shows a reasonable layered structure and is more organized than a completely tangled legacy application. The presence of separate `View`, `Model`, `Dao`, and `JdbcDao` modules helps make the system understandable and gives it some architectural discipline.

However, the separation is only partial. In practice, the controllers in the View layer are tightly coupled to both the domain model and persistence logic. `GameController` directly creates and manipulates `SudokuBoard` and `SudokuField` objects, while `MainMenuController` directly handles DAO creation and database-loading logic. This makes the system easier to understand initially, but harder to modify safely when requirements change.

So my overall assessment is that the architecture **helps with navigation and basic understanding**, but **its direct dependencies between layers make long-term maintenance harder than it needs to be.**