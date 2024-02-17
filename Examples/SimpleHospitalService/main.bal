import ballerina/http;
import ballerina/xmldata;

import hasitha/diagram as _;

function transformMapleRidge(MapleRidgeDoctor doctor) returns DoctorDetails => {
    name: doctor.doctorName,
    mobile: doctor.contactNumber,
    room: doctor.wardNumber.toString(),
    specialization: "ENT"
};

function transformPineValley(PineValleyDoctor doctor) returns DoctorDetails => {
    name: doctor.name,
    mobile: doctor.mobile,
    room: doctor.room.toString(),
    specialization: doctor.department
};


final http:Client pineValleyEp = check new ("http://localhost:9091/pineValley/");
final http:Client mapleRidgeEp = check new ("http://localhost:9093/mapleRidge/");

service /search on new http:Listener(9090) {
    resource function get doctor/[string doctorType]() returns DoctorDetails|error {
        if doctorType == "ENT" {
            // Call Maple Ridge
            Payload payload = {data: {doctorType: "ENT"}};
            json aResponse = check mapleRidgeEp->/doctor/[doctorType].post(payload);
            MapleRidgeDoctor aMapleRidgeDoctor = check aResponse.cloneWithType();
            DoctorDetails aDoctorDetail = transformMapleRidge(aMapleRidgeDoctor);
            return aDoctorDetail;
        } else {
            xml data = xml `<data>
    <doctorType>any</doctorType>
</data>`;
            xml aResponse = check pineValleyEp->post("/doctor/" + doctorType, data);
            json aJson = check xmldata:toJson(aResponse);
            PineValleyDoctor aPineValleyDoctor = check aJson.cloneWithType();
            return transformPineValley(aPineValleyDoctor);
        }
    }
}

type Payload record {
    record {
        string doctorType;
    } data;
};

type MapleRidgeDoctor record {
    string doctorName;
    string contactNumber;
    int wardNumber;
};

type PineValleyDoctor record {
    string name;
    string mobile;
    int room;
    string department;
};

type DoctorDetails record {
    string name;
    string mobile;
    string room;
    string specialization;
};