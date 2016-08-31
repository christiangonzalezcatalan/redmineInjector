package redmineInjector

import static org.mockserver.integration.ClientAndServer.startClientAndServer
import static org.mockserver.model.HttpRequest.request
import static org.mockserver.model.HttpResponse.response
import static redmineInjector.Mocks.RedmineResponses.listarRegistrosPlan
import static redmineInjector.Mocks.RedmineResponses.listarRegistrosHorasTrabajadas
import static redmineInjector.Mocks.RedmineResponses.obtenerIssue

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
    }
}
