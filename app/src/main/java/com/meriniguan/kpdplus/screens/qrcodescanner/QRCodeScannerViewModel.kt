package com.meriniguan.kpdplus.screens.qrcodescanner

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meriniguan.kpdplus.R
import com.meriniguan.kpdplus.data.room.ToolDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QRCodeScannerViewModel @Inject constructor(
    private val toolDao: ToolDao,
    state: SavedStateHandle
) : ViewModel() {

    private val isAdding = state.get<Boolean>("isAdding") ?: true

    private val qrCodeScannerEventChannel = Channel<QRCodeScannerEvent>()
    val qrCodeScannerEventFlow = qrCodeScannerEventChannel.receiveAsFlow()

    fun onDecoded(code: String) = viewModelScope.launch {
        val id = toolDao.findToolByCode(code)
        if (isAdding) {
            if (id == null) {
                qrCodeScannerEventChannel.send(QRCodeScannerEvent.NavigateToAddToolScreen(R.string.new_tool, code))
            } else {
                qrCodeScannerEventChannel.send(QRCodeScannerEvent.ShowMessage(R.string.there_already_is_a_tool_with_this_code))
            }
        } else {
            if (id != null) {
                qrCodeScannerEventChannel.send(QRCodeScannerEvent.NavigateToToolInfoScreen(code))
            } else {
                qrCodeScannerEventChannel.send(QRCodeScannerEvent.ShowMessage(R.string.there_is_no_tool_with_this_code))
            }
        }
    }

    sealed class QRCodeScannerEvent {
        data class NavigateToAddToolScreen(val titleRes: Int, val code: String) : QRCodeScannerEvent()
        data class NavigateToToolInfoScreen(val code: String) : QRCodeScannerEvent()
        data class ShowMessage(val msgRes: Int) : QRCodeScannerEvent()
    }
}