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

const GET_AVAILABLE_NODES_API = "getAvailableNodes";

type GetAvailableNodesApi record {|
    *Api;
    GET_AVAILABLE_NODES_API api = GET_AVAILABLE_NODES_API;
    record {|
        LineRange parentNodeLineRange;
        NodeKind parentNodeKind;
        string? branchLabel;
    |} request;
    record {|
        NodeKind[] availableNodes;
    |} response;
|};

const GET_NODE_TEMPLATE_API = "getNodeTemplate";

type RecordName record {|
    *Api;
    GET_NODE_TEMPLATE_API api = GET_NODE_TEMPLATE_API;
    record {|
        NodeKind nodeKind;
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
