# Ballerina Diagram Generator

This is a tool to generate diagrams for Ballerina projects.

## Usage

Import the `hasitha/bgraph` module. That's it!

```ballerina
import hasitha/bgraph as _;

public function main() {
    int[] arr = [1, 2, 3, 4, 5];
    if arr.length() > 3 {
        io:println("Length is greater than 3");
    } else {
        return;
    }
    io:println("End of the program!");
}
```

See Diagrams in the 'diagrams' directory!

Warning: Early stage of development. Use at your own risk.