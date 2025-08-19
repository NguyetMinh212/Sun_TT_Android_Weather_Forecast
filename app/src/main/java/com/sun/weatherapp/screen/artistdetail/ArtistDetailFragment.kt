package com.sun.weatherapp.screen.artistdetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.sun.weatherapp.R
import com.sun.weatherapp.data.model.Artist
import com.sun.weatherapp.data.model.Song
import com.sun.weatherapp.databinding.FragmentArtistDetailBinding
import com.sun.weatherapp.screen.base.BaseFragment
import com.sun.weatherapp.screen.music.adapter.SongAdapter
import com.sun.weatherapp.utils.ImageLoader

class ArtistDetailFragment : BaseFragment<FragmentArtistDetailBinding, ArtistDetailPresenter>(), ArtistDetailContract.View {
    
    private lateinit var songAdapter: SongAdapter
    private var currentArtist: Artist? = null
    
    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentArtistDetailBinding {
        return FragmentArtistDetailBinding.inflate(inflater, container, false)
    }
    
    override fun initializePresenter() {
        presenter = ArtistDetailPresenter()
        presenter?.attachView(this)
    }
    
    override fun setupViews() {
        setupToolbar()
        setupRecyclerView()
        loadArtistFromArguments()
    }
    
    override fun setupListeners() {
        // Toolbar navigation
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }
    
    private fun setupToolbar() {
        binding.toolbar.title = "Artist Detail"
    }
    
    private fun setupRecyclerView() {
        songAdapter = SongAdapter { song ->
            presenter?.onSongClicked(song)
        }
        
        binding.rvSongs.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = songAdapter
        }
    }
    
    private fun loadArtistFromArguments() {
        arguments?.getParcelable<Artist>("artist")?.let { artist ->
            currentArtist = artist
            presenter?.loadArtistDetail(artist)
        } ?: run {
            showError("Không tìm thấy thông tin nghệ sĩ")
            findNavController().navigateUp()
        }
    }
    
    override fun showArtistInfo(artist: Artist) {
        binding.apply {
            tvArtistName.text = artist.name
            tvSongCount.text = "${artist.songCount} songs"
            
            // Load artist image
            ImageLoader.loadArtistImage(artist.imageUrl, ivArtistImage)
        }
    }
    
    override fun showSongs(songs: List<Song>) {
        songAdapter.submitList(songs)
    }
    
    override fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
    }
    
    override fun hideLoading() {
        binding.progressBar.visibility = View.GONE
    }
    
    override fun showEmptyState() {
        binding.emptyState.visibility = View.VISIBLE
    }
    
    override fun hideEmptyState() {
        binding.emptyState.visibility = View.GONE
    }
    
    override fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
    
    override fun navigateToPlayingMusic(song: Song, playlist: List<Song>) {
        val bundle = Bundle().apply {
            putParcelable("song", song)
            putParcelableArrayList("playlist", ArrayList(playlist))
        }
        findNavController().navigate(R.id.playing_music_fragment, bundle)
    }
}
