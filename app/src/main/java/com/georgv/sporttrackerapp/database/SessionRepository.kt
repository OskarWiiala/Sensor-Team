import android.app.Application
import androidx.lifecycle.LiveData
import com.georgv.sporttrackerapp.data.Session
import com.georgv.sporttrackerapp.data.TrackedSession
import com.georgv.sporttrackerapp.database.SessionDB
import com.georgv.sporttrackerapp.database.SessionDao

class SessionRepository(context:Application) {
    private val sessionDao: SessionDao = SessionDB.get(context).sessionDao()

    fun getTrackedSession(id:Int): LiveData<TrackedSession> {
        return sessionDao.getTrackedSessionById(id)
    }

    fun getData(): LiveData<List<Session>> {
        return sessionDao.getAll()
    }

    fun getRunningSession(): Session{
        return sessionDao.getRunningSession()
    }

}
