package com.sun.weatherapp.screen.profile

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseUser
import com.sun.weatherapp.R
import com.sun.weatherapp.WeatherApplication
import com.sun.weatherapp.data.helper.PreferenceHelper
import com.sun.weatherapp.data.reposiroty.AuthRepository
import com.sun.weatherapp.databinding.FragmentProfileBinding
import com.sun.weatherapp.screen.base.BaseFragment
import com.sun.weatherapp.utils.showChangePasswordDialog
import com.sun.weatherapp.utils.showWarningDialog
import com.sun.weatherapp.utils.ImageLoader

class ProfileFragment : BaseFragment<FragmentProfileBinding, ProfilePresenter>(), ProfileContract.View {

    private lateinit var pref: PreferenceHelper
    private var user: FirebaseUser? = null
    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                presenter?.updateProfileImage(uri.toString())
            }
        }

    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentProfileBinding {
        return FragmentProfileBinding.inflate(inflater, container, false)
    }

    override fun initializePresenter() {
        presenter = ProfilePresenter(AuthRepository())
        presenter?.attachView(this)
    }

    override fun setupViews() {
        pref = WeatherApplication.getInstance().preferenceHelper
        user = presenter?.getProfileInfo()

        binding.apply {
            // Load profile image using ImageLoader
            ImageLoader.loadProfileImage(user?.photoUrl?.toString(), profileImage)

            tvEmail.text = user?.email ?: getString(R.string.no_email)
        }
    }

    override fun setupListeners() {
        binding.apply {
            btnLogout.setOnClickListener {
                showWarningDialog(getString(R.string.logout_confirmation)) {
                    presenter?.logout()
                }
            }

            llDeleteAccount.setOnClickListener {
                showWarningDialog(getString(R.string.delete_account_confirmation)) {
                    presenter?.deleteAccount()
                }
            }

            llChangePassword.setOnClickListener {
                showChangePasswordDialog { newPassword, confirmNewPassword, dialog ->
                    presenter?.changePassword(newPassword, confirmNewPassword, dialog)
                }
            }

            profileImage.setOnClickListener {
                pickImageLauncher.launch("image/*")
            }

            llNotification.setOnClickListener {
                findNavController().navigate(R.id.notify_fragment)
            }
        }
    }

    override fun showLogoutSuccess() {
        Toast.makeText(requireContext(), getString(R.string.logout_success), Toast.LENGTH_SHORT).show()
        findNavController().navigate(R.id.login_fragment)
    }

    override fun showDeleteAccountSuccess() {
        pref.clear()
        Toast.makeText(requireContext(), getString(R.string.delete_account_success), Toast.LENGTH_SHORT).show()
        findNavController().navigate(R.id.login_fragment)
    }

    override fun showChangePasswordSuccess() {
        Toast.makeText(requireContext(), getString(R.string.change_password_success), Toast.LENGTH_SHORT).show()
    }

    override fun showUpdateProfileImageSuccess() {
        Toast.makeText(requireContext(), getString(R.string.update_image_success), Toast.LENGTH_SHORT).show()
        setupViews()
    }
}
