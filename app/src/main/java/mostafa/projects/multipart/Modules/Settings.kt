package mostafa.projects.doctorjobs.Modules

import android.content.Context
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.Module
import dagger.Provides
import mostafa.projects.doctorjobs.Helper.Constants
import mostafa.projects.multipart.Connection.UserClient
import mostafa.projects.multipart.Model.FileInfo
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@Module
class Settings @Inject constructor(){
    lateinit var userClient: UserClient
    lateinit var retrofit: Retrofit


    @Provides
    fun getService(): UserClient {
        userClient = getClient().create(UserClient::class.java)
        return userClient
    }

    @Provides
    fun provideLinearLayoutManager(context: Context): LinearLayoutManager {
        return LinearLayoutManager(context)
    }





    @Provides
    fun getClient(): Retrofit {
        val client = OkHttpClient.Builder()
            .connectTimeout(100, TimeUnit.SECONDS)
            .readTimeout(100, TimeUnit.SECONDS).build()

        retrofit = Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()

        return retrofit
    }
}