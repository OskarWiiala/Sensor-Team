import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import com.georgv.sporttrackerapp.customHandlers.TypeConverterUtil
import com.georgv.sporttrackerapp.data.Session
import com.georgv.sporttrackerapp.data.TrackedSession
import com.georgv.sporttrackerapp.database.SessionDB
import com.georgv.sporttrackerapp.database.SessionDao
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class SessionRepository(context:Application) {
    private val sessionDao: SessionDao = SessionDB.get(context).sessionDao()

    fun getTrackedSession(id:Int): TrackedSession {
        return sessionDao.getTrackedSessionById(id)
    }

    fun getSession(id:Long): LiveData<Session> {
        return sessionDao.getSessionById(id)
    }

    fun getData(): LiveData<List<Session>> {
        return sessionDao.getAll()
    }

    fun getRunningSession(): Session{
        return sessionDao.getRunningSession()
    }

//    @RequiresApi(Build.VERSION_CODES.O)
//    fun getDay(): List<Session> {
//        LocalDate.parse(Date().toString(), DateTimeFormatter.ofPattern("dd"))
//        return SessionListDao.getMainVariablesToday(TypeConverterUtil().localDateToTimestamp(LocalDate.parse(Date().toString(), DateTimeFormatter.ofPattern("dd"))))
//    }

}
