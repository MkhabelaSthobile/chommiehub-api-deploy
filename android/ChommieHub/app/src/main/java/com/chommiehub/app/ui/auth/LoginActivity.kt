package com.chommiehub.app.ui.auth
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.chommiehub.app.R
import com.chommiehub.app.ui.student.StudentActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
class LoginActivity:AppCompatActivity(){
    private val auth by lazy{FirebaseAuth.getInstance()}; private val googleCode=120
    override fun onCreate(s:Bundle?){super.onCreate(s); if(auth.currentUser!=null){openStudent();return}; setContentView(R.layout.activity_login)
        val email=findViewById<EditText>(R.id.etLoginEmail); val password=findViewById<EditText>(R.id.etLoginPassword); val error=findViewById<TextView>(R.id.tvLoginError)
        findViewById<Button>(R.id.btnLogin).setOnClickListener{ val e=email.text.toString().trim(); val p=password.text.toString(); if(!Patterns.EMAIL_ADDRESS.matcher(e).matches()) error.text="Enter a valid email address." else if(p.isBlank()) error.text="Enter your password." else auth.signInWithEmailAndPassword(e,p).addOnSuccessListener{openStudent()}.addOnFailureListener{error.text=it.message} }
        findViewById<View>(R.id.btnGoogleLogin).setOnClickListener{ val gso=GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build(); startActivityForResult(GoogleSignIn.getClient(this,gso).signInIntent,googleCode) }

    }
    @Deprecated("Deprecated in Java") override fun onActivityResult(r:Int,c:Int,d:Intent?){super.onActivityResult(r,c,d); if(r==googleCode) try{ val account=GoogleSignIn.getSignedInAccountFromIntent(d).getResult(Exception::class.java); auth.signInWithCredential(GoogleAuthProvider.getCredential(account.idToken,null)).addOnSuccessListener{openStudent()} }catch(e:Exception){Toast.makeText(this,e.message,Toast.LENGTH_LONG).show()} }
    private fun openStudent(){startActivity(Intent(this,StudentActivity::class.java));finish()}
}
