# Testing and Build State

### Presence of tests
Tests are present in the repository. They are organized across multiple modules, primarily within the `Model`, `Dao`, and `JdbcDao` projects, each containing a `src/test/java/...` directory.

**For example:**
- Model/src/test/java/... contains:
  - `UniqueCheckerTest`
  - `SudokuBaseContainerTest`
  - `SudokuBoardTest`
  - `SudokuFieldTest`
- `Dao/src/test/java/...` contains:
  - `FileSudokuBoardDaoTest`
  - `SudokuBoardDaoFactoryTest`
- `JdbcDao/src/test/java/...` contains:
  - `JdbcSudokuBoardDaoTest`

The `View` module does not contain any test files, indicating that the UI layer is currently not covered by automated tests.

<br>

### Were the tests runnable?
Yes, the tests were runnable.

I executed the full test suite using: `mvn test`

All tests across the modules ran successfully with no failures or errors.

<br>

### Summary of results:
- **Model module**: 44 tests passed
- **Dao module**: 2 tests passed
- **JdbcDao module**: 5 tests passed
- **Total**: 51 tests passed, 0 failures, 0 errors

During execution, repeated log messages such as “Invalid Sudoku board after setting field value” appeared in `SudokuBoardTest`. However, these did not result in test failures and appear to be part of validation scenarios intentionally tested.

<br>

### Additional observation: running a single test
When attempting to run only my newly added test using:

`mvn -Dtest=SudokuFieldPropertyChangeTest test`

the build failed in the `Dao` module with the error: “No tests matching pattern were executed”  
This occurs because the project is a multi-module Maven build, and other modules (e.g., `Dao`) do not contain this specific test.  
To resolve this, I restricted execution to the Model module:

`mvn -pl Model -Dtest=SudokuFieldPropertyChangeTest test`

This successfully ran only the new test.  
This behavior highlights an important structural aspect of the system: tests are module-specific, and running targeted tests requires awareness of module boundaries.

<br>

### Coverage and what it implies
JaCoCo coverage reports were generated during the build process for each module. The number of tests suggests:
- Strong coverage in the Model layer (44 tests)
- Limited coverage in the Dao and JdbcDao layers
- No coverage in the View layer

This implies that:
- Core Sudoku logic is relatively well protected by tests
- Persistence and UI layers are more vulnerable to regression
- Coverage alone does not guarantee correctness, but it provides a useful indicator of testing focus

<br>

### Maintainability and risk
The presence of automated tests improves maintainability, especially in the model layer where correctness is critical.

However, there are risks:
- Lack of tests in the UI layer means user-facing bugs may go undetected
- Limited DAO/JDBC coverage increases risk when modifying persistence logic
- Multi-module structure makes targeted test execution slightly more complex

Overall, the system has a solid foundation for testing core logic, but uneven coverage introduces risk in other parts of the system.

<br>

### Required: Implement one automated test
### What I chose to test and why
I implemented a test for the `SudokuField` class, specifically verifying that changing the field’s value correctly triggers property change notifications.  
This behavior is important because other components, especially the UI, may rely on these notifications to update the display. If this mechanism breaks, the application could behave inconsistently even if the internal state is correct.

<br>

### Type of test
This is a unit test because:
- It tests a single class (`SudokuField`)
- It does not depend on external systems (e.g., database, file system, UI)
- It verifies behavior in isolation

<br>

### Test execution result
I executed the test independently using:  

`mvn -pl Model -Dtest=SudokuFieldPropertyChangeTest test`

The test ran successfully:
- Tests run: 2
- Failures: 0
- Errors: 0

<br>

### Refactoring required
No refactoring was required to enable this test.  
The SudokuField class already exposes methods such as:
- `addPropertyChangeListener(...)`
- `removePropertyChangeListener(...)`
- `setValue(...)`

This indicates that the class was already designed with testability in mind.

<br>

### Location of test file
The test file is located at:

`Model/src/test/java/models/SudokuFieldPropertyChangeTest.java`

<br>

### Final reflection
Overall, the project includes a functioning automated test suite with strong coverage in the model layer. However, the absence of tests in the UI layer and limited coverage in persistence layers suggest areas for improvement. While the existing tests provide confidence in core functionality, expanding test coverage would significantly improve the system’s maintainability and reliability.









