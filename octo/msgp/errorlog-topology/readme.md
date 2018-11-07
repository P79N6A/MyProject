
# run
代码打包
	
	mvn -U clean package

提交到storm执行

    测试环境：
	scp target/cos-loghandle-1.0.0-jar-with-dependencies.jar sankuai@cosdelta:~/logparser

	ssh sankuai@cosdelta
	storm jar cos-loghandle-1.0.0-jar-with-dependencies.jar com.sankuai.logparser.test.ExampleTopology /home/sankuai/logparser/config.proprities

	观察日志：/data/logs

    线上环境：
    storm jar cos-loghandle-1.0.0-jar-with-dependencies.jar com.sankuai.logparser.topology.LogHandleTopology /home/sankuai/loghandle/config.proprities

终止topology

	storm kill cos_errorlog_handle_topology
	
    经过30s（可配置）后该任务的所有工作进程被杀掉


# storm-starter
git clone git://github.com/nathanmarz/storm-starter.git


