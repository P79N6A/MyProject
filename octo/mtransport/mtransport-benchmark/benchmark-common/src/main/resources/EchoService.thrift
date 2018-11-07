struct Message {
    1: required i32 id;
    2: required string content;
}

service EchoService
{
    string sendString(1:string str)
    binary sendBytes(1:binary bytes)
    list<Message> sendPojo(1:list<Message> msgList)
}