package com.meriniguan.kpdplus.screens.toolinfo

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.meriniguan.kpdplus.R
import com.meriniguan.kpdplus.databinding.FragmentToolInfoBinding
import com.meriniguan.kpdplus.utils.exhaustive
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ToolInfoFragment : Fragment(R.layout.fragment_tool_info) {

    private val viewModel: ToolInfoViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = FragmentToolInfoBinding.bind(view)

        viewModel.tool.observe(viewLifecycleOwner) { tool ->
            binding.apply {
                textViewName.text = tool.name
                textViewBrand.text = tool.brand
                textViewHolderName.text = tool.holderName
                textViewCode.text = tool.code
                textViewDateCreated.text = tool.dateCreatedFormatted

                if (tool.hasPhoto()) {
                    Glide.with(root.context)
                        .load(tool.photoUri)
                        .into(imageViewTool)
                } else {
                    imageViewTool.setImageResource(R.drawable.default_image)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.toolInfoEventFlow.collect { event ->
                when (event) {
                    is ToolInfoViewModel.ToolInfoEvent.NavigateToEditToolScreen -> {
                        val action = ToolInfoFragmentDirections
                            .actionToolInfoFragmentToAddEditToolFragment(title = getString(event.titleRes), tool = event.tool)
                        findNavController().navigate(action)
                    }
                    is ToolInfoViewModel.ToolInfoEvent.ShowToolEditedConfirmationMessage -> {
                        Snackbar.make(requireView(), event.msgRes, Snackbar.LENGTH_SHORT).show()
                    }
                }.exhaustive
            }
        }

        setFragmentResultListener("add_edit_request") { _, bundle ->
            val result = bundle.getInt("add_edit_result")
            viewModel.onEditResult(result)
        }

        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_fragment_tool_info, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_edit -> {
                viewModel.onEditClick()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}