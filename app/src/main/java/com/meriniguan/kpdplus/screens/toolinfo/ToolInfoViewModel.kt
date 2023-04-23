package com.meriniguan.kpdplus.screens.toolinfo

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.meriniguan.kpdplus.EDIT_TOOL_RESULT_OK
import com.meriniguan.kpdplus.R
import com.meriniguan.kpdplus.data.room.Tool
import com.meriniguan.kpdplus.data.room.ToolDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ToolInfoViewModel @Inject constructor(
    private val toolDao: ToolDao,
    private val state: SavedStateHandle
) : ViewModel() {

    var tool = toolDao.getToolByCode(
        state.get<String>("code") ?: state.get<Tool>("tool")?.code
        ?: throw IllegalStateException("Cannot find tool")
    ).asLiveData()

    private val toolInfoEventChannel = Channel<ToolInfoEvent>()
    val toolInfoEventFlow = toolInfoEventChannel.receiveAsFlow()

    fun onEditClick() = viewModelScope.launch {
        toolInfoEventChannel.send(ToolInfoEvent.NavigateToEditToolScreen(R.string.edit_tool, tool.value!!))
    }

    fun onEditResult(result: Int) {
        when (result) {
            EDIT_TOOL_RESULT_OK -> showToolAddedConfirmationMessage(R.string.tool_edited)
        }
    }

    private fun showToolAddedConfirmationMessage(msgRes: Int) = viewModelScope.launch {
        toolInfoEventChannel.send(ToolInfoEvent.ShowToolEditedConfirmationMessage(msgRes))
    }

    sealed class ToolInfoEvent() {
        data class NavigateToEditToolScreen(val titleRes: Int, val tool: Tool) : ToolInfoEvent()
        data class ShowToolEditedConfirmationMessage(val msgRes: Int) : ToolInfoEvent()
    }
}