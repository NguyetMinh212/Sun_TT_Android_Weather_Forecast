package com.sun.weatherapp.screen.music

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.sun.weatherapp.data.model.Artist
import com.sun.weatherapp.data.model.MusicTabType
import com.sun.weatherapp.data.model.Song
import com.sun.weatherapp.databinding.FragmentMusicBinding
import com.sun.weatherapp.screen.base.BaseFragment
import com.sun.weatherapp.screen.music.adapter.ArtistAdapter
import com.sun.weatherapp.screen.music.adapter.SongAdapter
import android.os.Handler
import android.os.Looper

class MusicFragment : BaseFragment<FragmentMusicBinding, MusicPresenter>(), MusicContract.View {

    private lateinit var songAdapter: SongAdapter
    private lateinit var artistAdapter: ArtistAdapter
    
    private var currentTab = MusicTabType.RECOMMEND
    private var isInitialized = false

    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentMusicBinding {
        return FragmentMusicBinding.inflate(inflater, container, false)
    }

    override fun initializePresenter() {
        presenter = MusicPresenter()
    }

    override fun setupViews() {
        presenter?.attachView(this)
        setupRecyclerViews()
        setupTabs()
        isInitialized = true
        
        // Load initial data
        presenter?.loadWeatherInfo()
        presenter?.loadMusicData(MusicTabType.RECOMMEND)
    }

    override fun setupListeners() {
        binding.apply {
            tvTabRecommend.setOnClickListener {
                if (currentTab != MusicTabType.RECOMMEND) {
                    showLoading()
                    clearAdapterData()
                    presenter?.onTabSelected(MusicTabType.RECOMMEND)
                }
            }
            
            tvTabArtist.setOnClickListener {
                if (currentTab != MusicTabType.ARTIST) {
                    showLoading()
                    clearAdapterData()
                    presenter?.onTabSelected(MusicTabType.ARTIST)
                }
            }
            
            tvTabAllSongs.setOnClickListener {
                if (currentTab != MusicTabType.ALL_SONGS) {
                    showLoading()
                    clearAdapterData()
                    presenter?.onTabSelected(MusicTabType.ALL_SONGS)
                }
            }
        }
    }

    private fun setupRecyclerViews() {
        songAdapter = SongAdapter { song ->
            presenter?.onSongClicked(song)
        }
        
        binding.rvPlaylists.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = songAdapter
        }
        binding.rvSongs.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = songAdapter
        }

        artistAdapter = ArtistAdapter { artist ->
            presenter?.onArtistClicked(artist)
        }
        binding.rvArtists.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = artistAdapter
        }
    }

    private fun setupTabs() {
        updateTabSelection(MusicTabType.RECOMMEND)
    }

    private fun updateTabSelection(selectedTab: MusicTabType) {
        if (!isInitialized && currentTab == selectedTab) return
        
        binding.apply {
            tvTabRecommend.apply {
                setBackgroundResource(com.sun.weatherapp.R.drawable.bg_tab_unselected)
                setTypeface(null, Typeface.NORMAL)
            }
            tvTabArtist.apply {
                setBackgroundResource(com.sun.weatherapp.R.drawable.bg_tab_unselected)
                setTypeface(null, Typeface.NORMAL)
            }
            tvTabAllSongs.apply {
                setBackgroundResource(com.sun.weatherapp.R.drawable.bg_tab_unselected)
                setTypeface(null, Typeface.NORMAL)
            }

            when (selectedTab) {
                MusicTabType.RECOMMEND -> {
                    tvTabRecommend.apply {
                        setBackgroundResource(com.sun.weatherapp.R.drawable.bg_tab_selected)
                        setTypeface(null, Typeface.BOLD)
                    }
                }
                MusicTabType.ARTIST -> {
                    tvTabArtist.apply {
                        setBackgroundResource(com.sun.weatherapp.R.drawable.bg_tab_selected)
                        setTypeface(null, Typeface.BOLD)
                    }
                }
                MusicTabType.ALL_SONGS -> {
                    tvTabAllSongs.apply {
                        setBackgroundResource(com.sun.weatherapp.R.drawable.bg_tab_selected)
                        setTypeface(null, Typeface.BOLD)
                    }
                }
            }
        }
    }

    private fun showContentLayout(layoutType: MusicTabType) {
        binding.apply {
            val targetLayout = when (layoutType) {
                MusicTabType.RECOMMEND -> layoutPlaylists
                MusicTabType.ARTIST -> layoutArtists
                MusicTabType.ALL_SONGS -> layoutSongs
            }
            
            if (targetLayout.visibility != View.VISIBLE) {
                listOf(layoutPlaylists, layoutArtists, layoutSongs).forEach { layout ->
                    if (layout.visibility == View.VISIBLE) {
                        layout.visibility = View.GONE
                    }
                }
                
                targetLayout.visibility = View.VISIBLE
            }
        }
    }

    private fun clearAdapterData() {
        songAdapter.submitList(emptyList())
        artistAdapter.submitList(emptyList())
    }

    override fun showSongs(songs: List<Song>) {
        songAdapter.submitList(songs)
    }

    override fun showArtists(artists: List<Artist>) {
        artistAdapter.submitList(artists)
    }

    override fun updateSelectedTab(tabType: MusicTabType) {
        if (currentTab == tabType && isInitialized) return
        currentTab = tabType
        updateTabSelection(tabType)
        showContentLayout(tabType)
    }

    override fun showWeatherInfo(location: String, temperature: String) {
        binding.apply {
            tvLocation.text = location
            tvTemperature.text = temperature
        }
    }

    override fun showLoading() {
        binding.progressLoading.visibility = View.VISIBLE
    }

    override fun hideLoading() {
        Handler(Looper.getMainLooper()).postDelayed({
            binding.progressLoading.visibility = View.GONE
        }, 100)
    }

    override fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}
