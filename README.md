# Jack compiler

Compiles jack programming language source code into vm code.

The compiler is the first step into a two stage translation process: 
- stage one: jack high level programs (.jack) are compiled into vm code (.vm)
- stage two: vm-translator translates vm code into hack assembly language (.asm). 

VM code is a simple stack based programming language

```
    .jack   == compilation ==>   .vm   == translation ==>   .asm 
```

Finally, an assembler translates assembly into (pseudo) binary code (.hack) for the hack platform.

```
    .asm   == assembly ==>   .hack
```


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

