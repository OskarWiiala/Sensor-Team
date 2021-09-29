import android.app.Application
import androidx.lifecycle.LiveData
import com.georgv.sporttrackerapp.data.Session
import com.georgv.sporttrackerapp.database.SessionDB
import com.georgv.sporttrackerapp.database.SessionDao

class SessionRepository(context:Application) {
    private val SessionListDao: SessionDao = SessionDB.get(context).sessionDao()

    fun getData(): LiveData<List<Session>> {
        return SessionListDao.getAll()
    }

}
