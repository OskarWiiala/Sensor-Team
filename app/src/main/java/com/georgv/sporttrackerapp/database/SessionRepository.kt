import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import com.georgv.sporttrackerapp.customHandlers.TypeConverterUtil
import com.georgv.sporttrackerapp.data.Session
import com.georgv.sporttrackerapp.data.TrackedSession
import com.georgv.sporttrackerapp.database.SessionDB
import com.georgv.sporttrackerapp.database.SessionDao
import com.georgv.sporttrackerapp.viewmodel.TrackedSessionLiveData
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class SessionRepository(context:Application) {
    private val sessionDao: SessionDao = SessionDB.get(context).sessionDao()


    fun getData(): LiveData<List<Session>> {
        return sessionDao.getAllFinishedSessions(false)
    }

//    fun getLocationData():TrackedSessionLiveData {
//        return TrackedSessionLiveData(context)
//    }


//    @RequiresApi(Build.VERSION_CODES.O)
//    fun getDay(): List<Session> {
//        LocalDate.parse(Date().toString(), DateTimeFormatter.ofPattern("dd"))
//        return SessionListDao.getMainVariablesToday(TypeConverterUtil().localDateToTimestamp(LocalDate.parse(Date().toString(), DateTimeFormatter.ofPattern("dd"))))
//    }

}
