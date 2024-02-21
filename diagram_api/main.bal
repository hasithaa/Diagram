// import ballerina/http;
import ballerina/io;

// final http:Client asiri = check new ("http://localhost:9090");
// final http:Client nawaloka = check new ("http://localhost:9091");

// service on new http:Listener(9090) {
//     resource function get search/doctors/[string area]() returns json|error {
//         if area == "kandy" {
//             json j = check asiri->get("/doctors/kandy");
//             return j;
//         } else {
//             json j = check nawaloka->get("/doctors");
//             return j;
//         }
//     }
// }

type Diagram record {
    Node[] nodes;
    string name;
};

public function main() returns error? {

    Diagram diag = {name: "", nodes: []};
    HttpApiEventNode httpApiEventNode = {
        id: "1",
        lineRange: {fileName: "", startLine: [7, 7], endLine: [15, 6]},
        method: {key: "method", typeKind: IDENTIFIER, value: "GET", 'type: ()},
        path: {key: "path", typeKind: URI_PATH, value: "/search/doctors/[string name]", 'type: ()},
        fixed: true
    };
    diag.nodes.push(httpApiEventNode);
    IfNode ifNode = {
        id: "2",
        lineRange: {fileName: "", startLine: [8, 9], endLine: [14, 10]},
        condition: {key: "condition", 'type: "boolean", value: "name == \"kandy\""},
        thenBranch: [],
        elseBranch: []
    };
    diag.nodes.push(ifNode);
    HttpGetNode get1 = {
        id: "3",
        lineRange: {fileName: "", startLine: [9, 13], endLine: [9, 57]},
        'client: {key: "client", value: "asiri", 'type: "http:Client"},
        path: {key: "path", 'type: "string", value: "/doctors/kandy"},
        headers: {'key: "headers", 'type: "map<string|string[]>?", optional: true},
        targetType: {'key: "targetType", 'type: "Response|anydata", value: "json"},
        params: {'key: "params", 'type: "http:QueryParamType", value: [], optional: true},
        variable: {'key: "j", 'type: "json"}
    };
    HttpGetNode get2 = {
        id: "4",
        lineRange: {fileName: "", startLine: [12, 13], endLine: [12, 60]},
        'client: {key: "client", value: "nawaloka", 'type: "http:Client"},
        path: {key: "path", 'type: "string", value: "/doctors"},
        headers: {'key: "headers", 'type: "map<string|string[]>?", optional: true},
        targetType: {'key: "targetType", 'type: "Response|anydata", value: "json"},
        params: {'key: "params", 'type: "http:QueryParamType", value: [], optional: true},
        variable: {'key: "j", 'type: "json"}
    };
    ifNode.thenBranch.push(get1);
    ifNode.elseBranch.push(get2);
    ReturnNode ret1 = {
        id: "5",
        lineRange: {fileName: "", startLine: [10, 13], endLine: [10, 22]},
        expr: {key: "expression", 'type: "json", value: "j"}
    };
    ReturnNode ret2 = {
        id: "6",
        lineRange: {fileName: "", startLine: [13, 13], endLine: [13, 22]},
        expr: {key: "expression", 'type: "json", value: "j"}
    };
    ifNode.thenBranch.push(ret1);
    ifNode.elseBranch.push(ret2);
    check io:fileWriteJson("diagram.json", diag.toJson());
}
