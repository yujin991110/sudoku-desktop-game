# Overall Maintainability Assessment

### Does the system appear actively maintained?
The system appears to be partially maintained, but not actively evolving.

On the positive side, the project builds successfully using Maven and includes automated tests across multiple modules (`Model`, `Dao`, `JdbcDao`). The presence of a multi-module structure and tools such as JaCoCo suggests that the system was developed with some level of discipline and modern tooling.

However, there are also clear signs that the system is not actively maintained:
- The `View` module contains no automated tests
- Build warnings appear during execution (e.g., duplicate `logback.xml` files, SQLite compatibility warnings)
- Some modules have minimal test coverage (e.g., only 2 tests in `Dao`)

These factors suggest that while the system is functional, it is not being actively improved or refactored.

<br>

### Is technical debt visible?

Yes, there is visible technical debt in several areas.

**1. Uneven test coverage**  
The Model module has strong test coverage (44 tests), while the Dao and JdbcDao modules have significantly fewer tests, and the View module has none. This imbalance increases the risk of regressions in persistence and UI layers.

**2. Configuration issues**  
During test execution, warnings such as:
- duplicate logback.xml files on the classpath
- SQLite version mismatch warnings
indicate configuration inconsistencies and potential environment fragility.

**3. Multi-module complexity**  
Running a single test required additional configuration (-pl Model), which indicates that the build setup is somewhat complex and not fully developer-friendly.

**4. Tight coupling between layers**  
As discussed in Section 5, the persistence layer directly depends on domain objects, creating tight coupling and increasing maintenance cost.  

These issues collectively represent technical debt that will make future modifications more difficult.

<br>

### Are SOLID principles respected or violated?
The system partially respects SOLID principles, but there are also clear violations.

**Single Responsibility Principle (SRP)**  
Some classes respect SRP well, particularly in the model layer (e.g., `SudokuField`, which focuses on value management and change notification).
However, DAO classes such as `JdbcSudokuBoardDao` violate SRP by:
- handling database operations
- mapping database data to domain objects
- understanding internal domain structure
This combines multiple responsibilities into a single class.

**Open/Closed Principle (OCP)**  
The system partially follows OCP through the use of DAO abstractions (e.g., `SudokuBoardDaoFactory` allows switching implementations such as file-based vs JDBC).

However, due to tight coupling, extending functionality (e.g., adding a new persistence type) would still require modifying existing mapping logic, which weakens adherence to OCP.

**Liskov Substitution Principle (LSP)**  
DAO implementations appear to follow LSP at a high level (e.g., different DAO implementations can be used interchangeably through the factory).

However, because implementations rely on specific internal structures of domain objects, substitutability may be fragile if those structures change.

**Interface Segregation Principle (ISP)**  
There is limited evidence of fine-grained interfaces. DAO interfaces are relatively broad, and responsibilities are not clearly separated into smaller, specialized interfaces.

**Dependency Inversion Principle (DIP)**  
The system partially violates DIP. High-level modules (domain logic) are not fully decoupled from low-level modules (persistence), as DAOs directly depend on concrete domain implementations rather than abstract mappings.

<br>

### How difficult would it be to extend this system long-term?
Extending this system would be moderately difficult, primarily due to:
- Tight coupling between persistence and domain logic
- Lack of abstraction layers (e.g., no mapper layer)
- Uneven test coverage
- Missing tests in the UI layer

For example, adding a new feature such as:
- cloud-based storage
- undo/redo functionality
- enhanced UI interactions

would require changes across multiple modules and careful coordination to avoid breaking existing behavior.  

While the model layer is relatively stable and well-tested, other layers introduce risk and complexity.

<br>

### Recommendation: Incremental improvement vs major refactor
I would recommend **incremental improvement** rather than a full refactor.

**Reasons:**
1. The system is already functional and builds successfully
2. Core logic (model layer) is well-tested and stable
3. A full refactor would introduce unnecessary risk and require significant effort

Instead, improvements should focus on:
- Introducing a mapping layer between DAO and domain (as described in Section 5)
- Increasing test coverage in `Dao`, `JdbcDao`, and especially `View`
- Cleaning up configuration issues (e.g., logging setup)
- Simplifying test execution in the multi-module structure

This approach aligns with course concepts of safe, incremental refactoring and working effectively with legacy systems, rather than rewriting them entirely.

<br>

### Final conclusion
Overall, the system demonstrates a reasonable architectural foundation, particularly in its model layer and use of automated testing. However, technical debt, uneven test coverage, and tight coupling between layers limit its maintainability.

With targeted incremental improvements especially improving separation of concerns and expanding test coverage the system could become significantly more maintainable and extensible over time.