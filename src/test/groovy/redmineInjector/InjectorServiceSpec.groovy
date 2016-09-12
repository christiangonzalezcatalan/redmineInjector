package redmineInjector

import static org.mockserver.integration.ClientAndServer.startClientAndServer
import static org.mockserver.model.HttpRequest.request
import static org.mockserver.model.HttpResponse.response

import static redmineInjector.Mocks.BlackboardResponses.getPlanFromBlackboard
import static redmineInjector.Mocks.BlackboardResponses.getTraceFromBlackboard
import static redmineInjector.Mocks.BlackboardResponses.getPlanMappingsFromBlackboard
import static redmineInjector.Mocks.BlackboardResponses.getTraceMappingsFromBlackboard
import static redmineInjector.Mocks.BlackboardResponses.getMemberByEmailFromBlackboard
import static redmineInjector.Mocks.BlackboardResponses.postPlanToBlackbord

import static redmineInjector.Mocks.RedmineResponses.getIssuesFromRedmine
import static redmineInjector.Mocks.RedmineResponses.getTimeEntriesFromRedmine
import static redmineInjector.Mocks.RedmineResponses.getUserFromRedmine
import static redmineInjector.Mocks.RedmineResponses.listarRegistrosHorasTrabajadas
import static redmineInjector.Mocks.RedmineResponses.getIssueFromRedmine

import grails.test.mixin.TestFor
import org.mockserver.integration.ClientAndServer
import org.mockserver.model.Header
import org.mockserver.model.Parameter
import org.mockserver.verify.VerificationTimes
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
@TestFor(InjectorService)
class InjectorServiceSpec extends Specification {

    protected static ClientAndServer mockServer

    def setupSpec() {
        mockServer = startClientAndServer(8081)
    }

    def cleanupSpec() {
        mockServer.stop()
    }

    def setup() {
    }

    def cleanup() {
        mockServer.reset()
    }

    void 'test inject plan'() {
        setup:
        def projectId = '57cc59368acec62bf2f7d7ed'
        def redmineProjectId = '3'
        def redmineKey = 'baa9da1d47247ea95bedc425027e7bb30df8f883'

        mockServer.when(
                request('/plans')
                        .withMethod('GET')
                        .withQueryStringParameters(new Parameter('projectId', projectId))
        ).respond(response(getPlanFromBlackboard())
                .withStatusCode(200)
                .withHeaders(new Header('Content-Type', 'application/json; charset=utf-8'))
        )

        mockServer.when(
                request("/projects/${projectId}/mappings")
                        .withMethod('GET')
                        .withQueryStringParameters(new Parameter('tool', 'Redmine'))
                        .withQueryStringParameters(new Parameter('entityType', 'Plan'))
        ).respond(response(getPlanMappingsFromBlackboard())
                .withStatusCode(200)
                .withHeaders(new Header('Content-Type', 'application/json; charset=utf-8'))
        )

        mockServer.when(
                request('/issues.json')
                        .withMethod('GET')
                        .withQueryStringParameters(new Parameter('project_id', redmineProjectId))
        ).respond(response(getIssuesFromRedmine())
                .withStatusCode(200)
                .withHeaders(new Header('Content-Type', 'application/json; charset=utf-8'))
        )

        // "${redmineUrl}/users/${redmineUserId}.json?key=baa9da1d47247ea95bedc425027e7bb30df8f883")
        mockServer.when(
                request('/users/3.json')
                        .withMethod('GET')
                        .withQueryStringParameters(new Parameter('key', redmineKey))
        ).respond(response(getUserFromRedmine(3))
                .withStatusCode(200)
                .withHeaders(new Header('Content-Type', 'application/json; charset=utf-8'))
        )
        mockServer.when(
                request('/users/4.json')
                        .withMethod('GET')
                        .withQueryStringParameters(new Parameter('key', redmineKey))
        ).respond(response(getUserFromRedmine(4))
                .withStatusCode(200)
                .withHeaders(new Header('Content-Type', 'application/json; charset=utf-8'))
        )
        // "${gemsbbUrl}/members?email=${result.user.mail}")
        mockServer.when(
                request('/members')
                        .withMethod('GET')
                        .withQueryStringParameters(new Parameter('email', 'christiangonzalezcatalan@hotmail.com'))
        ).respond(response(getMemberByEmailFromBlackboard("57c3c4858acec662dab6dcf4",
                            "christiangonzalezcatalan@hotmail.com",
                            "Christian González"))
                .withStatusCode(200)
                .withHeaders(new Header('Content-Type', 'application/json; charset=utf-8'))
        )
        mockServer.when(
            request('/members')
                    .withMethod('GET')
                    .withQueryStringParameters(new Parameter('email', 'jperez@miempresita.cl'))
        ).respond(response(getMemberByEmailFromBlackboard("57c3c4838acec662dab6dcf2",
                            "jperez@miempresita.cl",
                            "Juan Pérez"))
                .withStatusCode(200)
                .withHeaders(new Header('Content-Type', 'application/json; charset=utf-8'))
        )
        mockServer.when(
            request('/plans/57cf835f8acec65eba3b579f')
            .withMethod('PUT')
        ).respond(response(postPlanToBlackbord())
                .withStatusCode(200)
                .withHeaders(new Header('Content-Type', 'application/json; charset=utf-8'))
        )
        mockServer.when(
                request("/projects/${projectId}/mappings/57d0c86c8acec66d7306700d")
                        .withMethod('PUT')
        ).respond(response(getPlanMappingsFromBlackboard())
                .withStatusCode(200)
                .withHeaders(new Header('Content-Type', 'application/json; charset=utf-8'))
        )

        when:
        service.injectPlan(projectId, redmineProjectId)

        then:
        mockServer.verify(
                request()
                        .withMethod("PUT")
                        .withPath("/plans/57cf835f8acec65eba3b579f")
                ,
                VerificationTimes.exactly(1)
        )
    }

    void 'test inject trace'() {
        setup:
        def projectId = '57cc59368acec62bf2f7d7ed'
        def redmineProjectId = '3'
        def redmineKey = 'baa9da1d47247ea95bedc425027e7bb30df8f883'

        mockServer.when(
                request('/traces')
                        .withMethod('GET')
                        .withQueryStringParameters(new Parameter('projectId', projectId))
        ).respond(response(getTraceFromBlackboard())
                .withStatusCode(200)
                .withHeaders(new Header('Content-Type', 'application/json; charset=utf-8'))
        )

        mockServer.when(
                request("/projects/${projectId}/mappings")
                        .withMethod('GET')
                        .withQueryStringParameters(new Parameter('tool', 'Redmine'))
                        .withQueryStringParameters(new Parameter('entityType', 'Trace'))
        ).respond(response(getTraceMappingsFromBlackboard())
                .withStatusCode(200)
                .withHeaders(new Header('Content-Type', 'application/json; charset=utf-8'))
        )

        mockServer.when(
                request('/time_entries.json')
                        .withMethod('GET')
                        .withQueryStringParameters(new Parameter('project_id', redmineProjectId))
        ).respond(response(getTimeEntriesFromRedmine())
                .withStatusCode(200)
                .withHeaders(new Header('Content-Type', 'application/json; charset=utf-8'))
        )
        mockServer.when(
                request('/issues/9.json')
                        .withMethod('GET')
        ).respond(response(getIssueFromRedmine(9, 'Carga de un plan de redmine en blackboard.'))
                .withStatusCode(200)
                .withHeaders(new Header('Content-Type', 'application/json; charset=utf-8'))
        )
        mockServer.when(
                request('/issues/11.json')
                        .withMethod('GET')
        ).respond(response(getIssueFromRedmine(9, 'Revisión de código.'))
                .withStatusCode(200)
                .withHeaders(new Header('Content-Type', 'application/json; charset=utf-8'))
        )
        mockServer.when(
                request('/users/3.json')
                        .withMethod('GET')
                        .withQueryStringParameters(new Parameter('key', redmineKey))
        ).respond(response(getUserFromRedmine(3))
                .withStatusCode(200)
                .withHeaders(new Header('Content-Type', 'application/json; charset=utf-8'))
        )
        mockServer.when(
                request('/users/4.json')
                        .withMethod('GET')
                        .withQueryStringParameters(new Parameter('key', redmineKey))
        ).respond(response(getUserFromRedmine(4))
                .withStatusCode(200)
                .withHeaders(new Header('Content-Type', 'application/json; charset=utf-8'))
        )
        mockServer.when(
                request('/members')
                        .withMethod('GET')
                        .withQueryStringParameters(new Parameter('email', 'christiangonzalezcatalan@hotmail.com'))
        ).respond(response(getMemberByEmailFromBlackboard("57c3c4858acec662dab6dcf4",
                            "christiangonzalezcatalan@hotmail.com",
                            "Christian González"))
                .withStatusCode(200)
                .withHeaders(new Header('Content-Type', 'application/json; charset=utf-8'))
        )
        mockServer.when(
            request('/members')
                    .withMethod('GET')
                    .withQueryStringParameters(new Parameter('email', 'jperez@miempresita.cl'))
        ).respond(response(getMemberByEmailFromBlackboard("57c3c4838acec662dab6dcf2",
                            "jperez@miempresita.cl",
                            "Juan Pérez"))
                .withStatusCode(200)
                .withHeaders(new Header('Content-Type', 'application/json; charset=utf-8'))
        )
        mockServer.when(
            request('/traces/57d5f5e48acec62fb22f8a73')
            .withMethod('PUT')
        ).respond(response(getTraceFromBlackboard())
                .withStatusCode(200)
                .withHeaders(new Header('Content-Type', 'application/json; charset=utf-8'))
        )
        mockServer.when(
                request("/projects/${projectId}/mappings/57d5f5e88acec62fb22f8a74")
                        .withMethod('PUT')
        ).respond(response(getTraceMappingsFromBlackboard())
                .withStatusCode(200)
                .withHeaders(new Header('Content-Type', 'application/json; charset=utf-8'))
        )

        when:
        service.injectProjectTrace(projectId, redmineProjectId)

        then:
        true
    }

    /*
    void "test inject project plan"() {
        setup:
        mockServer.when(
                request('/issues.json')
                        .withMethod('GET')
                        .withQueryStringParameters(new Parameter('project_id', '3'))
        ).respond(response(listarRegistrosPlan())
                .withStatusCode(200)
                .withHeaders(new Header('Content-Type', 'application/json; charset=utf-8'))
        )

        mockServer.when(
                request('/projects/search')
                        .withMethod('GET')
                        .withQueryStringParameters([new Parameter('externalKey', '3'),
                                                    new Parameter('tool', 'Redmine')])
        ).respond(
                response('[{"id": "abcde"}]')
                        .withStatusCode(200)
                        .withHeaders(new Header('Content-Type', 'application/json; charset=utf-8'))
        )

        mockServer.when(
                request('/plans/search')
                        .withMethod('GET')
                        .withQueryStringParameters([new Parameter('externalKey', '3'),
                                                    new Parameter('tool', 'Redmine')])
        ).respond(
                response('[]')
                        .withStatusCode(200)
                        .withHeaders(new Header('Content-Type', 'application/json; charset=utf-8'))
        )

        mockServer.when(
                request('/members/search')
                        .withMethod('GET')
                        .withQueryStringParameters([new Parameter('externalKey', '4'),
                                                    new Parameter('tool', 'Redmine')])
        ).respond(
                response('[{"id": "abcde"}]')
                        .withStatusCode(200)
                        .withHeaders(new Header('Content-Type', 'application/json; charset=utf-8'))
        )

        mockServer.when(
                request('/plans')
                        .withMethod('POST')
        ).respond(response('')
                .withStatusCode(200)
                .withHeaders(new Header('Content-Type', 'application/json; charset=utf-8'))
        )

        when:
        service.injectProjectPlan(3)

        then:
        mockServer.verify(
                request()
                        .withMethod("POST")
                        .withPath("/plans")
                //.withBody("{username: 'foo', password: 'bar'}")
                ,
                VerificationTimes.exactly(1)
        );
    }

    void "test inject project trace"() {
        setup:
        mockServer.when(
                request('/time_entries.json')
                        .withMethod('GET')
                        .withQueryStringParameters(new Parameter('project_id', '3'))
        ).respond(response(listarRegistrosHorasTrabajadas())
                .withStatusCode(200)
                .withHeaders(new Header('Content-Type', 'application/json; charset=utf-8'))
        )

        mockServer.when(
                request('/projects/search')
                        .withMethod('GET')
                        .withQueryStringParameters([new Parameter('externalKey', '3'),
                                                    new Parameter('tool', 'Redmine')])
        ).respond(
                response('[{"id": "abcde"}]')
                        .withStatusCode(200)
                        .withHeaders(new Header('Content-Type', 'application/json; charset=utf-8'))
        )

        mockServer.when(
                request('/issues/9.json')
                        .withMethod('GET')
        ).respond(response(obtenerIssue())
                .withStatusCode(200)
                .withHeaders(new Header('Content-Type', 'application/json; charset=utf-8'))
        )

        mockServer.when(
                request('/traces/search')
                        .withMethod('GET')
                        .withQueryStringParameters([new Parameter('externalKey', '3'),
                                                    new Parameter('tool', 'Redmine')])
        ).respond(
                response('[]')
                        .withStatusCode(200)
                        .withHeaders(new Header('Content-Type', 'application/json; charset=utf-8'))
        )

        mockServer.when(
                request('/members/search')
                        .withMethod('GET')
                        .withQueryStringParameters([new Parameter('externalKey', '3'),
                                                    new Parameter('tool', 'Redmine')])
        ).respond(
                response('[{"id": "abcde"}]')
                        .withStatusCode(200)
                        .withHeaders(new Header('Content-Type', 'application/json; charset=utf-8'))
        )

        mockServer.when(
                request('/traces')
                        .withMethod('POST')
        ).respond(response('')
                .withStatusCode(200)
                .withHeaders(new Header('Content-Type', 'application/json; charset=utf-8'))
        )

        when:
        service.injectProjectTrace(3)

        then:
        true
        mockServer.verify(
                request()
                        .withMethod("POST")
                        .withPath("/traces")
                ,
                VerificationTimes.exactly(1)
        );
    }*/
}
