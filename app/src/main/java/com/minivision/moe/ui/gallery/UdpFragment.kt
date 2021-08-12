package com.minivision.moe.ui.gallery

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.minivision.moe.R

class UdpFragment : Fragment() {

    private lateinit var galleryViewModel: UdpViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        galleryViewModel =
            ViewModelProvider(this).get(UdpViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_udp, container, false)
        val textView: TextView = root.findViewById(R.id.text_gallery)
        galleryViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })
        return root
    }
}