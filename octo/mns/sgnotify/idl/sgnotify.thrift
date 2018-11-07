include 'fb303.thrift'

service SgNotifyService extends fb303.FacebookService
{
    string Notify(1:i32 cmdType, 2:string sData);
}
