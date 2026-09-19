package com.chommiehub.app.ui.auth
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.chommiehub.app.R
import com.chommiehub.app.ui.student.StudentActivity
import com.google.firebase.auth.FirebaseAuth
class RegisterActivity:AppCompatActivity(){ override fun onCreate(s:Bundle?){super.onCreate(s);setContentView(R.layout.activity_register); val auth=FirebaseAuth.getInstance(); val email=findViewById<EditText>(R.id.etRegisterEmail); val pass=findViewById<EditText>(R.id.etRegisterPassword); val confirm=findViewById<EditText>(R.id.etConfirmPassword); val error=findViewById<TextView>(R.id.tvRegisterError)
    findViewById<Button>(R.id.btnRegister).setOnClickListener{ val e=email.text.toString().trim(); val p=pass.text.toString(); when{!Patterns.EMAIL_ADDRESS.matcher(e).matches()->error.text="Enter a valid email address.";p.length<6->error.text="Password must contain at least 6 characters.";p!=confirm.text.toString()->error.text="Passwords do not match.";else->auth.createUserWithEmailAndPassword(e,p).addOnSuccessListener{startActivity(Intent(this,StudentActivity::class.java));finishAffinity()}.addOnFailureListener{error.text=it.message}}}; findViewById<View>(R.id.tvBackToLogin).setOnClickListener{finish()} }
}
