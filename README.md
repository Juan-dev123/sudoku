# Sudoku 📝

_Instructions for deploying, compiling, and running the project._

First go to the documents folder with `cd Documents`, clone the project with `git clone https://github.com/Juan-dev123/sudoku` and rename the project folder with SudokuPECA.

Now in the file generator/src/main/resources/inputSudoku.txt enter the sudoku you want to solve.

Compress the project folder so that there is SudokuPECA.zip.

Use the command `scp ./SudokuPECA.zip user@ip:/home/user/Documents/` to copy the .zip to the chosen machine and enter your user password.

You must connect to the computer where you want to deploy by means of a command `ssh user@ip` and enter user password.

Browse to the location of the compressed file and use the `unzip SudokuPECA.zip` command to extract its contents.

Enter the folder with `cd SudokuPECA` and build the entire project with the `gradle build` command.

Finally run the .jar generated with the command `java -jar sudokuComponent/build/libs/sudokuComponent.jar`.

## Starting 🚀

_To get a copy of the project, you just need to clone or download it_

### Prerequisites 📋

_You must have JDK 1.8 and Gradle 7+ installed._

## Built with 🛠️

* [Java SE Development Kit](https://www.oracle.com/co/java/technologies/javase-downloads.html)
* [Ice](https://zeroc.com/downloads/ice/3.7/java)
* [Gradle](https://gradle.org/releases/)
* [IntellJ IDEA](https://www.jetbrains.com/idea/download/)

## Authors ✒️

* **Juan Pablo Ramos** - *All* - [Juan-dev123](https://github.com/Juan-dev123)
* **Juan Esteban Caicedo** - *All* - [TheLordJuanes](https://github.com/TheLordJuanes)
* **Jose Alejandro Garcia** - *All* - [jose-2001](https://github.com/jose-2001)
* **Carlos Jimmy Pantoja** - *All* - [CarlosJPantoja](https://github.com/CarlosJPantoja)

## Expressions of Gratitude 🎁

* Thank you very much to all
