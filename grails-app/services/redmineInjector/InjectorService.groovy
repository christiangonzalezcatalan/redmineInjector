package redmineInjector

import grails.plugins.rest.client.RestBuilder
import grails.transaction.Transactional
import org.grails.web.json.JSONObject
import org.springframework.http.HttpStatus
import grails.util.Holders

@Transactional
class InjectorService {
    RestBuilder restClient = new RestBuilder()
    String redmineUrl = Holders.grailsApplication.config.getProperty('injector.redmineUrl')
    String gemsbbUrl = Holders.grailsApplication.config.getProperty('injector.gemsbbUrl')

    private String getProjectId(Integer id, String name) {
        def resp = restClient.get("${gemsbbUrl}/projects/search?externalKey=${id}&tool=Redmine")
        JSONObject result = resp.json

        if(result.size() == 1) {
            return result.id
        }
        else {
            def rpost = restClient.post('${gemsbbUrl}/projects') {
                contentType "application/json"
                json {
                    name = name
                }
            }
            return rpost.json.id
        }
    }

    private String getPlanId(Integer id) {
        def resp = restClient.get("${gemsbbUrl}/plans/search?externalKey=${id}&tool=Redmine")
        JSONObject result = resp.json

        if(result.size() == 1) {
            return result.id
        }
    }

    private String getMemberId(Integer id) {
        def resp = restClient.get("${gemsbbUrl}/members/search?externalKey=${id}&tool=Redmine")
        JSONObject result = resp.json

        if(result.size() == 1) {
            return result.id
        }
        else {
            def apiKey = 'baa9da1d47247ea95bedc425027e7bb30df8f883'
            def user = restClient.get("${redmineUrl}/users.json?project_id=3&key=${apiKey}").json.user
            def rpost = restClient.post('${gemsbbUrl}/members') {
                contentType "application/json"
                json {
                    name = "${user.firstname} ${user.lastname}"
                    email = user.mail
                }
            }

            return rpost.json.id
        }
    }

    private def getTask(JSONObject issue) {
        def responsible = null
        if(issue.assigned_to != null) {
            getMemberId(issue.assigned_to.id.toInteger())
        }

        [
                name: "Task 1",
                startDate: Date.parse('yyyy-MM-dd', issue.start_date),
                dueDate: Date.parse('yyyy-MM-dd', issue.due_date),
                status: issue.status.name,
                responsible: responsible,
                contributors: ["57b135d78acec62754906455"]
        ]
    }


    def injectProjectPlan(Integer externalProjectId) {
        // 1. Obtener proyecto. Si no está en bb, crear. (esto debiera ser en otro método)
        //  1.1 Obtener por id.
        // 2. Para cada tarea del plan:
        //  2.1. Obtener responsable. Si no existe en bb, crear.
        //  2.2. Agregar issues a la lista de tareas.

        //def resp = restClient.get("http://10.0.2.2:3000/issues.json?project_id=3")
        def resp = restClient.get("${redmineUrl}/issues.json?project_id=${externalProjectId}")
        JSONObject result = resp.json
        if(result.issues.size() > 0) {
            def firstIssue = result.issues[0]
            println firstIssue.project.name
            println "${firstIssue.status.name} ${firstIssue.tracker.name}. ${firstIssue.subject}: ${firstIssue.description}"
            println firstIssue

            def projectId = getProjectId(firstIssue.project.id, firstIssue.project.name)

            def taskList = []

            result.issues.each {
                taskList.add(getTask(it))
            }

            def planId = getPlanId(externalProjectId)
            def responsePlan
            println 'planid: ' + planId
            if(planId == null) {
                responsePlan = restClient.post("${gemsbbUrl}/plans") {
                    contentType "application/json"
                    json {
                        externalKey = externalProjectId
                        tool = 'Redmine'
                        project = projectId
                        tasks = taskList
                    }
                }
            }
            else {
                responsePlan = restClient.put("${gemsbbUrl}/plans") {
                    contentType "application/json"
                    json {
                        id = planId
                        externalKey = externalProjectId
                        tool = 'Redmine'
                        project = projectId
                        tasks = taskList
                    }
                }
            }

            if (responsePlan.getStatusCode() != HttpStatus.OK) {
                throw new Exception("Error al guardar el registro del plan. HttpStatusCode: ${responsePlan.getStatusCode()}")
            }
        }
    }
}
