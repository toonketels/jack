# Jack compiler

Compiles jack programming language source code into hack assembly code.

From https://www.nand2tetris.org/


## Install

```
make build
```

## Usage

Compile all .jack sources in a directory into .vm files.

```
# generates .vm files in src/test/resources/project-11/Square
make DIR=src/test/resources/project-11/Square genDir
```

