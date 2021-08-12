package com.minivision.moe.ui.tcp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.minivision.moe.MoeFactory
import com.minivision.moe.core.MoeClient
import com.minivision.moe.core.MoeListener
import com.minivision.moe.databinding.FragmentTcpBinding
import com.minivision.moe.entiy.LogIn
import com.minivision.moe.interceptor.LMGInterceptor
import com.minivision.moe.meta.HeartBeatConfig
import com.minivision.moe.meta.LMGEntity
import com.minivision.moe.meta.MoeConfig
import com.minivision.moe.meta.MoeType
import com.minivision.parameter.util.LogUtil
import okio.ByteString

class TcpFragment : Fragment() {

    private lateinit var homeViewModel: TcpViewModel
    private lateinit var tcpClient: MoeClient
    private lateinit var udpClient: MoeClient

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        homeViewModel =
            ViewModelProvider(this).get(TcpViewModel::class.java)
        val binding = FragmentTcpBinding.inflate(inflater, container, false)
        binding.ui = this
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val build = MoeConfig.Builder()
            .setIp("125.75.98.143")
            .setPort(10102)
            .setTimeout(30000)
            .setMoeType(MoeType.TCP)
            .build()

        tcpClient = MoeFactory.create(build)
            .setHeartBeat(HeartBeatConfig(LMGEntity(65535, ""), 180000))
            .setInterceptor(LMGInterceptor())
            .setListener(object : MoeListener {
                override fun onConnected() {
                    LogUtil.i("home", "onConnected")
                    tcpClient.sendData(LMGEntity(823, LogIn().toString()).parse())
                }

                override fun onDisConnected() {
                    LogUtil.i("home", "onDisConnect")
                }

                override fun onResponse(data: String) {
                    LogUtil.i("home", "onResponse:$data")
                }
            })

//        udpClient = MoeFactory.create(build, MoeType.UDP)
//            .setHeartBeat(HeartBeatConfig(Heart(), 30000))
//            .setInterceptor(WisdomSiteInterceptor())
//            .setListener(object :MoeListener{
//                override fun onConnected() {
//                }
//
//                override fun onDisConnected() {
//                }
//
//                override fun onResponse(data: String) {
//                }
//
//            })
    }

    fun tcpConnect(view: View) {
        tcpClient.connect()
    }

    fun tcpDisConnect(view: View) {
        tcpClient.close()
    }

    fun tcpSend(view: View) {
        tcpClient.sendData(LMGEntity(823, LogIn().toString()).parse())
    }


    fun udpSend(view: View) {
        udpClient.sendData("我i的骄傲四的骄傲哦收集大量减少到了")
    }

    fun udpConnect(view: View) {
        udpClient.connect()
    }

    fun udpDisConnect(view: View) {
        udpClient.close()
    }
}