type Diagram record {|
    string name;
    Node[] nodes;
    Client[] clients = [];
|};

type Node record {|
    readonly string id;
    readonly string label?;
    readonly NodeKind kind;
    readonly LineRange lineRange;
    readonly boolean returning?;
    readonly boolean fixed?;
    map<Expression> nodeProperties?;
    Branch[] branches?;
    readonly int flags = 0;
|};

type Branch record {|
    readonly BLOCK kind = BLOCK;
    readonly string label;
    Node[] children;
|};

enum NodeKind {
    EVENT_HTTP_API,
    BLOCK,
    IF,
    HTTP_API_GET_CALL,
    HTTP_API_POST_CALL,
    EXPRESSION,
    RETURN
}

type Expression record {|
    readonly string label;
    string? 'type;
    readonly ExpressionTypeKind typeKind = BTYPE;
    readonly boolean optional?;
    readonly boolean editable?;
    readonly string documentation?;
    string value?;
|};

enum ExpressionTypeKind {
    BTYPE,
    IDENTIFIER,
    URI_PATH
}

type ExpressionList record {|
    readonly string label;
    readonly string? 'type;
    Expression[] value;
    readonly boolean optional?;
|};

type LineRange record {|
    string fileName;
    LinePosition startLine;
    LinePosition endLine;
|};

type LinePosition [int, int];


type Client record {|
    readonly string id;
    readonly string label?;
    readonly ClientKind kind;
    readonly LineRange lineRange;
    readonly ClientScope scope = GLOBAL;
    readonly string value;
    readonly int flags = 0;
|};

// Only for visualization purposes
enum ClientKind {
    HTTP,
    OTHER
}

enum ClientScope {
    LOCAL,
    OBJECT,
    GLOBAL
}

// Events (Starting elements)

type HttpApiEventNode record {|
    *Node;
    readonly EVENT_HTTP_API kind = EVENT_HTTP_API;
    readonly EVENT_HTTP_API_LABEL label = EVENT_HTTP_API_LABEL;
    record {|
        HttpApiEventNodeMethodExpression method = {value: "GET"};
        HttpApiEventNodePathExpression path = {value: "."};
    |} nodeProperties;
    readonly false returning = false;
    readonly true fixed = true;
|};

// Flow Control

type IfNode record {|
    *Node;
    readonly IF kind = IF;
    readonly IF_LABEL label = IF_LABEL;
    record {|
        IfNodeConditionExpression condition = {value: "true"};
    |} nodeProperties;
    [IfBranchThen, IfBranchElse] branches;
    readonly false returning = false;
    readonly false fixed = false;
|};

enum IF_BRANCH {
    IF_BRANCH_THEN = "Then",
    IF_BRANCH_ELSE = "Else"
}

type IfBranchThen record {|
    readonly BLOCK kind = BLOCK;
    readonly IF_BRANCH_THEN label = IF_BRANCH_THEN;
    Node[] children;
|};

type IfBranchElse record {|
    readonly BLOCK kind = BLOCK;
    readonly IF_BRANCH_ELSE label = IF_BRANCH_ELSE;
    Node[] children;
|};

type ReturnNode record {|
    *Node;
    readonly RETURN kind = RETURN;
    readonly RETURN_LABEL label = RETURN_LABEL;
    record {|
        ReturnExpression expression = {'type: "()", value: "()"};
    |} nodeProperties;
    readonly true returning = true;
    readonly false fixed = false;
|};

// Library Specific Elements

type HttpGetNode record {|
    *Node;
    readonly HTTP_API_GET_LABEL label = HTTP_API_GET_LABEL;
    readonly HTTP_API_GET_CALL kind = HTTP_API_GET_CALL;
    record {|
        HttpApiGetClientExpression 'client;
        HttpApiGetPathExpression path = {value: "."};
        HttpApiGetHeadersExpression headers?;
        HttpApiGetTargetTypeExpression targetType;
        VariableExpression variable;
    |} nodeProperties;
    readonly false returning = false;
    readonly false fixed = false;
|};

// Default Expression Node 

type ExpressionNode record {|
    *Node;
    readonly EXPRESSION_LABEL label = EXPRESSION_LABEL;
    readonly EXPRESSION kind = EXPRESSION;
    record {|
        VariableExpression variable;
        ExpressionRHS expression;
    |} nodeProperties;
    readonly false returning = false;
    readonly false fixed = false;
|};

// Expressions

//// HTTP Event Specific Expressions

type HttpApiEventNodeMethodExpression record {|
    readonly EVENT_HTTP_API_METHOD label = EVENT_HTTP_API_METHOD;
    () 'type = ();
    readonly IDENTIFIER typeKind = IDENTIFIER;
    readonly boolean optional = false;
    readonly boolean editable = true;
    readonly EVENT_HTTP_API_METHOD_DOC documentation = EVENT_HTTP_API_METHOD_DOC;
    string value;
|};

type HttpApiEventNodePathExpression record {|
    readonly EVENT_HTTP_API_PATH label = EVENT_HTTP_API_PATH;
    () 'type = ();
    readonly URI_PATH typeKind = URI_PATH;
    readonly boolean optional = false;
    readonly boolean editable = true;
    readonly EVENT_HTTP_API_PATH_DOC documentation = EVENT_HTTP_API_PATH_DOC;
    string value;
|};

//// If Specific Expressions

type IfNodeConditionExpression record {|
    *Expression;
    readonly IF_CONDITION label = IF_CONDITION;
    TYPE_BOOLEAN 'type = TYPE_BOOLEAN;
    readonly BTYPE typeKind = BTYPE;
    readonly boolean optional = false;
    readonly boolean editable = true;
    readonly IF_CONDITION_DOC documentation = IF_CONDITION_DOC;
    string value;
|};

//// Return Specific Expressions

type ReturnExpression record {|
    readonly RETURN_EXPRESSION label = RETURN_EXPRESSION;
    string 'type;
    readonly BTYPE typeKind = BTYPE;
    readonly boolean optional = false;
    readonly boolean editable = true;
    readonly RETURN_EXPRESSION_DOC documentation = RETURN_EXPRESSION_DOC;
    string value;
|};

//// HTTP GET Specific Expressions

type HttpApiGetClientExpression record {|
    readonly HTTP_API_GET_CLIENT label = HTTP_API_GET_CLIENT;
    readonly HTTP_API_GET_CLIENT_TYPE 'type = HTTP_API_GET_CLIENT_TYPE;
    readonly BTYPE typeKind = BTYPE;
    readonly boolean optional = false;
    readonly boolean editable = true;
    readonly HTTP_API_GET_CLIENT_DOC documentation = HTTP_API_GET_CLIENT_DOC;
    string value;
|};

type HttpApiGetPathExpression record {|
    readonly HTTP_API_GET_PATH label = HTTP_API_GET_PATH;
    readonly TYPE_STRING 'type = TYPE_STRING;
    readonly BTYPE typeKind = BTYPE;
    readonly boolean optional = false;
    readonly boolean editable = true;
    readonly HTTP_API_GET_PATH_DOC documentation = HTTP_API_GET_PATH_DOC;
    string value;
|};

type HttpApiGetHeadersExpression record {|
    readonly HTTP_API_GET_HEADERS label = HTTP_API_GET_HEADERS;
    readonly HTTP_API_GET_HEADERS_TYPE 'type = HTTP_API_GET_HEADERS_TYPE;
    readonly BTYPE typeKind = BTYPE;
    readonly boolean optional = true;
    readonly boolean editable = true;
    readonly HTTP_API_GET_HEADERS_DOC documentation = HTTP_API_GET_HEADERS_DOC;
    string value;
|};

type HttpApiGetTargetTypeExpression record {|
    readonly HTTP_API_GET_TARGET_TYPE label = HTTP_API_GET_TARGET_TYPE;
    readonly BTYPE typeKind = BTYPE;
    HTTP_API_GET_TARGET_TYPE_TYPE 'type = HTTP_API_GET_TARGET_TYPE_TYPE;
    readonly boolean optional = false;
    readonly boolean editable = true;
    readonly HTTP_API_GET_TARGET_TYPE_DOC documentation = HTTP_API_GET_TARGET_TYPE_DOC;
    string value;
|};

//// Common Expressions

type VariableExpression record {|
    readonly VARIABLE_LABEL label = VARIABLE_LABEL;
    readonly BTYPE typeKind = BTYPE;
    readonly boolean optional = false;
    readonly boolean editable = true;
    readonly VARIABLE_DOC documentation = VARIABLE_DOC;
    string value;
    string 'type;
|};

type ExpressionRHS record {|
    readonly EXPRESSION_RHS_LABEL label = EXPRESSION_RHS_LABEL;
    readonly BTYPE typeKind = BTYPE;
    readonly boolean optional = false;
    readonly boolean editable = true;
    readonly EXPRESSION_RHS_DOC documentation = EXPRESSION_RHS_DOC;
    string value;
    string 'type;
|};

const EVENT_HTTP_API_LABEL = "HTTP API";
const EVENT_HTTP_API_METHOD = "Method";
const EVENT_HTTP_API_METHOD_DOC = "HTTP Method";
const EVENT_HTTP_API_PATH = "Path";
const EVENT_HTTP_API_PATH_DOC = "HTTP Path";

const IF_LABEL = "If";
const IF_CONDITION = "Condition";
const IF_CONDITION_DOC = "Boolean Condition";

const RETURN_LABEL = "Return";
const RETURN_EXPRESSION = "Expression";
const RETURN_EXPRESSION_DOC = "Return value";

const HTTP_API_GET_LABEL = "HTTP GET";

const HTTP_API_GET_CLIENT = "Client";
const HTTP_API_GET_CLIENT_TYPE = "http:Client";
const HTTP_API_GET_CLIENT_DOC = "HTTP Client Connection";

const HTTP_API_GET_PATH = "Path";
const HTTP_API_GET_PATH_DOC = "HTTP Path";

const HTTP_API_GET_HEADERS = "Headers";
const HTTP_API_GET_HEADERS_DOC = "HTTP Headers";
const HTTP_API_GET_HEADERS_TYPE = "map<string|string[]>?";

const HTTP_API_GET_TARGET_TYPE = "Target Type";
const HTTP_API_GET_TARGET_TYPE_DOC = "HTTP Response Type";
const HTTP_API_GET_TARGET_TYPE_TYPE = "http:Response|anydata";

const VARIABLE_LABEL = "Variable";
const VARIABLE_DOC = "Result Variable";

const EXPRESSION_LABEL = "Custom Expression";
const EXPRESSION_DOC = "Custom Expression";
const EXPRESSION_RHS_LABEL = "Expression";
const EXPRESSION_RHS_DOC = "Expression";

const TYPE_STRING = "string";
const TYPE_BOOLEAN = "boolean";

const NODE_FLAG_CHECKED = 1 << 0;
const NODE_FLAG_CHECKPANIC = 1 << 1;
const NODE_FLAG_FINAL = 1 << 2;
const NODE_FLAG_REMOTE = 1 << 10;
const NODE_FLAG_RESOURCE = 1 << 11;
