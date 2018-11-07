

service DetectorService {

    /*
     * env:
     * appkey:
     * path:
     * providers:
     * scanRoundCounter:
     * timestamp:
     *
     */
    void check(1:string env, 2:string appkey, 3:string path, 4:list<string> providers, 5:i32 scanRoundCounter, 6:i64 timestamp)

    void userDefinedHttpCheck(1:string env, 2:string appkey, 3:string path, 4:list<string> providers, 5:string checkUrl, 6:i32 scanRoundCounter, 7:i64 timestamp)
}