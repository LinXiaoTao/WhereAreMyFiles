package com.kewei.files.ui.search

import android.Manifest
import android.app.Application
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.kewei.files.databinding.FragmentSearchBinding
import com.kewei.files.util.DisplayUtil.dpToPx
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.RuntimePermissions

@RuntimePermissions
class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val viewModel: SearchViewModel by viewModels(
        { this },
        { ViewModelProvider.AndroidViewModelFactory(requireContext().applicationContext as Application) }
    )

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private val historyAdapter = HistoryAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        onRequestPermissionsResult(requestCode, grantResults)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.listHistory.adapter = historyAdapter
        binding.listHistory.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(
                outRect: Rect,
                view: View,
                parent: RecyclerView,
                state: RecyclerView.State
            ) {
                super.getItemOffsets(outRect, view, parent, state)
                outRect.bottom = 10.dpToPx().toInt()
            }
        })
        viewModel.historyLiveData.observe(viewLifecycleOwner) { historyModelList ->
            historyAdapter.submitList(historyModelList)
        }
        queryHistoryWithPermissionCheck()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    @NeedsPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
    fun queryHistory() {
        viewModel.queryHistory()
    }
}