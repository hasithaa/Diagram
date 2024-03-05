const FLOW_MODEL_SERVICE = "flowDesignService";

type Api record {|
    string api;
    map<anydata> request;
    map<anydata> response;
|};

// API implementations

const GET_FLOW_DESIGN_API = "getFlowDesignModel";

type GetFlowModelApi record {|
    *Api;
    GET_FLOW_DESIGN_API api = GET_FLOW_DESIGN_API;
    record {|
        string filePath;
        LinePosition startLine;
        LinePosition endLine;
    |} request;
    record {|
        Diagram flowDesignModel;
    |} response;
|};

const GET_SOURCE_API = "getSourceCode";

type GetSourceCodeApi record {|
    *Api;
    GET_SOURCE_API api = "getSourceCode";
    record {|
        Node diagramNode;
    |} request;
    record {|
        TextEdit[] textEdits;
    |} response;
|};

// Common data types

type TextEdit record {|
    Range range;
    string newText;
|};

type Range record {|
    Position 'start;
    Position end;
|};

type Position record {|
    int line;
    int character;
|};
