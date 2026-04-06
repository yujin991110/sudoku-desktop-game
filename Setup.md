# Getting the System Running

### Environment

The system was set up and executed in the following environment:

- Operating System: Windows 10
- IDE: IntelliJ IDEA
- Java Version: JDK 21
- Build Tool: Maven 3.9+
- UI Framework: JavaFX 22


### Steps Taken to Run the System
To run the system locally, I followed the instructions provided in the repository

1. Cloned the repository:
   ```
   git clone https://github.com/Oskarowski/sudoku-desktop-game.git
   cd sudoku-desktop-game
   ```
2. Built the project using Maven: ``` mvn install ```
3. Ran the application: ``` mvn javafx:run -pl View ```

### Challenges and Issues Encountered
Although the steps appeared straightforward, getting the system to run required significant trial and error.
- **Java version mismatch issues**  
The project required Java 21+, but the system was not initially configured correctly. This caused Maven build errors. I had to explicitly verify and switch the JDK version used by both the system and the IDE.


- **JavaFX configuration complexity**  
Since JavaFX is not bundled with newer Java versions, it was not immediately clear whether additional setup was required. Running the project through Maven helped resolve this, but understanding why it worked required inspecting the project configuration.


- **Multi-module project confusion**  
The repository is structured into multiple modules (e.g., Dao, Model, View). Initially, it was unclear where the entry point of the application was. Running the project without specifying the correct module failed.
I eventually identified that the application must be run from the View module using: ```mvn javafx:run -pl View```


- **Dependency resolution and build issues**    
  The project depends on several libraries (JOOQ, SQLite, SLF4J, Logback). While Maven manages these automatically, initial builds failed until all dependencies were fully resolved via ```mvn install```.


- **Lack of clear entry point documentation**    
  There was no explicit explanation of where the main class was located, which required manually exploring the project structure to understand how the application starts.     


### How Issues Were Diagnosed and Resolved
To resolve these issues, I used a combination of:
- Reading Maven error logs carefully to identify configuration problems
- Verifying Java version settings both in the terminal and IDE
- Exploring the project directory structure to understand module relationships
- Running different Maven commands and adjusting parameters (trial-and-error)
- Identifying the correct execution module (View) by inspecting where UI-related code exists

### Overall Approach and Reflection
My approach to getting the system running was not linear. Instead, it involved iterative experimentation and investigation.    

Initially, I expected the setup process to be straightforward based on the provided instructions. However, due to the multi-module structure and external dependencies, the process required deeper inspection of the build system and project organization.   

Through this process, I learned that:

- Running a legacy system often requires understanding the build configuration, not just following instructions
- Maven plays a critical role in managing dependencies and execution, especially in multi-module projects
- Identifying the correct entry point is essential and may require manual exploration when documentation is limited

Overall, the setup process provided valuable insight into how the system is structured and how its components interact, which was helpful for understanding the codebase in later stages.



