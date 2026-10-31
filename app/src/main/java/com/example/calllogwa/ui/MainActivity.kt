package com.example.calllogwa.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.calllogwa.data.CallLogRepository
import com.example.calllogwa.databinding.ActivityMainBinding
import com.example.calllogwa.util.WhatsAppLauncher
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val viewModel: CallLogViewModel by viewModels {
        CallLogViewModelFactory(CallLogRepository(applicationContext))
    }

    private lateinit var adapter: CallLogAdapter

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val readCallLogGranted = permissions[Manifest.permission.READ_CALL_LOG] ?: false
        val readContactsGranted = permissions[Manifest.permission.READ_CONTACTS] ?: false

        if (readCallLogGranted) {
            showCallLogUi()
            viewModel.loadCallLogs()
        } else {
            showPermissionOverlay()
            Toast.makeText(this, "Call Log permission is required to view recent calls", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupListeners()
        observeViewModel()

        checkAndRequestPermissions()
    }

    private fun setupRecyclerView() {
        adapter = CallLogAdapter { entry ->
            WhatsAppLauncher.openWhatsAppChat(this, entry.number)
        }
        binding.rvCallLogs.layoutManager = LinearLayoutManager(this)
        binding.rvCallLogs.adapter = adapter
    }

    private fun setupListeners() {
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.loadCallLogs()
        }

        binding.btnRefresh.setOnClickListener {
            viewModel.loadCallLogs()
        }

        binding.btnGrantPermission.setOnClickListener {
            checkAndRequestPermissions()
        }

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.onSearchQueryChanged(s?.toString() ?: "")
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.callLogs.collect { logs ->
                        adapter.submitList(logs)
                        if (logs.isEmpty() && !viewModel.isLoading.value) {
                            binding.tvEmptyState.visibility = View.VISIBLE
                        } else {
                            binding.tvEmptyState.visibility = View.GONE
                        }
                    }
                }

                launch {
                    viewModel.isLoading.collect { loading ->
                        binding.swipeRefresh.isRefreshing = loading
                    }
                }
            }
        }
    }

    private fun checkAndRequestPermissions() {
        val readCallLogGranted = ContextCompat.checkSelfPermission(
            this, Manifest.permission.READ_CALL_LOG
        ) == PackageManager.PERMISSION_GRANTED

        val readContactsGranted = ContextCompat.checkSelfPermission(
            this, Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED

        if (readCallLogGranted) {
            showCallLogUi()
            viewModel.loadCallLogs()
        } else {
            showPermissionOverlay()
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.READ_CALL_LOG,
                    Manifest.permission.READ_CONTACTS
                )
            )
        }
    }

    private fun showCallLogUi() {
        binding.layoutPermission.visibility = View.GONE
        binding.swipeRefresh.visibility = View.VISIBLE
        binding.cvSearch.visibility = View.VISIBLE
    }

    private fun showPermissionOverlay() {
        binding.layoutPermission.visibility = View.VISIBLE
        binding.swipeRefresh.visibility = View.GONE
        binding.cvSearch.visibility = View.GONE
        binding.tvEmptyState.visibility = View.GONE
    }
}
