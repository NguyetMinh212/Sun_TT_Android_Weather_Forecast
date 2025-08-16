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

class PlayingMusicFragment : BaseFragment<FragmentPlayingMusicBinding, PlayingMusicPresenter>(), PlayingMusicContract.View {
    
    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentPlayingMusicBinding {
        return FragmentPlayingMusicBinding.inflate(inflater, container, false)
    }
    
    override fun initializePresenter() {
        presenter = PlayingMusicPresenter()
    }
    
    override fun setupViews() {
        presenter?.attachView(this)
        
        // Load song from arguments
        arguments?.getParcelable<Song>("song")?.let { song ->
            presenter?.loadSong(song)
        }
    }
    
    override fun setupListeners() {
        binding.apply {
            // Back button
            ivBack.setOnClickListener {
                findNavController().popBackStack()
            }
            
            // Play/Pause button
            ivPlayPause.setOnClickListener {
                presenter?.onPlayPauseClicked()
            }
            
            // Previous button
            ivPrevious.setOnClickListener {
                presenter?.onPreviousClicked()
            }
            
            // Next button
            ivNext.setOnClickListener {
                presenter?.onNextClicked()
            }
            
            // Shuffle button
            ivShuffle.setOnClickListener {
                presenter?.onShuffleClicked()
            }
            
            // Repeat button
            ivRepeat.setOnClickListener {
                presenter?.onRepeatClicked()
            }
            
            // Seek bar
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
            // Load song cover image using Glide
            // Glide.with(this@PlayingMusicFragment)
            //     .load(song.imageUrl)
            //     .placeholder(R.drawable.bg_image_placeholder)
            //     .into(ivSongCover)
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
