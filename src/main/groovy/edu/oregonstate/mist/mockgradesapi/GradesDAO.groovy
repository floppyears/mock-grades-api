package edu.oregonstate.mist.mockgradesapi

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.core.JsonGenerationException
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.ObjectMapper
import io.dropwizard.lifecycle.Managed
import java.util.concurrent.ConcurrentHashMap

class GradesException extends RuntimeException {
    GradesException(String message, Throwable cause) {
        super(message, cause)
    }
}

class GradesDAO implements Managed {
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
        new TypeReference<ConcurrentHashMap<String, ArrayList<GradesResourceObject>>>() {}
    private random = new Random()
    private ConcurrentHashMap<String,LinkedList<GradesResourceObject>> allGrades

    GradesDAO(String filename) {
        this.file = new File(filename)
    }

    @Override
    public void start() throws Exception {
        this.allGrades = this.readGrades()
    }

    @Override
    public void stop() throws Exception {
        this.writeGrades(this.allGrades)
    }

    /**
     * Look up the grades for a user by their ONID username.
     *
     * @param username the user to look up
     * @return a list of grades, or null if the user doesn't exist
     */
    List<GradesResourceObject> getGradesByUsername(String username) {
        def grades = this.allGrades[username]
        if (grades == null) {
            grades = this.randomGrades()
            this.allGrades[username] = grades
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
        int num = 101 + this.random.nextInt(500 - 101)
        return this.SUBJECTS[index] + num.toString()
    }

    private ConcurrentHashMap<String,LinkedList<GradesResourceObject>> readGrades() {
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

    private writeGrades(ConcurrentHashMap<String,LinkedList<GradesResourceObject>> grades) {
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
