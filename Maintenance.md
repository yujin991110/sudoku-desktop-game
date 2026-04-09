# Identifying a Maintenance Opportunity
### Selected Issue: Design Flaw – Tight Coupling Between Persistence and Domain Logic

### Description of the problem
A key design flaw in the system is the tight coupling between persistence logic (DAO/JDBC) and the domain model (SudokuBoard and related classes).  
Specifically, the persistence layer (e.g., `JdbcSudokuBoardDao`) directly interacts with domain objects such as `SudokuBoard` and `SudokuField`, and is responsible for translating database rows into domain objects and vice versa.

This creates a situation where:
- Changes in the domain model (e.g., adding fields or changing structure) will require modifications in DAO classes
- Persistence logic is aware of internal structure of domain objects
- There is no clear separation between data storage representation and business logic

This violates separation of concerns and makes the system harder to maintain and extend.

<br>

### Affected classes and modules
This issue primarily affects the following modules and classes:

**JdbcDao module:**
- JdbcSudokuBoardDao
- Generated classes under sudokujdbc.jooq.generated.*

**Dao module:**
- FileSudokuBoardDao
- SudokuBoardDaoFactory

**Model module:**
- SudokuBoard
- SudokuField
- SudokuBaseContainer

The dependency flow looks like: JdbcSudokuBoardDao → SudokuBoard → SudokuField  
This shows that the persistence layer depends directly on internal domain structures.

<br>

### Architectural risk
This design introduces several risks:

**1. High coupling**  
If the structure of `SudokuBoard` changes (e.g., adding metadata or changing field representation), all DAO implementations must be updated accordingly.

**2. Fragile persistence layer**  
Persistence logic relies on detailed knowledge of how the board is structured (rows, columns, values). Any change in representation may break save/load functionality.

**3. Difficult to extend**  
Adding a new persistence method (e.g., REST API, cloud storage) would require duplicating mapping logic across multiple classes.

**4. Testing complexity**  
Testing persistence requires constructing full domain objects, increasing test complexity. Changes in domain logic may indirectly break persistence tests.

**5. Risk of hidden bugs**  
Because there is no clear abstraction layer between storage and domain, inconsistencies between database representation and in-memory objects may occur.

<br>

### Proposed seam for safe modification
To reduce coupling and improve maintainability, I would introduce a mapping layer (seam) between the persistence layer and the domain model.

**Proposed solution: Introduce a Mapper (Adapter)**  
Create a new class, for example: SudokuBoardMapper

Responsibilities:
- Convert database records → domain objects
- Convert domain objects → database format

<br>

### New structure:
JdbcSudokuBoardDao → SudokuBoardMapper → SudokuBoard

Instead of: JdbcSudokuBoardDao → SudokuBoard (direct dependency)

<br>

### Benefits of introducing this seam
**1. Reduced coupling**  
DAO no longer depends directly on internal structure of domain objects.

**2. Improved maintainability**  
Changes in domain model only require updates in the mapper, not across all DAO classes.
 
**3. Easier testing**  
Mapper logic can be tested independently with unit tests.

**4. Better extensibility**  
New persistence mechanisms can reuse the same mapping logic.

**5. Safer refactoring**  
Domain and persistence layers can evolve independently with reduced risk.

<br>

### What needs protection via tests
If this change were implemented, the following should be protected with tests:
- Correct mapping between database records and SudokuBoard
- Preservation of board values across save/load operations
- Edge cases (empty board, partially filled board, invalid values)

Existing tests such as:
- `JdbcSudokuBoardDaoTest`
- `FileSudokuBoardDaoTest`
would need to be extended or complemented with dedicated mapper tests.

<br>

### Final reflection
The current system works, but the tight coupling between persistence and domain logic increases maintenance cost and risk of future changes. Introducing a mapping layer would significantly improve modularity, testability, and long-term maintainability without requiring a full system rewrite.