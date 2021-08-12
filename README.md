# 集成说明
## 1.gradle集成
```
implementation 'com.minivision.moe:moe-pad:1.0.0'
```
## 2.初始化
```kotlin
//业务端自己实现单例模式，避免创建多个通信客户端
//初始化配置IP,PORT,超时时间
val build = MoeConfig.Builder()
                .setIp(httpServerIp)
                .setPort(httpServerPort)
                .setTimeout(30000)
                .build()
//创建TCP客户端
val tcpClient: MoeClient = MoeFactory.create(build, MoeType.TCP)
                .setInterceptor(LMGInterceptor())//拦截器，拦截消息解析,或者自己实现拦截器
                .setHeartBeat(HeartBeatConfig(LMGEntity(CMD.DevStatus, ""), 180000))//设置心跳以及间隔
                .setListener(object: MoeListener{
 					override fun onConnected() {
            		//连接成功	
       				}

        			override fun onDisConnected() {
            		//断开连接
        			}

        			override fun onResponse(data: String) {
					//消息响应
					}
				})
```
