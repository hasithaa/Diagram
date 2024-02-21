type Node record {
    string id;
    string label?;
    NodeKind kind;
    LineRange lineRange;
    boolean returning?;
    boolean fixed?;
};

enum NodeKind {
    EVENT_HTTP_API,
    IF,
    HTTP_API_GET_CALL,
    HTTP_API_POST_CALL,
    RETURN
}

// Events (Starting elements)

type HttpApiEventNode record {
    *Node;
    EVENT_HTTP_API kind = EVENT_HTTP_API;
    string label = "HTTP API Event";
    Expression method;
    Expression path;
};

// Flow Control

type IfNode record {
    *Node;
    IF kind = IF;
    string label = "If";
    Expression condition;
    Node[] thenBranch;
    Node[] elseBranch;
};

type ReturnNode record {
    *Node;
    Expression expr;
    RETURN kind = RETURN;
    true returning = true;
};

// Output Node;
type OutputNode record {
    *Node;
    Expression variable;
};

// Library Specific Elements

type HttpGetNode record {
    *OutputNode;
    string label = "HTTP GET";
    HTTP_API_GET_CALL kind = HTTP_API_GET_CALL;
    Expression 'client;
    Expression path;
    Expression headers;
    Expression targetType;
    ExpressionList params;
};

type HTTPPostNode record {
    *OutputNode;
    string label = "HTTP POST";
    HTTP_API_POST_CALL kind = HTTP_API_POST_CALL;
    Expression 'client;
    Expression paths;
    Expression message;
    Expression headers;
    Expression mediaType;
    Expression targetType;
    ExpressionList params;
};

type Expression record {
    string key;
    string? 'type;
    string value?;
    ExpressionTypeKind typeKind = BTYPE;
    boolean optional?;
    boolean editable?;
};

enum ExpressionTypeKind {
    BTYPE,
    IDENTIFIER,
    URI_PATH
}

type ExpressionList record {|
    string key;
    string? 'type;
    Expression[] value;
    boolean optional?;
|};

type LineRange record {|
    string fileName;
    LinePosition startLine;
    LinePosition endLine;
|};

type LinePosition [int, int];

