package com.chommiehub.app.ui.student
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.chommiehub.app.R
import com.chommiehub.app.data.remote.ApiClient
import com.chommiehub.app.data.repository.StudentRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class MyApplicationsFragment:Fragment(R.layout.fragment_my_applications){
    override fun onViewCreated(v:View,b:Bundle?){
        val list=v.findViewById<LinearLayout>(R.id.applicationList)
        val uid=FirebaseAuth.getInstance().currentUser?.uid?:return
        viewLifecycleOwner.lifecycleScope.launch{
            try{
                val xs=StudentRepository(ApiClient.api).applications(uid); list.removeAllViews()
                if(xs.isEmpty()) v.findViewById<TextView>(R.id.tvEmpty).visibility=View.VISIBLE
                xs.forEach{a->
                    val item=layoutInflater.inflate(R.layout.item_student_application,list,false)
                    item.findViewById<TextView>(R.id.tvAppAccommodation).text=a.accommodationName
                    item.findViewById<TextView>(R.id.tvAppRoom).text="${a.roomTypePreference} • ${a.rentalPeriod}"
                    item.findViewById<TextView>(R.id.tvAppStatus).text=a.status
                    item.findViewById<TextView>(R.id.tvAppNotes).text=if(a.reviewNotes.isBlank())"Awaiting manager update" else a.reviewNotes
                    item.setOnClickListener{ parentFragmentManager.beginTransaction().replace(R.id.studentContainer,ApplicationStatusFragment.newInstance(a.status,a.assignedRoomId,a.accommodationName,a.reviewNotes)).addToBackStack(null).commit() }
                    list.addView(item)
                }
            }catch(e:Exception){Toast.makeText(requireContext(),e.message,Toast.LENGTH_LONG).show()}
        }
    }
}