package com.sun.weatherapp.screen.playingmusic

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.sun.weatherapp.R
import com.sun.weatherapp.data.model.Song
import com.sun.weatherapp.databinding.FragmentPlayingMusicBinding
import com.sun.weatherapp.screen.base.BaseFragment
import com.sun.weatherapp.utils.ImageLoader

class PlayingMusicFragment : BaseFragment<FragmentPlayingMusicBinding, PlayingMusicPresenter>(), PlayingMusicContract.View {
    
    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentPlayingMusicBinding {
        return FragmentPlayingMusicBinding.inflate(inflater, container, false)
    }
    
    override fun initializePresenter() {
        presenter = PlayingMusicPresenter()
    }
    
    override fun setupViews() {
        presenter?.attachView(this)
        
        binding.apply {
            seekBarProgress.progress = 0
            tvCurrentTime.text = "0:00"
            tvTotalDuration.text = "0:00"
        }
        
        presenter?.bindService(requireContext())
        
        // Load song and playlist from arguments
        arguments?.getParcelable<Song>("song")?.let { song ->
            val playlist = arguments?.getParcelableArrayList<Song>("playlist") ?: emptyList()
            android.util.Log.d("PlayingMusicFragment", "Loading song: ${song.title}, playlist size: ${playlist.size}")
            
            if (playlist.isNotEmpty()) {
                presenter?.loadSongWithPlaylist(song, playlist)
            } else {
                presenter?.loadSong(song)
            }
            // Initially show playing state since song will auto-play
            showPlayingState()
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        presenter?.unbindService(requireContext())
        _binding = null
    }
    
    override fun onPause() {
        super.onPause()
        presenter?.onPlayPauseClicked()
    }
    
    override fun setupListeners() {
        binding.apply {
            ivBack.setOnClickListener {
                presenter?.stopMusicAndService(requireContext())
                findNavController().popBackStack()
            }
            
            ivPlayPause.setOnClickListener {
                presenter?.onPlayPauseClicked()
            }
            
            ivPrevious.setOnClickListener {
                presenter?.onPreviousClicked()
            }
            
            ivNext.setOnClickListener {
                presenter?.onNextClicked()
            }
            
            ivShuffle.setOnClickListener {
                presenter?.onShuffleClicked()
            }
            
            ivRepeat.setOnClickListener {
                presenter?.onRepeatClicked()
            }
            
            seekBarProgress.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    if (fromUser) {
                        presenter?.onSeekChanged(progress)
                    }
                }
                
                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })
        }
    }
    
    override fun showSongInfo(song: Song) {
        binding.apply {
            tvSongTitle.text = song.title
            tvArtistName.text = song.artist
            
            seekBarProgress.progress = 0
            tvCurrentTime.text = "0:00"
            tvTotalDuration.text = song.duration
            
            // Load song cover image using Glide from Firebase
            ImageLoader.loadSongImage(song.imageUrl, ivSongCover)
        }
    }
    
    override fun updateProgress(currentTime: String, duration: String, progress: Int) {
        binding.apply {
            tvCurrentTime.text = currentTime
            tvTotalDuration.text = duration
            seekBarProgress.progress = progress
        }
    }
    
    override fun showPlayingState() {
        binding.ivPlayPause.setImageResource(R.drawable.ic_pause)
    }
    
    override fun showPausedState() {
        binding.ivPlayPause.setImageResource(R.drawable.ic_play)
    }
    
    override fun updatePlaybackControls(isPlaying: Boolean, isShuffleEnabled: Boolean, isRepeatEnabled: Boolean) {
        binding.apply {
            // Update shuffle button state
            if (isShuffleEnabled) {
                ivShuffle.setColorFilter(ContextCompat.getColor(requireContext(), R.color.primary_color))
            } else {
                ivShuffle.setColorFilter(ContextCompat.getColor(requireContext(), R.color.control_button_color))
            }
            
            // Update repeat button state
            if (isRepeatEnabled) {
                ivRepeat.setColorFilter(ContextCompat.getColor(requireContext(), R.color.primary_color))
            } else {
                ivRepeat.setColorFilter(ContextCompat.getColor(requireContext(), R.color.control_button_color))
            }
        }
    }
}
