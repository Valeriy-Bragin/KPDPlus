package com.meriniguan.kpdplus.screens.tools

import androidx.lifecycle.*
import com.meriniguan.kpdplus.ADD_TOOL_RESULT_OK
import com.meriniguan.kpdplus.R
import com.meriniguan.kpdplus.data.preferences.PreferencesManager
import com.meriniguan.kpdplus.data.preferences.ShowSetting
import com.meriniguan.kpdplus.data.preferences.SortOrder
import com.meriniguan.kpdplus.data.room.Tool
import com.meriniguan.kpdplus.data.room.ToolDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ToolsViewModel @Inject constructor(
    private val toolDao: ToolDao,
    private val preferencesManager: PreferencesManager,
    state: SavedStateHandle
) : ViewModel() {

    val searchQuery = state.getLiveData("searchQuery", "")

    private val preferencesFlow = preferencesManager.preferencesFlow

    private val toolsEventChannel = Channel<ToolsEvent>()
    val toolsEventFlow = toolsEventChannel.receiveAsFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    private val toolsFlow = combine(
        searchQuery.asFlow(),
        preferencesFlow
    ) { searchQuery, filterPreferences ->
        Pair(searchQuery, filterPreferences)
    }.flatMapLatest { (searchQuery, filterPreferences) ->
        toolDao.getTools(searchQuery, filterPreferences.sortOrder, filterPreferences.showSetting)
    }

    val tools = toolsFlow.asLiveData()

    val toolsCount = toolDao.getRowCount().asLiveData()

    fun onAddToolClick() = viewModelScope.launch {
        toolsEventChannel.send(ToolsEvent.NavigateToQRCodeScannerScreen(true))
    }

    fun onScanQRCodeClick() = viewModelScope.launch {
        toolsEventChannel.send(ToolsEvent.NavigateToQRCodeScannerScreen(false))
    }

    fun onToolItemClick(tool: Tool) = viewModelScope.launch {
        toolsEventChannel.send(ToolsEvent.NavigateToToolInfoScreen(tool))
    }

    fun onToolSwiped(tool: Tool) = viewModelScope.launch {
        toolDao.delete(tool)
    }

    fun onSortOrderSelected(sortOrder: SortOrder) = viewModelScope.launch {
        preferencesManager.updateSortOrder(sortOrder)
    }

    fun onShowSettingSelected(showSetting: ShowSetting) = viewModelScope.launch {
        preferencesManager.updateShowSetting(showSetting)
    }

    fun onAddResult(result: Int) {
        when (result) {
            ADD_TOOL_RESULT_OK -> showToolAddedConfirmationMessage(R.string.tool_added)
        }
    }

    private fun showToolAddedConfirmationMessage(msgRes: Int) = viewModelScope.launch {
        toolsEventChannel.send(ToolsEvent.ShowToolAddedConfirmationMessage(msgRes))
    }

    sealed class ToolsEvent {
        data class NavigateToQRCodeScannerScreen(val isAdding: Boolean): ToolsEvent()
        data class NavigateToToolInfoScreen(val tool: Tool): ToolsEvent()
        data class ShowToolAddedConfirmationMessage(val msgRes: Int): ToolsEvent()
    }

}