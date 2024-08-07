package com.example.playlistmaker.ui.audioPlayer

import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.ActivityAudioPlayerBinding
import com.example.playlistmaker.databinding.FragmentAudioPlayerBinding
import com.example.playlistmaker.domain.models.PlayList
import com.example.playlistmaker.domain.models.Track
import com.example.playlistmaker.domain.utils.DpToPx
import com.example.playlistmaker.ui.audioPlayer.view_model_audio_player.AudioPlayerViewModel
import com.example.playlistmaker.ui.media.addPlayList.AddPlayListFragment
import com.example.playlistmaker.ui.media.playList.PlayListState
import com.example.playlistmaker.ui.media.playList.PlayListTrackState
import com.example.playlistmaker.util.Changer
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.Locale


class AudioPlayerFragment : Fragment() {


    private val viewModel: AudioPlayerViewModel by viewModel()
    private lateinit var artworkImageView: ImageView
    private lateinit var binding: FragmentAudioPlayerBinding
    private lateinit var savedTimeTrack: String
    private lateinit var track: Track

    private val playlistAdapter = PlayListAdapter {
        addTrackInPlaylist(it)
    }

    private var isPlaylistClickAllowed = true


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAudioPlayerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        track = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable(TRACK_INFO, Track::class.java)!!
        } else {
            arguments?.getParcelable(TRACK_INFO)!!
        }


        viewModel.observeStateLiveData().observe(viewLifecycleOwner) {
            savedTimeTrack = it.progress
            binding.playButton.isEnabled = it.isPlayButtonEnabled
            binding.playButton.setImageResource(it.buttonResource)
            binding.trackTime.text = it.progress
        }

        viewModel.observeFavoriteState().observe(viewLifecycleOwner) { isFavorite ->
            updateFavoriteButton(isFavorite)
            track.isFavorite = isFavorite
        }

        viewModel.checkIfFavorite(track)

        if (track.isFavorite) {
            binding.likeButton.setImageResource(R.drawable.button_was_liked)
        } else {
            binding.likeButton.setImageResource(R.drawable.button_liked_track)
        }



        viewModel.observePlaylistState().observe(viewLifecycleOwner) {
            renderPlaylists(it)
        }
        binding.recyclerPlaylistsInPLayer.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerPlaylistsInPLayer.adapter = playlistAdapter

        val bottomSheetContainer = binding.bottomSheetPlaylist
        val bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetContainer).apply {
            state = BottomSheetBehavior.STATE_HIDDEN
        }

        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when(newState) {
                    BottomSheetBehavior.STATE_HIDDEN -> binding.overlay.visibility = View.GONE
                    else -> binding.overlay.visibility = View.VISIBLE
                }
            }
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
            }
        })

        viewModel.observeAddedToPlaylistState().observe(viewLifecycleOwner) { addedToPlaylist ->
            when(addedToPlaylist) {
                is PlayListTrackState.Match -> renderToast(addedToPlaylist.playlistName, false)
                is PlayListTrackState.Added -> {
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
                    renderToast(addedToPlaylist.playlistName, true)
                    binding.addToPlayListButton.setImageResource(R.drawable.button_added_in_playlist)
                }
                else -> Unit
            }

        }


        binding.minAndSecTrack.text = Changer.convertMillisToMinutesAndSeconds(track.trackTimeMillis)
        binding.trackTime.text = Changer.convertMillisToMinutesAndSeconds(track.trackTimeMillis)
        artworkImageView = binding.artworkImageView
        binding.nameTrackTextView.text = track.trackName
        binding.artistNameTextView.text = track.artistName
        binding.nameOfAlbum.text = track.collectionName ?: ""
        binding.yearRightSide.text = convertToYear(track.releaseDate)?: ""
        binding.primaryGenreName.text = track.primaryGenreName ?: ""
        binding.countryRightSide.text = track.country ?: ""

        Glide.with(this)
            .load(track.getCoverArtwork())
            .centerCrop()
            .transform((RoundedCorners(resources.getDimensionPixelOffset(R.dimen.dimen8dp))))
            .placeholder(R.drawable.placeholder)
            .error(R.drawable.placeholder)
            .into(this.artworkImageView)


        if (track.previewUrl != null) {
            viewModel.preparePlayer(previewUrl = track.previewUrl)
        } else {
            Toast.makeText(context, "Данные трека не прогрузились", Toast.LENGTH_SHORT).show()
        }

        binding.playButton.setOnClickListener {
            viewModel.playbackControl()
        }

        binding.toolBar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    findNavController().popBackStack()
                }
            }
        )

        binding.likeButton.setOnClickListener {
            viewModel.onFavoriteClicked(track)
        }

        binding.addToPlayListButton.setOnClickListener {
            viewModel.getPlaylists()
            binding.overlay.visibility = View.VISIBLE
            bottomSheetBehavior.halfExpandedRatio = 0.65F
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED
        }

        binding.newPlayListButton.setOnClickListener {
            findNavController().navigate(R.id.action_audioPlayerFragment_to_addPlayListFragment)

            bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            binding.audioPlayerMainScreen.isVisible = false
            binding.overlay.visibility = View.GONE
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.pausePlayer()
    }

    private fun renderToast(playlistName: String?, added: Boolean) {
        if (added) {

            Toast.makeText(
                requireContext(),
                getString(R.string.playlist_track_added, playlistName),
                Toast.LENGTH_SHORT
            ).show()
        }
        else {
            Toast.makeText(
                requireContext(),
                getString(R.string.playlist_track_exists, playlistName),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun renderPlaylists(state: PlayListState) {
        when(state) {
            is PlayListState.Empty -> showEmpty()
            is PlayListState.Content -> showContent(state.playlists)
        }
    }

    private fun showEmpty() {
        binding.recyclerPlaylistsInPLayer.visibility = View.GONE
    }

    private fun showContent(playlists: List<PlayList>) {
        playlistAdapter.playlists.clear()
        playlistAdapter.playlists.addAll(playlists)
        playlistAdapter.notifyDataSetChanged()

        binding.recyclerPlaylistsInPLayer.visibility = View.VISIBLE
    }

    private fun clickDebounce(): Boolean {
        val current = isPlaylistClickAllowed
        if (isPlaylistClickAllowed) {
            isPlaylistClickAllowed = false
            lifecycleScope.launch {
                delay(CLICK_DEBOUNCE_DELAY)
                isPlaylistClickAllowed = true
            }
        }
        return current
    }

    private fun addTrackInPlaylist(playlist: PlayList) {
        if (clickDebounce()) {
            viewModel.addTrackInPlaylist(playlist, track)
        }
    }

    private fun updateFavoriteButton(isFavorite: Boolean) {
        if (isFavorite) {
            binding.likeButton.setImageResource(R.drawable.button_was_liked)
        } else {
            binding.likeButton.setImageResource(R.drawable.button_liked_track)
        }
    }
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putCharSequence(PLAY_TIME, binding.trackTime.text)
    }

    override fun onResume() {
        super.onResume()
        viewModel.pausePlayer()
    }

    private fun convertToYear(releaseDate: String?): String? {
        return Changer.convertToYear(releaseDate)
    }


    companion object {
        const val PLAY_TIME = "PLAY_TIME"
        private const val CLICK_DEBOUNCE_DELAY = 1000L
        private const val TRACK_INFO = "track"
    }

}