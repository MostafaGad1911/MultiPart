package mostafa.projects.multipart

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import mostafa.projects.dagger2.Component.DaggerMainComponent
import mostafa.projects.dagger2.Component.MainComponent
import mostafa.projects.multipart.Model.FileInfo
import mostafa.projects.multipart.Views.MainView
import okhttp3.*
import retrofit2.Call
import retrofit2.Response
import java.io.File

class MainActivity : AppCompatActivity(), MainView , View.OnClickListener{

    lateinit var pickImg :Button
    lateinit var uploadImg :Button
    lateinit var mainComponent: MainComponent
    lateinit var imageUri: Uri
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initViews()
        initObjects()
        pickImg.setOnClickListener(this)
        uploadImg.setOnClickListener(this)
//        mainComponent.connect().getService().uploadFile()
    }

    override fun initObjects() {
        mainComponent = DaggerMainComponent.create()
    }

    override fun initViews() {
        pickImg = findViewById(R.id.pickImg)
        uploadImg = findViewById(R.id.uploadImg)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK){
            if(data == null){
                Toast.makeText(this@MainActivity , "Unable to choose image" , Toast.LENGTH_SHORT).show()
                return
            }
            imageUri = data.data!!
        }
    }

    override fun onClick(view: View?) {
        when(view!!.id){
            R.id.pickImg -> {
               val intent = Intent(Intent.ACTION_PICK)
               intent.setType("image/*")
               startActivityForResult(intent , 0)
            }

            R.id.uploadImg -> {
              val file = File(imageUri.toString())
              val requestBody :RequestBody = RequestBody.create(MediaType.parse("multipart/form-data") , file)
              val multipartBody : MultipartBody.Part = MultipartBody.Part.createFormData("file" , file.name , requestBody)
              mainComponent.connect().getService().uploadFile(multipartBody).enqueue(object : retrofit2.Callback<FileInfo> {
                  override fun onFailure(call: Call<FileInfo>, t: Throwable) {
                     Log.w("UploadFile" , t.message.toString())
                  }

                  override fun onResponse(call: Call<FileInfo>, response: Response<FileInfo>) {
                      Log.w("UploadFile" , response.body().toString())
                  }

              })

            }
        }
    }
}
