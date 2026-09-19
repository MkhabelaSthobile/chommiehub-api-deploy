package com.chommiehub.app.ui.auth
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.chommiehub.app.R
import com.chommiehub.app.ui.student.StudentProfileFragment
class ProfileSetupActivity:AppCompatActivity(){override fun onCreate(s:Bundle?){super.onCreate(s);setContentView(R.layout.activity_profile_setup);if(s==null)supportFragmentManager.beginTransaction().replace(R.id.profileSetupContainer,StudentProfileFragment()).commit()}}
