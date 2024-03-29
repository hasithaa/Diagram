import ballerina/io;

public function main() returns error? {

    Diagram diag = {name: "", nodes: []};

    Client cl1 = {
        id: "cl1",
        label: "HTTP Client",
        kind: HTTP,
        lineRange: {fileName: "main.bal", startLine: [2, 1], endLine: [2, 63]},
        value: "asiri"
    };
    Client cl2 = {
        id: "cl2",
        label: "HTTP Client",
        kind: HTTP,
        lineRange: {fileName: "main.bal", startLine: [3, 1], endLine: [3, 66]},
        value: "nawaloka"
    };
    diag.clients.push(cl1);
    diag.clients.push(cl2);

    Node httpApiEventNode = <HttpApiEventNode>{
        id: "1",
        lineRange: {fileName: "main.bal", startLine: [7, 7], endLine: [15, 6]},
        nodeProperties: {
            method: {value: "GET"},
            path: {value: "/search/doctors/[string name]"}
        }
    };
    IfNode ifNode = {
        id: "2",
        lineRange: {fileName: "main.bal", startLine: [8, 9], endLine: [14, 10]},
        branches: [{children: []}, {children: []}],
        nodeProperties: {
            condition: {value: "name == \"kandy\""}
        }
    };
    Node ret1 = <ReturnNode>{
        id: "5",
        lineRange: {fileName: "main.bal", startLine: [10, 13], endLine: [10, 22]},
        nodeProperties: {
            expression: {'type: "json", value: "j"}
        }
    };
    Node ret2 = <ReturnNode>{
        id: "6",
        lineRange: {fileName: "", startLine: [13, 13], endLine: [13, 22]},
        nodeProperties: {
            expression: {'type: "json", value: "j"}
        }
    };
    diag.nodes.push(httpApiEventNode);
    diag.nodes.push(ifNode);
    HttpGetNode get1 = {
        id: "3",
        lineRange: {fileName: "", startLine: [9, 13], endLine: [9, 57]},
        nodeProperties: {
            'client: {value: "asiri"},
            path: {value: "/doctors/kandy"},
            targetType: {value: "json"},
            variable: {'type: "json", value: "j"}
        }
    };
    HttpGetNode get2 = {
        id: "4",
        lineRange: {fileName: "", startLine: [12, 13], endLine: [12, 60]},
        nodeProperties: {
            'client: {value: "nawaloka"},
            path: {value: "/doctors"},
            targetType: {value: "json"},
            variable: {'type: "json", value: "j"}
        }
    };
    ifNode.branches[0].children.push(get1);
    ifNode.branches[1].children.push(get2);
    ifNode.branches[0].children.push(ret1);
    ifNode.branches[1].children.push(ret2);
    check io:fileWriteJson("diagram.json", diag);
}
