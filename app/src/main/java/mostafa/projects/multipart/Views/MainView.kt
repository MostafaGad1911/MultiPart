package mostafa.projects.multipart.Views

import retrofit2.http.Part

interface MainView {
    fun initObjects()
    fun initViews()
    fun showLoading()
    fun hideLoading()

}