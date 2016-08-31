package edu.oregonstate.mist.mockgradesapi

import com.codahale.metrics.annotation.Timed
import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.ObjectMapper
import edu.oregonstate.mist.api.AuthenticatedUser
import edu.oregonstate.mist.api.Resource
import edu.oregonstate.mist.api.jsonapi.ResultObject
import io.dropwizard.auth.Auth
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

class GradeResourceObject {
    String id
    String type
    GradeAttributes attributes
    Map<String,String> links
}

class GradeAttributes {
    String courseNumber
    String grade
}

@Path("/grades")
@Produces(MediaType.APPLICATION_JSON)
class GradesResource extends Resource {
    private static final Logger LOGGER = LoggerFactory.getLogger(GradesResource.class)
    private String gradesJsonPath

    GradesResource(URI endpointUri, String gradesJsonPath) {
        this.endpointUri = endpointUri
        this.gradesJsonPath = gradesJsonPath
    }

    @GET
    @Timed
    Response grades(@Auth AuthenticatedUser _) {
        def f

        try {
            f = new File(this.gradesJsonPath)
        } catch (IOException e) {
            return this.internalServerError("error opening grades.json $e").build()
        }

        def mapper = new ObjectMapper()
        def typeRef = new TypeReference<HashMap<String, ArrayList<GradeResourceObject>>>() {}
        def grades

        try {
            grades = mapper.readValue(f, typeRef)
        } catch (IOException e) {
            return this.internalServerError("error reading grades.json: $e").build()
        } catch (JsonParseException e) {
            return this.internalServerError("error parsing grades.json: $e").build()
        } catch (JsonMappingException e) {
            return this.internalServerError("error mapping grades.json: $e").build()
        }
        
        def user = "ekstedta"

        def myGrades = grades[user]
        if( myGrades == null) {
            return this.notFound().build() // XXX 401?
        }

        def res = new ResultObject(
            links: [
                [
                    self: this.endpointUri
                ]
            ],
            data: myGrades
        )

        return this.ok(res).build()
    }
}
