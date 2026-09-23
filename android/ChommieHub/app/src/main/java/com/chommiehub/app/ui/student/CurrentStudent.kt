package com.chommiehub.app.ui.student

import com.chommiehub.app.data.model.student.StudentResponse

/**
 * Stores the currently authenticated ChommieHub student identity and lifecycle state.
 *
 * Firebase remains responsible for authentication, while these values store the matching
 * backend student identifier and whether the student is currently an Applicant or Resident.
 */
object CurrentStudent {
     var ID = ""
     var USER_TYPE = "Applicant"
     var ACCOMMODATION_ID = ""
     var ROOM_ID = ""

     /**
      * Refreshes the cached student state using the latest profile returned by the REST API.
      */
     fun update(student: StudentResponse) {
          ID = student.id
          USER_TYPE = student.userType
          ACCOMMODATION_ID = student.accommodationId
          ROOM_ID = student.roomId
     }

     /**
      * Clears cached identity information when the authenticated user signs out.
      */
     fun clear() {
          ID = ""
          USER_TYPE = "Applicant"
          ACCOMMODATION_ID = ""
          ROOM_ID = ""
     }
}