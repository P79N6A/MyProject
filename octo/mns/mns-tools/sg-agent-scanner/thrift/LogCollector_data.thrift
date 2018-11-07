include 'sgagent_data.thrift'

struct LogList {
    1: required list<sgagent_data.SGLog> logs;
}
