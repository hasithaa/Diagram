# Flowchart Features

- [ ] Start Node
    - [ ] Function Statement
    - [ ] Network Event Start
- [ ] End Node
    - [ ] Return Statement
- Support Switch Condition
    - [x] If statement
    - [x] If-Else statement
    - [X] If-Else-If statement
    - [X] If-Else-If-Else statement
    - [ ] Match statement
- [x] Support Generic Expression Node
- Support Network Call
  - [x] Connector call
  - [ ] Sub-Flow Support
- [ ] Support Data Features
    - [ ] Support Data Transformation
    - [ ] Support Data Conversion
- [ ] Support Special Library functions
    - [ ] IO
    - [ ] Logging
- [ ] Support Async
    - [ ] Support Clone
        - [ ] Fork Statement
        - [ ] Worker Declaration
    - [ ] Support Aggregation

# List of Pending Non-Functional Requirements

- [ ] Editable and un-editable diagram nodes and edges
    - This capture, what are the nodes in the diagram are editable.
    - Added `Editable` and `Uneditable` Java interfaces to address this. Fix this properly.
- [ ] Capture Line numbers in the diagram as Comments.
- [ ] Have better name for Nodes in the diagram.
- [ ] Clean up `Sequnce.addCompositeOperationEnd` function. This was added to fix the linking between composite
  operation start and end.

# Bugs

- [ ] Escape " with &quot; in the description of the node.