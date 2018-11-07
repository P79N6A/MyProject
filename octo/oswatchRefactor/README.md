# sbt构建的scala项目：使用mtthrift/使用OPS发布

------

项目包含server 和 client， 使用的appkey为同一个，但是在实际使用场景中应该为是不同的appkey。如何启动：

###开发环境
#### 1. 启动项目
`> ./sbt `

#### 2. 运行项目
`> run `

```
Multiple main classes detected, select one to run:

 [1] com.sankuai.inf.octo.sbttemplate.client.MTThriftClient
 [2] com.sankuai.inf.octo.sbttemplate.server.MTThriftServer

Enter number: 
```

选择2，启动server 

选择1，启动client

###OPS发布
#### 1.项目设置

在build.sbt中修改：

```
assemblyOutputPath in assembly := file("deploy/inf-octo-sbttemplate-assembly.jar")
mainClass in assembly := Some("com.sankuai.inf.octo.sbttemplate.server.MTThriftServer")
```
配置启动类，并将`inf-octo-sbttemplate`替换为项目名，`-assembly.jar`请勿修改。

#### 2.发布配置

在OPS中创建发布应用，类型为Simple。

发布机预处理脚本为`assembly.sh`，编译后发布子目录为`deploy`，代码发布到`/opt/meituan/apps/inf-octo-sbttemplate`（`inf-octo-sbttemplate`替换为项目名），重启脚本为`run.sh`。

