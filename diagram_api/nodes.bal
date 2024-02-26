type Diagram record {
    Node[] nodes;
    string name;
};

type Node record {|
    readonly string id;
    readonly string label?;
    readonly NodeKind kind;
    readonly LineRange lineRange;
    readonly boolean returning?;
    readonly boolean fixed?;
    map<Expression> nodeProperties?;
    NodeList...;
|};

type NodeList record {|
    readonly BLOCK kind = BLOCK;
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

// Events (Starting elements)

type HttpApiEventNode record {|
    *Node;
    readonly EVENT_HTTP_API kind = EVENT_HTTP_API;
    readonly EVENT_HTTP_API_KEY label = EVENT_HTTP_API_KEY;
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
    readonly IF_KEY label = IF_KEY;
    record {|
        IfNodeConditionExpression condition = {value: "true"};
    |} nodeProperties;
    NodeList thenBranch;
    NodeList elseBranch;
    readonly false returning = false;
    readonly false fixed = false;
|};

type ReturnNode record {|
    *Node;
    readonly RETURN kind = RETURN;
    readonly RETURN_KEY label = RETURN_KEY;
    record {|
        ReturnExpression expression = {'type: "()", value: "()"};
    |} nodeProperties;
    readonly true returning = true;
    readonly false fixed = false;
|};

// Library Specific Elements

type HttpGetNode record {|
    *Node;
    readonly HTTP_API_GET_KEY label = HTTP_API_GET_KEY;
    readonly HTTP_API_GET_CALL kind = HTTP_API_GET_CALL;
    record {|
        HttpApiGetClientExpression 'client = {};
        HttpApiGetPathExpression path = {value: "."};
        HttpApiGetHeadersExpression headers?;
        HttpApiGetTargetTypeExpression targetType;
        VariableExpression variable;
    |} nodeProperties;
    readonly false returning = false;
    readonly false fixed = false;
|};

type IfNodeConditionExpression record {|
    *Expression;
    readonly IF_CONDITION key = IF_CONDITION;
    TYPE_BOOLEAN 'type = TYPE_BOOLEAN;
    readonly BTYPE typeKind = BTYPE;
    readonly boolean optional = false;
    readonly boolean editable = true;
    readonly IF_CONDITION_DOC documentation = IF_CONDITION_DOC;
    string value;
|};

type HttpApiEventNodeMethodExpression record {|
    *Expression;
    readonly EVENT_HTTP_API_METHOD key = EVENT_HTTP_API_METHOD;
    () 'type = ();
    readonly IDENTIFIER typeKind = IDENTIFIER;
    readonly boolean optional = false;
    readonly boolean editable = true;
    readonly EVENT_HTTP_API_METHOD_DOC documentation = EVENT_HTTP_API_METHOD_DOC;
|};

type HttpApiEventNodePathExpression record {|
    *Expression;
    readonly EVENT_HTTP_API_PATH key = EVENT_HTTP_API_PATH;
    () 'type = ();
    readonly URI_PATH typeKind = URI_PATH;
    readonly boolean optional = false;
    readonly boolean editable = true;
    readonly EVENT_HTTP_API_PATH_DOC documentation = EVENT_HTTP_API_PATH_DOC;
|};

type ReturnExpression record {|
    *Expression;
    readonly RETURN_EXPRESSION key = RETURN_EXPRESSION;
    readonly BTYPE typeKind = BTYPE;
    readonly boolean optional = false;
    readonly boolean editable = true;
    readonly RETURN_EXPRESSION_DOC documentation = RETURN_EXPRESSION_DOC;
|};

type HttpApiGetClientExpression record {|
    *Expression;
    readonly HTTP_API_GET_CLIENT key = HTTP_API_GET_CLIENT;
    readonly HTTP_API_GET_CLIENT_TYPE 'type = HTTP_API_GET_CLIENT_TYPE;
    readonly BTYPE typeKind = BTYPE;
    readonly boolean optional = false;
    readonly boolean editable = true;
    readonly HTTP_API_GET_CLIENT_DOC documentation = HTTP_API_GET_CLIENT_DOC;
|};

type HttpApiGetPathExpression record {|
    *Expression;
    readonly HTTP_API_GET_PATH key = HTTP_API_GET_PATH;
    readonly TYPE_STRING 'type = TYPE_STRING;
    readonly BTYPE typeKind = BTYPE;
    readonly boolean optional = false;
    readonly boolean editable = true;
    readonly HTTP_API_GET_PATH_DOC documentation = HTTP_API_GET_PATH_DOC;
|};

type HttpApiGetHeadersExpression record {|
    *Expression;
    readonly HTTP_API_GET_HEADERS key = HTTP_API_GET_HEADERS;
    readonly HTTP_API_GET_HEADERS_TYPE 'type = HTTP_API_GET_HEADERS_TYPE;
    readonly BTYPE typeKind = BTYPE;
    readonly boolean optional = true;
    readonly boolean editable = true;
    readonly HTTP_API_GET_HEADERS_DOC documentation = HTTP_API_GET_HEADERS_DOC;
|};

type HttpApiGetTargetTypeExpression record {|
    *Expression;
    readonly HTTP_API_GET_TARGET_TYPE key = HTTP_API_GET_TARGET_TYPE;
    readonly BTYPE typeKind = BTYPE;
    HTTP_API_GET_TARGET_TYPE_TYPE 'type = HTTP_API_GET_TARGET_TYPE_TYPE;
    readonly boolean optional = false;
    readonly boolean editable = true;
    readonly HTTP_API_GET_TARGET_TYPE_DOC documentation = HTTP_API_GET_TARGET_TYPE_DOC;
|};

type VariableExpression record {|
    *Expression;
    readonly VARIABLE_KEY key = VARIABLE_KEY;
    readonly BTYPE typeKind = BTYPE;
    readonly boolean optional = false;
    readonly boolean editable = true;
    readonly VARIABLE_DOC documentation = VARIABLE_DOC;
|};

type Expression record {
    readonly string key;
    string? 'type;
    readonly ExpressionTypeKind typeKind = BTYPE;
    readonly boolean optional?;
    readonly boolean editable?;
    readonly string documentation?;
    string value?;
};

enum ExpressionTypeKind {
    BTYPE,
    IDENTIFIER,
    URI_PATH
}

type ExpressionList record {|
    readonly string key;
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

type HttpGetPropertiesPath record {|
    *Expression;
    HTTP_API_GET_PATH key = HTTP_API_GET_PATH;
    TYPE_STRING 'type = TYPE_STRING;
|};

const EVENT_HTTP_API_KEY = "HTTP API";
const EVENT_HTTP_API_METHOD = "Method";
const EVENT_HTTP_API_METHOD_DOC = "HTTP Method";
const EVENT_HTTP_API_PATH = "Path";
const EVENT_HTTP_API_PATH_DOC = "HTTP Path";

const IF_KEY = "If";
const IF_CONDITION = "Condition";
const IF_CONDITION_DOC = "Boolean Condition";

const RETURN_KEY = "Return";
const RETURN_EXPRESSION = "Expression";
const RETURN_EXPRESSION_DOC = "Return value";

const HTTP_API_GET_KEY = "HTTP GET";

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

const VARIABLE_KEY = "Variable";
const VARIABLE_DOC = "Result Variable";

const TYPE_STRING = "string";
const TYPE_BOOLEAN = "boolean";
