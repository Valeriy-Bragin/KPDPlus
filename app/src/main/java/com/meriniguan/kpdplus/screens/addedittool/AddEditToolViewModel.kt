package com.meriniguan.kpdplus.screens.addedittool

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meriniguan.kpdplus.ADD_TOOL_RESULT_OK
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
class AddEditToolViewModel @Inject constructor(
    private val toolDao: ToolDao,
    private val state: SavedStateHandle
) : ViewModel() {

    val tool = state.get<Tool>("tool")

    var code = state.get<String>("code") ?: tool?.code ?: ""
        set(value) {
            field = value
            state["code"] = value
        }

    var name = state.get<String>("name") ?: tool?.name ?: ""
        set(value) {
            field = value
            state["name"] = value
        }

    var brand = state.get<String>("brand") ?: tool?.brand ?: ""
        set(value) {
            field = value
            state["brand"] = value
        }

    var holderName = state.get<String>("holder_name") ?: tool?.holderName ?: ""
        set(value) {
            field = value
            state["holder_name"] = value
        }

    var photoUri = state.get<String>("photo_uri") ?: tool?.photoUri ?: "empty"
        set(value) {
            field = value
            state["photo_uri"] = value
        }

    private val addEditToolEventChannel = Channel<AddEditToolEvent>()
    val addEditToolEventFlow = addEditToolEventChannel.receiveAsFlow()

    fun onPhotoClick() = viewModelScope.launch {
        addEditToolEventChannel.send(AddEditToolEvent.ShowSelectMethodOfTakingImageScreen)
    }

    fun onPhotoSelected(uri: Uri) = viewModelScope.launch {
        photoUri = uri.toString()
        addEditToolEventChannel.send(AddEditToolEvent.SetUriToImageViewTool(uri))
    }

    fun onDoneClick() {
        if (name.isBlank()) {
            showBlankNameWarningMessage()
            return
        }
        if (isAdding()) {
            addTool()
        } else {
            editTool()
        }
    }

    private fun showBlankNameWarningMessage() = viewModelScope.launch {
        addEditToolEventChannel.send(AddEditToolEvent.ShowMessage(R.string.tool_name_cannot_be_blank))
    }

    private fun addTool() = viewModelScope.launch {
        addEditToolEventChannel.send(AddEditToolEvent.NavigateBackWithResult(ADD_TOOL_RESULT_OK))
        toolDao.insert(
            Tool(
                name = name,
                code = code,
                holderName = holderName,
                brand = brand,
                photoUri = photoUri
            )
        )
    }

    private fun editTool() = viewModelScope.launch {
        addEditToolEventChannel.send(AddEditToolEvent.NavigateBackWithResult(EDIT_TOOL_RESULT_OK))
        toolDao.update(
            tool!!.copy(
                name = name,
                code = code,
                holderName = holderName,
                brand = brand,
                photoUri = photoUri
            )
        )
    }

    private fun isAdding(): Boolean = tool == null

    sealed class AddEditToolEvent {
        object ShowSelectMethodOfTakingImageScreen : AddEditToolEvent()
        data class SetUriToImageViewTool(val uri: Uri) : AddEditToolEvent()
        data class NavigateBackWithResult(val result: Int) : AddEditToolEvent()
        data class ShowMessage(val msgRes: Int) : AddEditToolEvent()
    }
}