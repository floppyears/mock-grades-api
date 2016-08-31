package edu.oregonstate.mist.mockgradesapi

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.ObjectMapper

class GradesException extends RuntimeException {
    GradesException(String message, Throwable cause) {
        super(message, cause)
    }
}

class GradesDAO {
    private String filename
    private ObjectMapper mapper = new ObjectMapper()
    private TypeReference typeRef =
        new TypeReference<HashMap<String, ArrayList<GradesResourceObject>>>() {}

    GradesDAO(String filename) {
        this.filename = filename
    }

    /**
     * Look up the grades for a user by their ONID username.
     *
     * @param username the user to look up
     * @return a list of grades, or null if the user doesn't exist
     */
    List<GradesResourceObject> getGradesByUsername(String username) {
        def allGrades = readGrades()
        return allGrades[username]
    }

    private HashMap<String,LinkedList<GradesResourceObject>> readGrades() {
        def f
        try {
            f = new File(this.filename)
        } catch (IOException e) {
            throw new GradesException("error opening grades.json: $e", e)
        }

        def grades
        try {
            grades = mapper.readValue(f, typeRef)
        } catch (IOException e) {
            throw new GradesException("error reading grades.json: $e", e)
        } catch (JsonParseException e) {
            throw new GradesException("error parsing grades.json: $e", e)
        } catch (JsonMappingException e) {
            throw new GradesException("error mapping grades.json: $e", e)
        }

        return grades
    }
}
