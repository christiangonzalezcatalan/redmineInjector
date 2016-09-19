package redmineInjector

import grails.plugins.rest.client.RestBuilder
import grails.transaction.Transactional
import org.grails.web.json.JSONObject
import org.springframework.http.HttpStatus
import grails.util.Holders
import org.bson.types.ObjectId

@Transactional
class InjectorService {
    private static String toolName = 'Redmine'
    RestBuilder restClient = new RestBuilder()
    String redmineUrl = Holders.grailsApplication.config.getProperty('injector.redmineUrl')
    String gemsbbUrl = Holders.grailsApplication.config.getProperty('injector.gemsbbUrl')

    private def buildTask(JSONObject issue) {
        def responsibleId = null
        if(issue.assigned_to != null) {
            responsibleId = getMemberByEmail(issue.assigned_to.id.toInteger()).id
        }

        [
            name: issue.subject,
            startDate: Date.parse('yyyy-MM-dd', issue.start_date),
            dueDate: Date.parse('yyyy-MM-dd', issue.due_date),
            status: issue.status.name,
            responsible: [id: responsibleId],
            contributors: []
        ]
    }

    private def getPlanFromBB(String projectId) {
        def resp = restClient.get("${gemsbbUrl}/plans?projectId=${projectId}")

        if(resp.getStatusCode() != HttpStatus.OK) {
            throw new Exception("Error al obtener el registro del plan del Blackboard. HttpStatusCode: ${resp.getStatusCode()}")
        }

        JSONObject result = resp.json

        if(result.size() == 1 || result.id != null) {
            return result
        }
    }

    private def getTaskFromMap(id, map){
        def taskId = map.findAll() { key, value -> value == id }
                        .collect() { key, value -> key }[0]

        if(taskId == null) {
            taskId = new ObjectId().toString()
            map[taskId] = id
        }
        [taskId: taskId]
    }

    private def getMappingFromBB(projectId, tool, entityType) {
        def resp = restClient.get(
            "${gemsbbUrl}/projects/${projectId}/mappings?tool=${tool}&entityType=${entityType}")

        if(resp.getStatusCode() != HttpStatus.OK) {
            throw new Exception("Error al obtener el mapping del plan. HttpStatusCode: ${resp.getStatusCode()}")
        }

        JSONObject result = resp.json

        if(result.size() == 1 || result.id != null) {
            return result
        }
    }

    private def getMemberByEmail(redmineUserId) {
        def resp = restClient.get(
            "${redmineUrl}/users/${redmineUserId}.json?key=baa9da1d47247ea95bedc425027e7bb30df8f883")

        if(resp.getStatusCode() != HttpStatus.OK) {
            throw new Exception("Error al obtener el usuario de Redmine. HttpStatusCode: ${resp.getStatusCode()}")
        }

        JSONObject result = resp.json

        if(result.user != null) {
            def memberResp = restClient.get(
                "${gemsbbUrl}/members?email=${result.user.mail}")
            memberResp.json
        }
    }

    private def getMapping(projectId, plan, tool, entityType) {
        def mapping
        if(plan.id != null) {
            mapping = getMappingFromBB(projectId, tool, entityType)
        }
        if(mapping == null) {
            mapping = [
                project: [
                    id: projectId
                ],
                tool: tool,
                entityType: entityType,
                map: new LinkedHashMap()
            ]
        }
        mapping
    }

    private def getIssuesFromRedmine(redmineProjectId) {
        def resp = restClient.get("${redmineUrl}/issues.json?project_id=${redmineProjectId}")

        if(resp.getStatusCode() != HttpStatus.OK) {
            throw new Exception("Error al obtener los issues de Redmine. HttpStatusCode: ${resp.getStatusCode()}")
        }
        resp.json
    }

    private def saveBlackboardPlan(plan, projectId, taskList) {
        def responsePlan
        if(plan.id == null) {
            responsePlan = restClient.post("${gemsbbUrl}/plans") {
                contentType "application/json"
                json {
                    project = [id: projectId]
                    tasks = taskList
                }
            }
        }
        else {
            responsePlan = restClient.put("${gemsbbUrl}/plans/${plan.id}") {
                contentType "application/json"
                json {
                    id = plan.id
                    project = [id: projectId]
                    tasks = taskList
                }
            }
        }
        if (responsePlan.getStatusCode() != HttpStatus.OK &&
            responsePlan.getStatusCode() != HttpStatus.CREATED) {
            throw new Exception("Error al guardar el registro del plan. HttpStatusCode: ${responsePlan.getStatusCode()}")
        }

        responsePlan.json
    }

    private def saveBlackboardMapping(mapping, projectId, bbObject) {
        def responseMapping

        if(mapping.id == null) {
            responseMapping = restClient.post("${gemsbbUrl}/projects/${projectId}/mappings") {
                contentType "application/json"
                json {
                    project = mapping.project
                    tool = mapping.tool
                    entityType = mapping.entityType
                    //externalId = bbObject.id
                    map = mapping.map
                }
            }
        }
        else {
            responseMapping = restClient.put("${gemsbbUrl}/projects/${projectId}/mappings/${mapping.id}") {
                contentType "application/json"
                json {
                    id = mapping.id
                    project = mapping.project
                    tool = mapping.tool
                    entityType = mapping.entityType
                    //externalId = bbObject.id
                    map = mapping.map
                }
            }
        }

        if (responseMapping.getStatusCode() != HttpStatus.OK &&
            responseMapping.getStatusCode() != HttpStatus.CREATED) {
            throw new Exception("Error al guardar el mapping del plan. HttpStatusCode: ${responseMapping.getStatusCode()}")
        }

        responseMapping.json
    }

    /*
    0. Get proyecto?
    1- Get plan
    2- Get mapping
    3. Get issues
    4. Post/Put plan
    5. Post/Put mapping
    */
    def injectPlan(String projectId, String externalProjectId) {
        def plan = getPlanFromBB(projectId)
        if(plan == null) {
            plan = [
                project: [
                    id: projectId
                ],
                tasks: new LinkedHashMap()
            ]
        }

        def mapping = getMapping(projectId, plan, 'Redmine', 'Plan')
        def redmineIssues = getIssuesFromRedmine(externalProjectId)

        if(redmineIssues.issues.size() > 0) {
            def taskList = []

            redmineIssues.issues.each {
                def issue = it
                def responsibleId = null
                if(issue.assigned_to != null) {
                    responsibleId = getMemberByEmail(issue.assigned_to.id.toInteger()).id
                }

                def task = getTaskFromMap(issue.id, mapping.map)
                task << [
                    name: issue.subject,
                    startDate: Date.parse('yyyy-MM-dd', issue.start_date),
                    dueDate: Date.parse('yyyy-MM-dd', issue.due_date),
                    status: issue.status.name,
                    responsible: [id: responsibleId],
                    contributors: []
                ]
                taskList.add(task)
            }

            def bbPlan = saveBlackboardPlan(plan, projectId, taskList)
            def bbMapping = saveBlackboardMapping(mapping, projectId, bbPlan)
        }
    }

    // ********************** Trace ******************************************
    private def getTraceFromBB(String projectId) {
        def resp = restClient.get("${gemsbbUrl}/traces?projectId=${projectId}")

        if(resp.getStatusCode() != HttpStatus.OK) {
            throw new Exception("Error al obtener el registro de traza del Blackboard. HttpStatusCode: ${resp.getStatusCode()}")
        }

        JSONObject result = resp.json

        if(result.size() == 1 || result.id != null) {
            return result
        }
    }

    private def getTaskTraceFromMap(id, map) {
        def taskTraceId = map.findAll() { key, value -> value == id }
                        .collect() { key, value -> key }[0]

        if(taskTraceId == null) {
            taskTraceId = new ObjectId().toString()
            map[taskTraceId] = id
        }
        [taskTraceId: taskTraceId]
    }

    private def getTimeEntriesFromRedmine(redmineProjectId) {
        def resp =  restClient.get("${redmineUrl}/time_entries.json?project_id=${redmineProjectId}")

        if(resp.getStatusCode() != HttpStatus.OK) {
            throw new Exception("Error al obtener los registros de hora de Redmine. HttpStatusCode: ${resp.getStatusCode()}")
        }
        resp.json.time_entries
    }

    private def getRedmineTaskById(Integer id) {
        def resp = restClient.get("${redmineUrl}/issues/${id}.json")

        if(resp.getStatusCode() != HttpStatus.OK) {
            throw new Exception("Error al obtener la tarea de Redmine. HttpStatusCode: ${resp.getStatusCode()}")
        }
        resp.json.issue
    }

    private def buildDetail(timeEntry) {
        [ member: [id: getMemberByEmail(timeEntry.user.id).id],
          date: Date.parse('yyyy-MM-dd', timeEntry.spent_on),
          hours: timeEntry.hours]
    }

    private def saveBlackboardTrace(trace, projectId, taskTraceList) {
        def responseTrace
        if(trace.id == null) {
            responseTrace = restClient.post("${gemsbbUrl}/traces") {
                contentType "application/json"
                json (
                  project: [id: projectId],
                  taskTraces: taskTraceList
                )
            }
        }
        else {
            responseTrace = restClient.put("${gemsbbUrl}/traces/${trace.id}") {
                contentType "application/json"
                json (
                  id: trace.id,
                  project: [id: projectId],
                  taskTraces: taskTraceList
                )
            }
        }

        if (responseTrace.getStatusCode() != HttpStatus.OK &&
            responseTrace.getStatusCode() != HttpStatus.CREATED) {
            throw new Exception("Error al guardar el registro de la traza. HttpStatusCode: ${responseTrace.getStatusCode()}")
        }

        responseTrace.json
    }

    def injectProjectTrace(String projectId, String externalProjectId) {
        def trace = getTraceFromBB(projectId)
        if(trace == null) {
            trace = [
                project: [
                    id: projectId
                ],
                taskTraces: new LinkedHashMap()
            ]
        }

        def mapping = getMapping(projectId, trace, 'Redmine', 'Trace')
        def redmineTimeEntries = getTimeEntriesFromRedmine(externalProjectId)

        if(redmineTimeEntries.size() > 0) {
            def taskTraceMap = new LinkedHashMap()
            redmineTimeEntries.each {
                if(!taskTraceMap.containsKey(it.issue.id)) {
                    taskTraceMap[it.issue.id] = [name: getRedmineTaskById(it.issue.id).subject, details: []]
                }
                taskTraceMap[it.issue.id].details.add(buildDetail(it))
            }

            def taskTraceList = taskTraceMap.collect {
                key, value ->
                    def taskTrace = getTaskTraceFromMap(key, mapping.map)
                    taskTrace << [
                        name: value.name,
                        status: 'ESTADO!',
                        traceDetails: value.details
                    ]
            }

            def bbTrace = saveBlackboardTrace(trace, projectId, taskTraceList)
            def bbMapping = saveBlackboardMapping(mapping, projectId, bbTrace)
        }
    }
}
