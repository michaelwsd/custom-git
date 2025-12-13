# Custom Git

This project is an implementation of a subset of Git commands in Java.

## Features

The following Git commands are implemented:

1. **init**: Initializes a new Git repository by creating the `.git` directory structure.
2. **cat-file**: Reads and prints the content of a Git object (blob) by its hash.
3. **hash-object**: Computes the SHA-1 hash of a file and stores it as a Git object.
4. **ls-tree**: Lists the contents of a tree object.
5. **write-tree**: Writes the current directory structure as a tree object.
6. **commit-tree**: Creates a new commit object with a specified tree, parent commit, and message.

## Project Structure

- `src/main/java/`: Contains the Java source code.
  - `Main.java`: Entry point of the application.
  - `Blob.java`: Handles blob-related operations.
  - `Tree.java`: Handles tree-related operations.
  - `Commit.java`: Handles commit-related operations.
  - `Utils.java`: Utility functions used across the project.
- `target/`: Contains compiled classes and build artifacts.
- `codecrafters.yml`: Configuration file for the Codecrafters platform.
- `pom.xml`: Maven configuration file for managing dependencies and building the project.

## Usage

### Prerequisites

- Java 17 or higher
- Maven

### Build the Project

Run the following command to build the project:

```sh
mvn package
```

#### Initialize a Git Repository
```sh
./your_program.sh init
```

#### Read a Git Object
```sh
./your_program.sh cat-file -p <hash>
```

#### Hash a File
```sh
./your_program.sh hash-object -w <file>
```

#### List Tree Contents
```sh
./your_program.sh ls-tree --name-only <hash>
```

#### Write a Tree Object
```sh
./your_program.sh write-tree
```

#### Create a Commit Object
```sh
./your_program.sh commit-tree <tree_sha> -p <commit_sha> -m <message>
```