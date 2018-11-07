namespace java cn.jason.test

enum TestEnum{
    TEST_1;
}

exception MtConfigException{
    1 : string message;
    2 : TestEnum exceptionType;
}

service MtConfigService {
    void test(1 : TestEnum testEnum) throws (1 : MtConfigException e);
}
