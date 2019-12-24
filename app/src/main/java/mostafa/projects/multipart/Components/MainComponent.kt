package mostafa.projects.dagger2.Component

import android.content.Context
import android.content.ServiceConnection
import dagger.Component
import mostafa.projects.doctorjobs.Modules.Settings
import javax.inject.Singleton

@Singleton
@Component (modules = [Settings::class])
interface MainComponent {
    fun connect() :Settings
}