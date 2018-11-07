ori="/usr/lib64/libclog*"
new="lib/libclog*"
header_ori="/usr/include/clog/log.h"
header_new="include/clog/log.h"
md5sum $new $ori
ls -l $new $ori $header_ori $header_new
cp lib/libclog* -f /usr/lib64 
cp $header_new -f $header_ori

