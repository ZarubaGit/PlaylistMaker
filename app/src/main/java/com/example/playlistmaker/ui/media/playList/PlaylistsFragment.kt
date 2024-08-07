package com.example.playlistmaker.ui.media.playList

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.FragmentPlaylistsBinding
import com.example.playlistmaker.domain.models.PlayList
import com.example.playlistmaker.ui.media.addPlayList.AddPlayListAdapter
import com.example.playlistmaker.ui.media.playListDetails.PlaylistDetailsFragment
import com.example.playlistmaker.ui.media.playListFragmentViewModel.PlayListFragmentViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class PlaylistsFragment : Fragment() {
    private lateinit var binding: FragmentPlaylistsBinding
    private val viewModel: PlayListFragmentViewModel by viewModel()

    private val playlistAdapter = AddPlayListAdapter{
        switchToPlayList(it)
    }

    private var isClickAllowed = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPlaylistsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.observePlaylistState().observe(viewLifecycleOwner) {
            render(it)
        }

        binding.recyclerPlaylists.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.recyclerPlaylists.adapter = playlistAdapter

        binding.createPlayListButton.setOnClickListener {
            findNavController().navigate(R.id.action_mediatekaFragment_to_addPlayListFragment)
        }
    }

    private fun render(state: PlayListState) {
        when(state) {
            is PlayListState.Empty -> showEmpty()
            is PlayListState.Content -> showContent(state.playlists)
        }
    }

    private fun showEmpty() {
        binding.recyclerPlaylists.visibility = View.GONE
        binding.emptyMediaPlayList.visibility = View.VISIBLE
        binding.emptyMediaPlayListText.visibility = View.VISIBLE
    }

    private fun showContent(playlists: List<PlayList>) {
        binding.emptyMediaPlayList.visibility = View.GONE
        binding.emptyMediaPlayListText.visibility = View.GONE

        playlistAdapter.playlists.clear()
        playlistAdapter.playlists.addAll(playlists)
        playlistAdapter.notifyDataSetChanged()

        binding.recyclerPlaylists.visibility = View.VISIBLE
    }

    override fun onResume() {
        super.onResume()
        viewModel.getPlaylists()
        isClickAllowed = true
    }

    private fun clickDebounce(): Boolean {
        val current = isClickAllowed
        if (isClickAllowed) {
            isClickAllowed = false
            viewLifecycleOwner.lifecycleScope.launch {
                delay(CLICK_DEBOUNCE_DELAY)
                isClickAllowed = true
            }
        }
        return current
    }

    private fun switchToPlayList(playlist: PlayList) {
        if (clickDebounce()) {
            findNavController().navigate(
                R.id.action_mediatekaFragment_to_playlistDetailsFragment,
                PlaylistDetailsFragment.createArgs(playlist.id)
            )
        }
    }

    companion object {

        private const val CLICK_DEBOUNCE_DELAY = 1000L

        fun newInstance(): PlaylistsFragment{
            return PlaylistsFragment()
        }
    }
}