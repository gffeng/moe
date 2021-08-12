package com.minivision.moe.ui.slideshow

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.minivision.moe.R

class WebSocketFragment : Fragment() {

    private lateinit var slideshowViewModel: WebSocketViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        slideshowViewModel =
            ViewModelProvider(this).get(WebSocketViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_websocket, container, false)
        val textView: TextView = root.findViewById(R.id.text_slideshow)
        slideshowViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })
        return root
    }
}