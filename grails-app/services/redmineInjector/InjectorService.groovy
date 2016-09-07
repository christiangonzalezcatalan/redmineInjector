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

    private String getProjectId(Integer id, String name) {
        def resp = restClient.get("${gemsbbUrl}/projects/search?externalKey=${id}&tool=Redmine")
        JSONObject result = resp.json

        if(result.size() == 1 || result.id != null) {
            return result.id
        }
        else {
            def rpost = restClient.post("${gemsbbUrl}/projects") {
                contentType "application/json"
                json(
                    externalKey: id,
                    tool: toolName,
                    name: name
                )
            }
            return rpost.json.id
        }
    }

    private String getPlanId(Integer id) {
        def resp = restClient.get("${gemsbbUrl}/plans/search?externalKey=${id}&tool=Redmine")
        JSONObject result = resp.json

        if(result.size() == 1 || result.id != null) {
            return result.id
        }
    }

    private String getMemberId(Integer id) {
        def resp = restClient.get("${gemsbbUrl}/members/search?externalKey=${id}&tool=Redmine")
        JSONObject result = resp.json

        if(result.size() == 1 || result.id != null) {
            return result.id
        }
        else {
            def apiKey = 'baa9da1d47247ea95bedc425027e7bb30df8f883'
            def getResponse = restClient.get("${redmineUrl}/users/${id}.json?key=${apiKey}")
            def user = getResponse.json.user
            def rpost = restClient.post("${gemsbbUrl}/members") {
                contentType "application/json"
                json {
                    name = "${user.firstname} ${user.lastname}"
                    email = user.mail
                    externalKey = user.id
                    tool = InjectorService.toolName
                }
            }

            return rpost.json.id
        }
    }

    private def buildTask(JSONObject issue) {
        def responsibleId = null
        if(issue.assigned_to != null) {
            responsibleId = getMemberId(issue.assigned_to.id.toInteger())
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

    def getPlanFromBB(String projectId) {
        println "get ${gemsbbUrl}/plans?projectId=${projectId}"
        def resp = restClient.get("${gemsbbUrl}/plans?projectId=${projectId}")
        JSONObject result = resp.json

        println resp.getStatusCode()

        if(result.size() == 1 || result.id != null) {
            return result
        }
    }

    def getTaskFromMap(id, map){
        def taskId = map.findAll() { key, value -> value == id}
                        .collect() { key, value -> key }​[0]
        if(taskId == null) {
            taskId = new ObjectId().toString()
            map[taskId] = id
        }
        [taskId: taskId]
    }

    def getMappingFromBB(projectId, tool, entityType) {
        println "get ${gemsbbUrl}/mappings?projectId=${projectId}&tool=${tool}&entityType=${entityType}"
        def resp = restClient.get(
            "${gemsbbUrl}/mappings?projectId=${projectId}&tool=${tool}&entityType=${entityType}")

        if(resp.getStatusCode() == HttpStatus.OK) {
            JSONObject result = resp.json

            if(result.size() == 1 || result.id != null) {
                return result
            }
        }
    }

    def getMemberByEmail(redmineUserId) {
        def resp = restClient.get(
            "${redmineUrl}/users/${redmineUserId}.json?key=baa9da1d47247ea95bedc425027e7bb30df8f883")
        JSONObject result = resp.json
        println "member1 response (${redmineUserId}: ${redmineUrl}/users/${redmineUserId}.json?key=baa9da1d47247ea95bedc425027e7bb30df8f883): ${resp.getStatusCode()} - ${resp.json}"

        if(result.user != null) {
            def memberResp = restClient.get(
                "${gemsbbUrl}/members?email=${result.user.mail}")
            println "memberResp.json: ${memberResp.json}"
            memberResp.json
        }
    }

    /*
    0. Get proyecto?
    1- Get plan
    2- Get mapping
    3. Get issues
    4. Put planId
    5. Put mapping
    */
    def injectPlan(String projectId, String externalProjectId) {
        def plan = getPlanFromBB(projectId)
        def mapping
        if(plan != null) {
            mapping = getMappingFromBB(projectId, 'Redmine', 'Plan')
        }
        else {
            plan = [
                project: [
                    id: projectId
                ],
                tasks: new LinkedHashMap()
            ]
        }

        if(mapping == null) {
            mapping = [
                project: [
                    id: projectId
                ],
                tool: 'Redmine',
                entityType: 'Plan',
                map: new LinkedHashMap()
            ]
        }

        println "mapping: ${mapping}"

        def resp = restClient.get("${redmineUrl}/issues.json?project_id=${externalProjectId}")
        JSONObject result = resp.json
        if(result.issues.size() > 0) {
            def taskList = []

            result.issues.each {
                def issue = it
                def responsibleId = null
                if(issue.assigned_to != null) {
                    //responsibleId = getMemberId(issue.assigned_to.id.toInteger())
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

            def responseMapping
            // Post de mapping
            if(mapping.id == null) {
                responseMapping = restClient.post("${gemsbbUrl}/projects/${projectId}/mappings") {
                    contentType "application/json"
                    json {
                        project = mapping.project
                        tool = mapping.tool
                        entityType = mapping.entityType
                        externalId = responsePlan.json.id
                        map = mapping.map
                    }
                }
            }
            // Put de mapping
            else {
                responsePlan = restClient.put("${gemsbbUrl}/projects/${projectId}/mappings/${mapping.id}") {
                    contentType "application/json"
                    json {
                        id = mapping.id
                        project = mapping.project
                        tool = mapping.tool
                        entityType = mapping.entityType
                        externalId = responsePlan.json.id
                        map = mapping.map
                    }
                }
            }

            println responseMapping.getStatusCode()
        }

        /*def resp = restClient.get("${redmineUrl}/issues.json?project_id=${externalProjectId}")
        JSONObject result = resp.json
        if(result.issues.size() > 0) {
            def taskList = []

            result.issues.each {
                taskList.add(buildTask(it))
            }

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
                responsePlan = restClient.put("${gemsbbUrl}/plans/${planId}") {
                    contentType "application/json"
                    json {
                        id = planId
                        externalKey = externalProjectId
                        tool = InjectorService.toolName
                        project = [id: projectId]
                        tasks = taskList
                    }
                }
            }

            if (responsePlan.getStatusCode() != HttpStatus.OK &&
                responsePlan.getStatusCode() != HttpStatus.CREATED) {
                throw new Exception("Error al guardar el registro del plan. HttpStatusCode: ${responsePlan.getStatusCode()}")
            }
        }*/
    }

    def injectProjectPlan(Integer externalProjectId) {
        def resp = restClient.get("${redmineUrl}/issues.json?project_id=${externalProjectId}")
        JSONObject result = resp.json
        if(result.issues.size() > 0) {
            def firstIssue = result.issues[0]
            def projectId = getProjectId(firstIssue.project.id, firstIssue.project.name)
            def taskList = []

            result.issues.each {
                taskList.add(buildTask(it))
            }

            def planId = getPlanId(externalProjectId)
            def responsePlan
            if(planId == null) {
                responsePlan = restClient.post("${gemsbbUrl}/plans") {
                    contentType "application/json"
                    json {
                        externalKey = externalProjectId
                        tool = InjectorService.toolName
                        project = [id: projectId]
                        tasks = taskList
                    }
                }
            }
            else {
                responsePlan = restClient.put("${gemsbbUrl}/plans/${planId}") {
                    contentType "application/json"
                    json {
                        id = planId
                        externalKey = externalProjectId
                        tool = InjectorService.toolName
                        project = [id: projectId]
                        tasks = taskList
                    }
                }
            }

            if (responsePlan.getStatusCode() != HttpStatus.OK &&
                responsePlan.getStatusCode() != HttpStatus.CREATED) {
                throw new Exception("Error al guardar el registro del plan. HttpStatusCode: ${responsePlan.getStatusCode()}")
            }
        }
    }

    private String getTraceId(Integer id) {
        def resp = restClient.get("${gemsbbUrl}/traces/search?externalKey=${id}&tool=Redmine")
        JSONObject result = resp.json

        if(result.size() == 1 || result.id != null) {
            return result.id
        }
    }

    private def getRedmineTaskById(Integer id) {
        def resp = restClient.get("${redmineUrl}/issues/${id}.json")
        if(resp.getStatusCode() != HttpStatus.OK) {
            throw new Exception("Error al obtener la tarea. Status code: ${resp.getStatusCode()}")
        }
        resp.json
    }

    def buildDetail(timeEntry) {
        [ member: [id: getMemberId(timeEntry.user.id)],
          date: Date.parse('yyyy-MM-dd', timeEntry.spent_on),
          hours: timeEntry.hours]
    }

    def injectProjectTrace(Integer externalProjectId) {
        def resp = restClient.get("${redmineUrl}/time_entries.json?project_id=${externalProjectId}")
        JSONObject result = resp.json
        if(result.time_entries.size() > 0) {
            def firstEntry = result.time_entries[0]
            def projectId = getProjectId(firstEntry.project.id, firstEntry.project.name)
            def taskTraceMap = new LinkedHashMap()

            result.time_entries.each {
                if(!taskTraceMap.containsKey(it.issue.id)) {
                  taskTraceMap[it.issue.id] = [name: getRedmineTaskById(it.issue.id).subject, details: []]
                }
                taskTraceMap[it.issue.id].details.add(buildDetail(it))
            }

            def taskTraces = taskTraceMap.collect {
                key, value -> [name: value.name, status: 'ESTADO!', traceDetails: value.details]
            }

            def traceId = getTraceId(externalProjectId)
            def responseTrace
            if(traceId == null) {
                responseTrace = restClient.post("${gemsbbUrl}/traces") {
                    contentType "application/json"
                    json (
                      project: [id: projectId],
                      taskTraces: taskTraces,
                      externalKey: externalProjectId,
                      tool: InjectorService.toolName
                    )
                }
            }
            else {
                responseTrace = restClient.put("${gemsbbUrl}/traces/${traceId}") {
                    contentType "application/json"
                    json (
                      id: traceId,
                      project: [id: projectId],
                      taskTraces: taskTraces,
                      externalKey: externalProjectId,
                      tool: InjectorService.toolName
                    )
                }
            }

            if (responseTrace.getStatusCode() != HttpStatus.OK &&
                responseTrace.getStatusCode() != HttpStatus.CREATED) {
                throw new Exception("Error al guardar el registro del plan. HttpStatusCode: ${responseTrace.getStatusCode()}")
            }
        }
    }
}
