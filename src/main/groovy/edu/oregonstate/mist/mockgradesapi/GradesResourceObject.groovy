package edu.oregonstate.mist.mockgradesapi

class GradesResourceObject {
    String id
    String type
    GradeAttributes attributes
    Map<String,String> links
}

class GradeAttributes {
    String courseNumber
    String grade
}
