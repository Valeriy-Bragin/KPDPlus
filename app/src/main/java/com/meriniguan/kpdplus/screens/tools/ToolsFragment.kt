package com.meriniguan.kpdplus.screens.tools

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.meriniguan.kpdplus.R
import com.meriniguan.kpdplus.data.preferences.ShowSetting
import com.meriniguan.kpdplus.data.preferences.SortOrder
import com.meriniguan.kpdplus.data.room.Tool
import com.meriniguan.kpdplus.databinding.FragmentToolsBinding
import com.meriniguan.kpdplus.utils.exhaustive
import com.meriniguan.kpdplus.utils.onQueryTextChanged
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class ToolsFragment : Fragment(R.layout.fragment_tools), ToolsAdapter.OnItemClickListener {

    private val viewModel: ToolsViewModel by viewModels()

    private lateinit var searchView: SearchView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = FragmentToolsBinding.bind(view)
        val toolsAdapter = ToolsAdapter(this)
        val layoutManager = LinearLayoutManager(requireContext())

        binding.apply {
            recyclerViewTools.adapter = toolsAdapter
            recyclerViewTools.layoutManager = layoutManager
            recyclerViewTools.setHasFixedSize(true)
            val dividerItemDecoration = DividerItemDecoration(
                recyclerViewTools.context,
                layoutManager.orientation
            )
            recyclerViewTools.addItemDecoration(dividerItemDecoration)

            fabAddTask.setOnClickListener {
                viewModel.onAddToolClick()
            }

            fabScan.setOnClickListener {
                viewModel.onScanQRCodeClick()
            }
        }

        viewModel.tools.observe(viewLifecycleOwner) {
            toolsAdapter.submitList(it)
        }

        viewModel.toolsCount.observe(viewLifecycleOwner) {
            if (it == 0) {
                binding.textViewNoToolsYet.visibility = View.VISIBLE
            } else {
                binding.textViewNoToolsYet.visibility = View.GONE
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.toolsEventFlow.collect { event ->
                when (event) {
                    is ToolsViewModel.ToolsEvent.NavigateToQRCodeScannerScreen -> {
                        val action = ToolsFragmentDirections.actionToolsFragmentToQRCodeScannerFragment(event.isAdding)
                        findNavController().navigate(action)
                    }
                    is ToolsViewModel.ToolsEvent.NavigateToToolInfoScreen -> {
                        val action = ToolsFragmentDirections.actionToolsFragmentToToolInfoFragment(event.tool)
                        findNavController().navigate(action)
                    }
                    is ToolsViewModel.ToolsEvent.ShowToolAddedConfirmationMessage -> {
                        Snackbar.make(requireView(), event.msgRes, Snackbar.LENGTH_SHORT).show()
                    }
                }.exhaustive
            }
        }

        setFragmentResultListener("add_edit_request") { _, bundle ->
            val result = bundle.getInt("add_edit_result")
            viewModel.onAddResult(result)
        }

        ItemTouchHelper(object :
            ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val tool = toolsAdapter.currentList[viewHolder.adapterPosition]
                viewModel.onToolSwiped(tool)
            }
        }).attachToRecyclerView(binding.recyclerViewTools)

        setHasOptionsMenu(true)
    }

    override fun onItemClick(tool: Tool) {
        viewModel.onToolItemClick(tool)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_fragment_tools, menu)

        val searchItem = menu.findItem(R.id.action_search)
        searchView = searchItem.actionView as SearchView

        val pendingQuery = viewModel.searchQuery.value
        if (pendingQuery != null && pendingQuery.isNotEmpty()) {
            searchItem.expandActionView()
            searchView.setQuery(pendingQuery, false)
        }

        searchView.onQueryTextChanged {
            viewModel.searchQuery.value = it
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_sort_by_name -> {
                viewModel.onSortOrderSelected(SortOrder.BY_NAME)
                true
            }
            R.id.action_sort_by_date_created -> {
                viewModel.onSortOrderSelected(SortOrder.BY_DATE_CREATED)
                true

            }
            R.id.action_show_all -> {
                viewModel.onShowSettingSelected(ShowSetting.ALL)
                true
            }
            R.id.action_show_free -> {
                viewModel.onShowSettingSelected(ShowSetting.FREE)
                true
            }
            R.id.action_show_held -> {
                viewModel.onShowSettingSelected(ShowSetting.HELD)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        searchView.setOnQueryTextListener(null)
    }
}