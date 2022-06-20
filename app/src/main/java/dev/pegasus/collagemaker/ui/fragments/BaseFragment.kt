package dev.pegasus.collagemaker.ui.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import dev.pegasus.collagemaker.MainActivity

open class BaseFragment<T : ViewDataBinding> : Fragment() {

    private var _binding: T? = null
    val binding get() = _binding!!
    lateinit var globalContext: Context
    lateinit var globalActivity: Activity
    lateinit var mainActivity: Activity
    private var layoutId: Int = 0

    fun getView(inflater: LayoutInflater, container: ViewGroup?, layout: Int): View {
        _binding = DataBindingUtil.inflate(inflater, layout, container, false)
        layoutId = layout
        globalContext = binding.root.context
        globalActivity = globalContext as Activity
        mainActivity = globalActivity as MainActivity
        return binding.root
    }

    @SuppressLint("SourceLockedOrientationActivity")
    fun lockRotation() {
        globalActivity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
    }

    fun unlockRotation() {
        globalActivity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR;
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}