struct SubMessage {
    1:optional i64 id;
    2:optional string value;
}

struct Message {
    1:optional i64 id;
    2:optional string value;
    3:optional list<SubMessage> subMessages;
}

exception GenericException {
    1:string message;
}

service Generic {
    void echo1();

    string echo2(1:string message);

    SubMessage echo3(1:SubMessage message);

    list<SubMessage> echo4(1:list<SubMessage> messages);

    map<SubMessage, SubMessage> echo5(1:map<SubMessage, SubMessage> messages);

    Message echo6(1:Message message);

    SubMessage echo7(1:string strMessage, 2:SubMessage message);

    void echo8() throws (1:GenericException genericException);

    byte echo9(1:byte param1, 2:i32 param2, 3:i64 param3, 4:double param4);
}

