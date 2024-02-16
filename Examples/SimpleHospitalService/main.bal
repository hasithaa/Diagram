import ballerina/http;

import hasitha/diagram as _;

final http:Client pineValleyEp = check new ("http://localhost:9091/pineValley/");
final http:Client mapleRidgeEp = check new ("http://localhost:9093/mapleRidge/");

service / on new http:Listener(9090) {
    resource function get doctor/[string doctorType]() returns json|error {
        if doctorType == "ENT" {
            // Call Maple Ridge
            Result res = check mapleRidgeEp->get("/doctor/" + doctorType);
            return res;
        } else {
            Result res = check pineValleyEp->get("/doctor/" + doctorType);
            return res;
        }
    }
}

type DoctorType record {|
    string name;
    string doctorType;
|};

type Result DoctorType[];
