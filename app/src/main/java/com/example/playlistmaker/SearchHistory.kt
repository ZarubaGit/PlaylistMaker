import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.playlistmaker.Track
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SearchHistory(context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("SearchHistory", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun getSearchHistory(): MutableList<Track> {
        val historyString = sharedPreferences.getString("history", null)
        return if (historyString != null) {
            gson.fromJson(historyString, object : TypeToken<MutableList<Track>>() {}.type)
        } else {
            mutableListOf()
        }
    }

    fun saveTrackToHistory(track: Track) {
        val history = getSearchHistory().toMutableList()
        if (history.none { it.trackId == track.trackId }) {
            history.add(0, track)
            if (history.size > 10) {
                history.removeAt(history.size - 1)
            }
            saveHistoryToPreferences(history)
            Log.d("SearchHistory", "Saved history: $history")
        }
    }

    private fun saveHistoryToPreferences(history: MutableList<Track>) {
        val historyString = gson.toJson(history)
        sharedPreferences.edit().putString("history", historyString).apply()
        Log.d("SearchHistory", "Saved history to SharedPreferences")
    }

    fun clearSearchHistory() {
        sharedPreferences.edit().remove("history").apply()
    }

    fun removeTrackFromHistory(trackId: Int) {
        val history = getSearchHistory().toMutableList()  // Создаем копию списка
        val index = history.indexOfFirst { it.trackId == trackId }
        if (index != -1) {
            history.removeAt(index)
            saveHistoryToPreferences(history)
        }
    }


}