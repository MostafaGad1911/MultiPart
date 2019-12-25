package mostafa.projects.multipart

import android.Manifest
import android.annotation.TargetApi
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.ImageDecoder
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.crystal.crystalpreloaders.widgets.CrystalPreloader
import de.hdodenhof.circleimageview.CircleImageView
import id.zelory.compressor.Compressor
import mostafa.projects.dagger2.Component.DaggerMainComponent
import mostafa.projects.dagger2.Component.MainComponent
import mostafa.projects.multipart.Views.MainView
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import java.io.File
import java.net.URI


class MainActivity : AppCompatActivity(), MainView, View.OnClickListener {

    val IMAGE_PICK_CODE = 1000;
    val PERMISSION_CODE = 1001;
    lateinit var pickImg: Button
    lateinit var uploadImg: Button
    lateinit var picked_img: CircleImageView
    lateinit var loading: CrystalPreloader
    lateinit var mainComponent: MainComponent
    var filePath: String? = null
    var postPath: String? = null
    lateinit var compressedFile: File
    lateinit var selectedImage:Uri
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initViews()
        initObjects()
        pickImg.setOnClickListener(this)
        uploadImg.setOnClickListener(this)

    }

    override fun initObjects() {
        mainComponent = DaggerMainComponent.create()
    }

    override fun initViews() {
        pickImg = findViewById(R.id.pickImg)
        uploadImg = findViewById(R.id.uploadImg)
        loading = findViewById(R.id.loading)
        picked_img = findViewById(R.id.Picked_img)
    }

    override fun showLoading() {
        loading.visibility = View.VISIBLE
    }

    override fun hideLoading() {
        loading.visibility = View.GONE
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (data == null) {
                Toast.makeText(this@MainActivity, "Unable to choose image", Toast.LENGTH_SHORT)
                    .show()
                return
            }
            if (data != null) {
                selectedImage = data.data!!
                picked_img.setImageURI(selectedImage)
                val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
                val cursor =
                    contentResolver.query(selectedImage!!, filePathColumn, null, null, null)
                assert(cursor != null)
                cursor!!.moveToFirst()
                val columnIndex = cursor.getColumnIndex(filePathColumn[0])
                filePath = cursor.getString(columnIndex)
                cursor.close()
                postPath = filePath
            }
        }
    }

    override fun onClick(view: View?) {
        when (view!!.id) {
            R.id.pickImg -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) ==
                        PackageManager.PERMISSION_DENIED
                    ) {
                        val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE);
                        requestPermissions(permissions, PERMISSION_CODE);
                    } else {
                        pickImageFromGallery();
                    }
                } else {
                    pickImageFromGallery();
                }

            }
            R.id.uploadImg -> {
                showLoading()
                if (postPath == null || postPath == "") {
                    Toast.makeText(this, "please select an image ", Toast.LENGTH_LONG).show()
                    return
                } else {
                    val file = File(postPath!!)
                    compressedFile = Compressor(this).compressToFile(file);
                    val requestBody = RequestBody.create(MediaType.parse("*/*"), compressedFile)
                    val multipartBody: MultipartBody.Part =
                        MultipartBody.Part.createFormData("file", file.name, requestBody)
                    mainComponent.connect().getService().uploadFile(multipartBody)
                        .enqueue(object : retrofit2.Callback<ResponseBody> {
                            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                                Log.w("UploadFile", t.message.toString())
                            }

                            @RequiresApi(Build.VERSION_CODES.P)
                            override fun onResponse(
                                call: Call<ResponseBody>,
                                response: Response<ResponseBody>
                            ) {
                                if (response.isSuccessful) {
                                    hideLoading()
                                    Toast.makeText(
                                        this@MainActivity,
                                        "Uploaded successfully",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    sendNotifications("Uploaded successfully" , selectedImage)
                                } else {
                                    hideLoading()
                                    Toast.makeText(
                                        this@MainActivity,
                                        "Uploaded failed",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        })
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            PERMISSION_CODE -> {
                if (grantResults.size > 0 && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED
                ) {
                    //permission from popup granted
                    pickImageFromGallery()
                } else {
                    //permission from popup denied
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun pickImageFromGallery() {
        val galleryIntent = Intent(
            Intent.ACTION_PICK,
            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        startActivityForResult(galleryIntent, IMAGE_PICK_CODE)
    }

    @TargetApi(Build.VERSION_CODES.P)
    @RequiresApi(Build.VERSION_CODES.P)
    fun sendNotifications(messageBody: String?, imageUri :Uri) {
        val channelId = "sample"
        val source:ImageDecoder.Source = ImageDecoder.createSource(this.getContentResolver(), imageUri);
        val bitmap = ImageDecoder.decodeBitmap(source)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_upload_black)
            .setLargeIcon(bitmap)
            .setContentTitle("MultiPart Sample")
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Channel human readable title",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }
        notificationManager.notify(0, notificationBuilder.build())
    }
}
