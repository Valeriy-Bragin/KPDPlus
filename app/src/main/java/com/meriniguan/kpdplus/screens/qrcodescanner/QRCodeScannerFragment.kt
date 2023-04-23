package com.meriniguan.kpdplus.screens.qrcodescanner

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.budiyev.android.codescanner.*
import com.google.android.material.snackbar.Snackbar
import com.meriniguan.kpdplus.R
import com.meriniguan.kpdplus.utils.exhaustive
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class QRCodeScannerFragment : Fragment(R.layout.fragment_qr_code_scanner) {

    private val viewModel: QRCodeScannerViewModel by viewModels()

    private lateinit var codeScanner: CodeScanner
    private lateinit var scannerView: CodeScannerView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        scannerView = view.findViewById(R.id.scanner_view)

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_DENIED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_REQUEST_CODE
            )
        } else {
            startScanning()
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.qrCodeScannerEventFlow.collect { event ->
                when (event) {
                    is QRCodeScannerViewModel.QRCodeScannerEvent.NavigateToAddToolScreen -> {
                        val action = QRCodeScannerFragmentDirections
                            .actionQRCodeScannerFragmentToAddEditToolFragment(title = getString(event.titleRes), code = event.code)
                        findNavController().navigate(action)
                    }
                    is QRCodeScannerViewModel.QRCodeScannerEvent.NavigateToToolInfoScreen -> {
                        val action = QRCodeScannerFragmentDirections
                            .actionQRCodeScannerFragmentToToolInfoFragment(code = event.code)
                        findNavController().navigate(action)
                    }
                    is QRCodeScannerViewModel.QRCodeScannerEvent.ShowMessage -> {
                        Snackbar.make(requireView(), event.msgRes, Snackbar.LENGTH_SHORT).show()
                    }
                }.exhaustive
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (::codeScanner.isInitialized) {
            codeScanner?.startPreview()
        }
    }

    override fun onPause() {
        if (::codeScanner.isInitialized) {
            codeScanner?.releaseResources()
        }
        super.onPause()
    }

    private fun startScanning() {
        val activity = requireActivity()
        codeScanner = CodeScanner(activity, scannerView)
        codeScanner.camera = CodeScanner.CAMERA_BACK
        codeScanner.formats = CodeScanner.ALL_FORMATS

        codeScanner.autoFocusMode = AutoFocusMode.SAFE
        codeScanner.scanMode = ScanMode.SINGLE
        codeScanner.isAutoFocusEnabled = true
        codeScanner.isFlashEnabled = false

        codeScanner.decodeCallback = DecodeCallback {
            activity.runOnUiThread {
                viewModel.onDecoded(it.text)
            }
        }
        scannerView.setOnClickListener {
            codeScanner.startPreview()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(requireContext(), R.string.camera_permission_granted, Toast.LENGTH_SHORT)
                    .show()
                startScanning()
            } else {
                Toast.makeText(requireContext(), R.string.camera_permission_denied, Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    companion object {
        private const val CAMERA_PERMISSION_REQUEST_CODE = 141
    }
}