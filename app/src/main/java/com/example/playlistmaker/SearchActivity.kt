package com.example.playlistmaker

import SearchHistory
import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class SearchActivity : AppCompatActivity() {

    private val urlApi = "https://itunes.apple.com"

    private val retrofit = Retrofit.Builder()
        .baseUrl(urlApi)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val iTunseService = retrofit.create(ApiSong::class.java)
    private val trackList = mutableListOf<Track>()
    private lateinit var inputEditText: EditText
    private lateinit var clearButton: ImageView
    private lateinit var linearLayout: LinearLayout
    private lateinit var savedText: String
    private lateinit var recyclerView: RecyclerView
    private lateinit var backButton: ImageView
    private lateinit var adapter: TrackAdapter
    private lateinit var imageHolder: ImageView
    private lateinit var textHolderMessage: TextView
    private lateinit var udpateButton: Button
    private lateinit var searchHistory: SearchHistory
    private lateinit var clearHistoryButton: Button
    private var lastSearchQuery: String? = null




    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        inputEditText = findViewById(R.id.inputEditText)
        clearButton = findViewById(R.id.clearIcon)
        linearLayout = findViewById(R.id.container)
        backButton = findViewById(R.id.backInMain)
        recyclerView = findViewById(R.id.recyclerView)
        textHolderMessage = findViewById(R.id.placeholderMessage)
        udpateButton = findViewById(R.id.refreshButton)
        imageHolder = findViewById(R.id.imageHolder)
        searchHistory = SearchHistory(this)
        clearHistoryButton = findViewById(R.id.clearHistoryButton)

        clearHistoryButton.setOnClickListener {
            clearSearchHistory()
        }

        setupRecyclerView()

        // Установка обработчика для кнопки "Очистить поисковый запрос"
        clearButton.setOnClickListener {
            inputEditText.setText("")
            hideSoftKeyboard()
            clearButton.visibility = View.GONE
            textHolderMessage.visibility = View.GONE
            recyclerView.visibility = View.GONE
            imageHolder.visibility = View.GONE
            searchHistory.clearSearchHistory()
            adapter.notifyDataSetChanged()
            updateUI()
        }

        // Установка TextWatcher для поля ввода
        val simpleTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                clearButton.visibility = clearButtonVisibility(s)
            }

            override fun afterTextChanged(s: Editable?) {}
        }
        inputEditText.addTextChangedListener(simpleTextWatcher)


        // Обработка нажатия на поле ввода для отображения клавиатуры
        inputEditText.setOnClickListener {
            inputEditText.text.clear()
            showSoftKeyboard()
            // Показ клавиатуры
        }

        udpateButton.setOnClickListener {
            val query = lastSearchQuery
            if (query != null) {
                performSearch(query)
            }
        }

        inputEditText.setOnEditorActionListener { _, actionId, _ ->
            val input = inputEditText.text.toString()
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                performSearch(input)
                lastSearchQuery = input
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }
        // Обработка нажатия на кнопку "Назад"
        backButton.setOnClickListener {
            finish()
        }
    }

    private fun clearSearchHistory() {
        searchHistory.clearSearchHistory()
        updateUI()
    }

    private fun updateUI() {
        val history = searchHistory.getSearchHistory()

        if (inputEditText.text.isEmpty() && history.isNotEmpty()) {
            textHolderMessage.text = getString(R.string.you_searched)
            recyclerView.visibility = View.VISIBLE
            adapter.setTrackList(history)  // Обновление данных в адаптере
        } else {
            textHolderMessage.text = ""
            recyclerView.visibility = View.GONE
        }
    }

    private fun performSearch(query: String) {
        iTunseService.search(query).enqueue(object : Callback<SongResponse> {
            @SuppressLint("NotifyDataSetChanged")
            override fun onResponse(call: Call<SongResponse>, response: Response<SongResponse>) {
                val bodyResponse = response.body()?.results
                if (response.isSuccessful) {
                    trackList.clear()
                    if (bodyResponse?.isNotEmpty() == true) {
                        trackList.addAll(bodyResponse)
                        adapter.notifyDataSetChanged()
                        recyclerView.visibility = View.VISIBLE
                    }
                    if (trackList.isEmpty()) {
                        imageHolder.visibility = View.VISIBLE
                        imageHolder.setImageResource(R.drawable.nothing_to_search)
                        showMessage(getString(R.string.nothing_find), "")
                    } else {
                        showMessage("", "")
                    }
                    udpateButton.visibility = View.GONE
                } else {
                    lastSearchQuery = query
                    imageHolder.visibility = View.VISIBLE
                    showMessage(
                        getString(R.string.trouble_with_network),
                        response.code().toString()
                    )
                    updateUI()
                }
            }

            override fun onFailure(call: Call<SongResponse>, t: Throwable) {
                lastSearchQuery = query
                imageHolder.visibility = View.VISIBLE
                imageHolder.setImageResource(R.drawable.network_error)
                showMessage(getString(R.string.trouble_with_network), t.message.toString())
                udpateButton.visibility = View.VISIBLE
                updateUI()
            }
        })
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun showMessage(text: String, additionalMessage: String) {
        if (text.isNotEmpty()) {
            textHolderMessage.visibility = View.VISIBLE
            trackList.clear()
            adapter.notifyDataSetChanged()
            textHolderMessage.text = text
        } else {
            textHolderMessage.visibility = View.GONE
        }
    }


    private fun setupRecyclerView() {
        adapter = TrackAdapter(trackList, searchHistory)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    // Метод для отображения клавиатуры
    private fun showSoftKeyboard() {
        inputEditText.requestFocus()
        val inputMethod = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethod.showSoftInput(inputEditText, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun hideSoftKeyboard() {
        val inputMethod = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethod.hideSoftInputFromWindow(inputEditText.windowToken, 0)
    }

    // Метод для определения видимости кнопки "Очистить поисковый запрос"
    private fun clearButtonVisibility(s: CharSequence?): Int {
        return if (s.isNullOrEmpty()) View.GONE else View.VISIBLE
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(PRODUCT_AMOUNT, inputEditText.text.toString())
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        savedText = savedInstanceState.getString(PRODUCT_AMOUNT, "")
        inputEditText.setText(savedText)
    }

    companion object {
        const val PRODUCT_AMOUNT = "PRODUCT_AMOUNT"
    }
}