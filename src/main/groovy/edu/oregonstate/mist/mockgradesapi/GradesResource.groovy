package edu.oregonstate.mist.mockgradesapi

import com.codahale.metrics.annotation.Timed
import edu.oregonstate.mist.api.AuthenticatedUser
import edu.oregonstate.mist.api.Error
import edu.oregonstate.mist.api.Resource
import edu.oregonstate.mist.api.jsonapi.ResultObject
import io.dropwizard.auth.Auth
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import javax.ws.rs.GET
import javax.ws.rs.HeaderParam
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

@Path("/grades")
@Produces(MediaType.APPLICATION_JSON)
@groovy.transform.CompileStatic
class GradesResource extends Resource {
    private static final Logger LOGGER = LoggerFactory.getLogger(GradesResource.class)
    private GradesDAO gradesDAO

    GradesResource(URI endpointUri, GradesDAO gradesDAO) {
        this.endpointUri = endpointUri
        this.gradesDAO = gradesDAO
    }

    @GET
    @Timed
    Response grades(
        @Auth AuthenticatedUser _,
        @HeaderParam('x-username') String username
    ) {
        if (!username) {
            return Response.status(Response.Status.FORBIDDEN)
                .entity(new Error(
                    status: Response.Status.FORBIDDEN.statusCode,
                    developerMessage: "not logged in",
                    userMessage: "",
                    code: 0,
                    details: ""
                ))
                .build()
        }

        def grades
        try {
            grades = this.gradesDAO.getGradesByUsername(username)
        } catch (GradesException e) {
            LOGGER.error("$e", e)
            return this.internalServerError("$e").build()
        }

        if (grades == null) {
            grades = []
        }

        def res = new ResultObject(
            links: [ self: this.endpointUri ],
            data: grades
        )

        return this.ok(res).build()
    }
}
