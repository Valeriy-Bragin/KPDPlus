package com.meriniguan.kpdplus.screens.addedittool

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.github.dhaval2404.imagepicker.ImagePicker
import com.meriniguan.kpdplus.R
import com.meriniguan.kpdplus.databinding.FragmentAddEditToolBinding
import com.meriniguan.kpdplus.utils.exhaustive
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddEditToolFragment : Fragment(R.layout.fragment_add_edit_tool) {

    private val viewModel: AddEditToolViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = FragmentAddEditToolBinding.bind(view)

        binding.apply {
            editTextName.setText(viewModel.name)
            editTextBrand.setText(viewModel.brand)
            editTextHolderName.setText(viewModel.holderName)
            editTextCode.setText(viewModel.code)
            if ((viewModel.photoUri != "empty") && (viewModel.photoUri.isNotEmpty())) {
                Glide.with(root.context)
                    .load(viewModel.photoUri)
                    .into(imageViewTool)
            } else {
                imageViewTool.setImageResource(R.drawable.default_image)
            }

            editTextName.addTextChangedListener {
                viewModel.name = it.toString()
                if (it.toString().isNotBlank()) {
                    textInputName.error = null
                }
            }
            editTextBrand.addTextChangedListener {
                viewModel.brand = it.toString()
            }
            editTextHolderName.addTextChangedListener {
                viewModel.holderName = it.toString()
            }
            editTextCode.addTextChangedListener {
                viewModel.code = it.toString()
            }

            imageViewTool.setOnClickListener {
                viewModel.onPhotoClick()
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.addEditToolEventFlow.collect { event ->
                when (event) {
                    AddEditToolViewModel.AddEditToolEvent.ShowSelectMethodOfTakingImageScreen -> {
                        val pictureDialog = AlertDialog.Builder(requireContext())
                        pictureDialog.setTitle(getString(R.string.select_action))
                        val pictureDialogItem = arrayOf(getString(R.string.select_photo_from_gallery),
                            getString(R.string.capture_photo_from_camera))
                        pictureDialog.setItems(pictureDialogItem) { dialog, which ->

                            when (which) {
                                0 -> pickImageFromGallery()
                                1 -> captureImageWithCamera()
                            }
                        }

                        pictureDialog.show()
                    }
                    is AddEditToolViewModel.AddEditToolEvent.SetUriToImageViewTool -> {
                        binding.imageViewTool.setImageURI(event.uri)
                    }
                    is AddEditToolViewModel.AddEditToolEvent.NavigateBackWithResult -> {
                        binding.editTextName.clearFocus()
                        binding.editTextBrand.clearFocus()
                        binding.editTextHolderName.clearFocus()
                        binding.editTextCode.clearFocus()
                        setFragmentResult(
                            "add_edit_request",
                            bundleOf("add_edit_result" to event.result)
                        )
                        findNavController().popBackStack()
                    }
                    is AddEditToolViewModel.AddEditToolEvent.ShowMessage -> {
                        binding.textInputName.error = getString(event.msgRes)
                    }
                }.exhaustive
            }
        }

        setHasOptionsMenu(true)
    }

    private fun pickImageFromGallery() {
        ImagePicker.with(this)
            .galleryOnly()
            .start()
    }

    private fun captureImageWithCamera() {
        ImagePicker.with(this)
            .cameraOnly()
            .start()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {

            when (requestCode) {
                ImagePicker.REQUEST_CODE -> {
                    val uri: Uri = data?.data!!
                    viewModel.onPhotoSelected(uri)
                }
            }

        }

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_fragment_add_edit_tool, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_done -> {
                viewModel.onDoneClick()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}