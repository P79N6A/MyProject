for pid in `ps aux | grep sg_agent | awk -F " " '{print $2}'`
do
    kill -9 $pid
done
