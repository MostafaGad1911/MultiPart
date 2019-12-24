package mostafa.projects.multipart.Connection

import mostafa.projects.multipart.Model.FileInfo
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface UserClient {

    @Multipart
    @POST("uploadFile.php")
    fun uploadFile(
        @Part  file: MultipartBody.Part
    ): Call<FileInfo>


}