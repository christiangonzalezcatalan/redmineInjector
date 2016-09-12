package redmineInjector.Mocks

/**
 * Created by christian on 27-08-16.
 */
class BlackboardResponses {
    static String getPlanFromBlackboard(){
        """[
  {
    "id": "57cf835f8acec65eba3b579f",
    "project": {
      "id": "57cc59368acec62bf2f7d7ed"
    },
    "tasks": [
      {
        "contributors": [],
        "dueDate": "2016-09-13T06:00:00Z",
        "name": "Revisión de código",
        "responsible": {
          "id": "57c3c4838acec662dab6dcf2"
        },
        "startDate": "2016-08-28T06:00:00Z",
        "status": "In Progress",
        "taskId": "57d0c86c8acec6725ee5accf"
      },
      {
        "contributors": [],
        "dueDate": "2016-08-30T06:00:00Z",
        "name": "Dashboard",
        "responsible": null,
        "startDate": "2016-08-25T06:00:00Z",
        "status": "New",
        "taskId": "57d0c86c8acec6725ee5acd0"
      },
      {
        "contributors": [],
        "dueDate": "2016-08-24T06:00:00Z",
        "name": "Inyector plan Redmine",
        "responsible": {
          "id": "57c3c4858acec662dab6dcf4"
        },
        "startDate": "2016-08-22T06:00:00Z",
        "status": "New",
        "taskId": "57d0c86c8acec6725ee5acd1"
      },
      {
        "contributors": [],
        "dueDate": "2016-08-19T06:00:00Z",
        "name": "API Blackboard",
        "responsible": {
          "id": "57c3c4858acec662dab6dcf4"
        },
        "startDate": "2016-08-08T06:00:00Z",
        "status": "New",
        "taskId": "57d0c86c8acec6725ee5acd2"
      }
    ]
  }
]"""
    }

    static String getPlanMappingsFromBlackboard() {
        """[
  {
    "id": "57d0c86c8acec66d7306700d",
    "entityType": "Plan",
    "map": {
      "57d0c86c8acec6725ee5accf": 11,
      "57d0c86c8acec6725ee5acd0": 10,
      "57d0c86c8acec6725ee5acd1": 9,
      "57d0c86c8acec6725ee5acd2": 8
    },
    "project": {
      "id": "57cc59368acec62bf2f7d7ed"
    },
    "tool": "Redmine"
  }
]"""
    }

    static String getMemberByEmailFromBlackboard(email, id, name) {
        """[
  {
    "id": ${id},
    "email": ${email},
    "name": ${name}
  }
]"""
    }

    static String postPlanToBlackbord() {
        """{
  "id": "57cf835f8acec65eba3b579f",
  "project": {
    "id": "57cc59368acec62bf2f7d7ed"
  },
  "tasks": [
    {
      "contributors": [
        {
          "id": "57c3c4838acec662dab6dcf2"
        }
      ],
      "dueDate": "2016-08-16T21:00:20Z",
      "name": "Tarea 1",
      "responsible": {
        "id": "57c3c4838acec662dab6dcf2"
      },
      "startDate": "2016-08-16T13:00:20Z",
      "status": "Mi estado",
      "taskId": "57c3c4868acec662dab6dcf6"
    },
    {
      "contributors": [],
      "dueDate": "2016-08-20T13:00:20Z",
      "name": "Tarea 2",
      "responsible": {
        "id": "57c3c4838acec662dab6dcf2"
      },
      "startDate": "2016-08-16T13:00:20Z",
      "status": "Mi estado",
      "taskId": "57c3c4868acec662dab6dcf7"
    }
  ]
}"""
    }

    static String getTraceFromBlackboard() {
        """[
  {
    "id": "57d5f5e48acec62fb22f8a73",
    "project": {
      "id": "57cc59368acec62bf2f7d7ed"
    },
    "taskTraces": [
      {
        "name": null,
        "status": "ESTADO!",
        "taskTraceId": "57d5fe9c8acec641a6c8d926",
        "traceDetails": [
          {
            "date": "2016-09-11T06:00:00Z",
            "hours": 5,
            "member": {
              "id": "57c3c4858acec662dab6dcf4"
            }
          }
        ]
      },
      {
        "name": null,
        "status": "ESTADO!",
        "taskTraceId": "57d5f5e28acec63dfc6b1317",
        "traceDetails": [
          {
            "date": "2016-08-30T06:00:00Z",
            "hours": 3,
            "member": {
              "id": "57c3c4858acec662dab6dcf4"
            }
          },
          {
            "date": "2016-08-29T06:00:00Z",
            "hours": 2,
            "member": {
              "id": "57c3c4858acec662dab6dcf4"
            }
          }
        ]
      }
    ]
  }
]"""
    }

    static String getTraceMappingsFromBlackboard() {
        """[
  {
    "id": "57d5f5e88acec62fb22f8a74",
    "entityType": "Trace",
    "map": {
      "57d5f5e28acec63dfc6b1317": 9,
      "57d5fe9c8acec641a6c8d926": 11
    },
    "project": {
      "id": "57cc59368acec62bf2f7d7ed"
    },
    "tool": "Redmine"
  }
]"""
    }
}
