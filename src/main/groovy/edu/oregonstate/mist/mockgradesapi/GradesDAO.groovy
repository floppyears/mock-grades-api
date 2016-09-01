package edu.oregonstate.mist.mockgradesapi

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.core.JsonGenerationException
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.ObjectMapper

class GradesException extends RuntimeException {
    GradesException(String message, Throwable cause) {
        super(message, cause)
    }
}

class GradesDAO {
    private static final GRADES = [
        "A", "A-",
        "B+", "B", "B-",
        "C+", "C", "C-",
        "D+", "D", "D-",
        "F"
    ]
    private static final SUBJECTS = [
        "CS", "MTH", "BI", "OC", "GEO", "WR", "PH",
    ]

    private File file
    private ObjectMapper mapper = new ObjectMapper()
    private TypeReference typeRef =
        new TypeReference<HashMap<String, ArrayList<GradesResourceObject>>>() {}
    private random = new Random()

    GradesDAO(String filename) {
        this.file = new File(filename)
    }

    /**
     * Look up the grades for a user by their ONID username.
     *
     * @param username the user to look up
     * @return a list of grades, or null if the user doesn't exist
     */
    List<GradesResourceObject> getGradesByUsername(String username) {
        def allGrades = this.readGrades()
        def grades = allGrades[username]
        if (grades == null) {
            grades = this.randomGrades()
            allGrades[username] = grades
            this.saveGrades(allGrades)
        }
        return grades
    }

    private List<GradesResourceObject> randomGrades() {
        def grade = new GradesResourceObject()
        grade.id = this.random.nextInt(1000).toString()
        grade.type = "grades"
        grade.attributes = new GradeAttributes()
        grade.attributes.courseNumber = this.randomCourse()
        grade.attributes.grade = this.randomGrade()
        return [grade]
    }

    private String randomGrade() {
        int index = this.random.nextInt(this.GRADES.size())
        return this.GRADES[index]
    }

    private String randomCourse() {
        int index = this.random.nextInt(this.SUBJECTS.size())
        int num = 100+this.random.nextInt(500-101)
        return this.SUBJECTS[index] + num.toString()
    }

    private HashMap<String,LinkedList<GradesResourceObject>> readGrades() {
        def grades
        try {
            grades = this.mapper.readValue(this.file, this.typeRef)
        } catch (IOException e) {
            throw new GradesException("error reading grades.json: $e", e)
        } catch (JsonParseException e) {
            throw new GradesException("error parsing grades.json: $e", e)
        } catch (JsonMappingException e) {
            throw new GradesException("error mapping grades.json: $e", e)
        }

        return grades
    }

    private saveGrades(HashMap<String,LinkedList<GradesResourceObject>> grades) {
        try {
            this.mapper.writeValue(this.file, grades)
        } catch (IOException e) {
            throw new GradesException("error reading grades.json: $e", e)
        } catch (JsonParseException e) {
            throw new GradesException("error parsing grades.json: $e", e)
        } catch (JsonMappingException e) {
            throw new GradesException("error mapping grades.json: $e", e)
        }
    }
}
