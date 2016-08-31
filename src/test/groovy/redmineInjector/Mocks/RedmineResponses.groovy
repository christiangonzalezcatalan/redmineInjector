package redmineInjector.Mocks

/**
 * Created by christian on 27-08-16.
 */
class RedmineResponses {
    static String listarRegistrosPlan() {
        """{
  "issues": [
    {
      "id": 10,
      "project": {
        "id": 3,
        "name": "Dashboard Gems"
      },
      "tracker": {
        "id": 2,
        "name": "Feature"
      },
      "status": {
        "id": 1,
        "name": "New"
      },
      "priority": {
        "id": 2,
        "name": "Normal"
      },
      "author": {
        "id": 4,
        "name": "Christian González"
      },
      "subject": "Dashboard",
      "description": "Dashboard que consume data del blackboard",
      "start_date": "2016-08-25",
      "due_date": "2016-08-30",
      "done_ratio": 0,
      "estimated_hours": 40,
      "created_on": "2016-08-17T02:22:40Z",
      "updated_on": "2016-08-17T02:23:57Z"
    },
    {
      "id": 9,
      "project": {
        "id": 3,
        "name": "Dashboard Gems"
      },
      "tracker": {
        "id": 2,
        "name": "Feature"
      },
      "status": {
        "id": 1,
        "name": "New"
      },
      "priority": {
        "id": 3,
        "name": "High"
      },
      "author": {
        "id": 4,
        "name": "Christian González"
      },
      "assigned_to": {
        "id": 4,
        "name": "Christian González"
      },
      "subject": "Inyector plan Redmine",
      "description": "Carga de un plan de redmine en blackboard.",
      "start_date": "2016-08-22",
      "due_date": "2016-08-24",
      "done_ratio": 0,
      "estimated_hours": 10,
      "created_on": "2016-08-17T02:19:49Z",
      "updated_on": "2016-08-17T02:21:01Z"
    },
    {
      "id": 8,
      "project": {
        "id": 3,
        "name": "Dashboard Gems"
      },
      "tracker": {
        "id": 2,
        "name": "Feature"
      },
      "status": {
        "id": 1,
        "name": "New"
      },
      "priority": {
        "id": 3,
        "name": "High"
      },
      "author": {
        "id": 4,
        "name": "Christian González"
      },
      "assigned_to": {
        "id": 4,
        "name": "Christian González"
      },
      "subject": "API Blackboard",
      "description": "API Rest para lectura/escritura en dashboard.",
      "start_date": "2016-08-08",
      "due_date": "2016-08-19",
      "done_ratio": 0,
      "estimated_hours": 40,
      "created_on": "2016-08-17T02:18:31Z",
      "updated_on": "2016-08-17T02:18:31Z"
    }
  ],
  "total_count": 3,
  "offset": 0,
  "limit": 25
}"""
    }

    static String listarRegistrosHorasTrabajadas()
    {
      """{
  "time_entries": [
    {
      "id": 10,
      "project": {
        "id": 3,
        "name": "Dashboard Gems"
      },
      "issue": {
        "id": 9
      },
      "user": {
        "id": 3,
        "name": "Christian González"
      },
      "activity": {
        "id": 9,
        "name": "Development"
      },
      "hours": 3,
      "comments": "",
      "spent_on": "2016-08-30",
      "created_on": "2016-08-30T04:00:20Z",
      "updated_on": "2016-08-30T04:00:20Z"
    },
    {
      "id": 11,
      "project": {
        "id": 3,
        "name": "Dashboard Gems"
      },
      "issue": {
        "id": 9
      },
      "user": {
        "id": 3,
        "name": "Christian González"
      },
      "activity": {
        "id": 9,
        "name": "Development"
      },
      "hours": 2,
      "comments": "",
      "spent_on": "2016-08-29",
      "created_on": "2016-08-30T04:03:17Z",
      "updated_on": "2016-08-30T04:03:17Z"
    }
  ],
  "total_count": 2,
  "offset": 0,
  "limit": 50
}"""
    }

    static String obtenerIssue()
    {
      """{
  "issue": {
    "id": 9,
    "project": {
      "id": 3,
      "name": "Dashboard Gems"
    },
    "tracker": {
      "id": 2,
      "name": "Feature"
    },
    "status": {
      "id": 1,
      "name": "New"
    },
    "priority": {
      "id": 3,
      "name": "High"
    },
    "author": {
      "id": 3,
      "name": "Christian González"
    },
    "assigned_to": {
      "id": 3,
      "name": "Christian González"
    },
    "subject": "Inyector plan Redmine",
    "description": "Carga de un plan de redmine en blackboard.",
    "start_date": "2016-08-22",
    "due_date": "2016-08-24",
    "done_ratio": 0,
    "estimated_hours": 10,
    "spent_hours": 5,
    "created_on": "2016-08-17T02:19:49Z",
    "updated_on": "2016-08-17T02:21:01Z"
  }
}"""
    }
}
