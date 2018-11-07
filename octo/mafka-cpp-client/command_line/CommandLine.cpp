#include <string>
#include <stdlib.h>
#include <stdio.h>
#include <fstream>

#include "Producer.h"
#include "Consumer.h"
#include "Log.h"
#include "TimeUtil.h"
#include "StringConverter.h"

#include "StringUtil.h"

#include "Mutex.h"
#include "LockGuard.h"


using namespace mafka;
using namespace std;


static bool ParseArgument(std::string const& argument, std::string& key, std::string& value)
{
	size_t pos = argument.find('=');
	if(pos ==  std::string::npos)
	{
		return false;
	}

	key = argument.substr(0, pos);
	StringUtil::TrimString(key);

	value = argument.substr(pos+1);
	StringUtil::TrimString(value);

	if(key.empty() || value.empty())
	{
		return false;
	}

	return true;
}

typedef std::map<std::string, std::string> Properties;
static void ParseArguments(int argc, char* argv[], Properties& args)
{
	for(int i = 1; i < argc; ++i)
	{
		std::string key;
		std::string value;
		if(ParseArgument(argv[i], key, value))
		{
			args.insert(Properties::value_type(key, value));
		}
	}
}

static void SendSync(ProducerPtr producer, std::string const& message, int count, int speed, int partition)
{
	mafka::timestamp_t start = TimeUtil::TimeNow();
	for (int i = 1; i <= count; ++i)
	{
		std::string content(StringConverter::IntToString(i) + ":" + message);
		int ret = producer->Send(partition, content.c_str(), content.length(), 1000);
		if(ret)
		{
			ERROR("send message failed, ret=%d, i=%d, content=%s\n", ret, i, content.c_str());
			exit(EXIT_FAILURE);
		}

		if (i % speed == 0)
		{
			mafka::timestamp_t end = TimeUtil::TimeNow();
			if (end - start < 1000)
			{
				usleep( (1000-(end-start))*1000 );
			}

			if (end - start > 1000)
			{
				ERROR("sync send can not reach speed=%d as expected, spend=%llu\n", speed, end-start);
				exit(EXIT_FAILURE);
			}

			start = TimeUtil::TimeNow();
		}
	}

	INFO("SendSync complete, count=%d, speed=%d\n", count, speed);
	
}

int complete_count = 0;
class AsyncHandler : public CompletionHandler
{
public:
	virtual void OnComplete(int error_code, int partition, const char* buf, int len)
	{
		if(error_code)
		{
			INFO("send complete error_code=%d\n", error_code);
		}
		++complete_count;
	}
};
AsyncHandler async_handler;
static void SendAsync(ProducerPtr producer, std::string const& message, int count, int speed, int partition)
{
	complete_count = 0;
	mafka::timestamp_t start = TimeUtil::TimeNow();
	for (int i = 1; i <= count; ++i)
	{
		std::string content(StringConverter::IntToString(i) + ":" + message);
		int ret = producer->SendAsync(partition, content.c_str(), content.length(), async_handler);
		if(ret)
		{
			ERROR("send async message failed, ret=%d, i=%d, content=%s\n", ret, i, content.c_str());
			exit(EXIT_FAILURE);
		}

		if (i % speed == 0)
		{
			mafka::timestamp_t end = TimeUtil::TimeNow();
			if (end - start < 1000)
			{
				usleep( (1000-(end-start))*1000 );
				if(complete_count != i)
				{
					ERROR("async send callback can not reach speed=%d expected, count=%d, complete=%d\n",
							speed, i, complete_count);
					exit(EXIT_FAILURE);
				}
			}

			if (end - start > 1000)
			{
				ERROR("async send itself can not reach speed=%d as expected, spend=%llu\n", speed, end-start);
				exit(EXIT_FAILURE);
			}

			start = TimeUtil::TimeNow();
		}

		start = TimeUtil::TimeNow();
	}

	while(complete_count != count)
	{
		ERROR("waiting last messages ack, count=%d, complete=%d\n",
			count, complete_count);
		sleep(1);
	}

	INFO("SendAsync complete, count=%d, speed=%d\n", count, speed);
	
}




std::ofstream output_file;
Mutex output_mutex;
long long sequence[1000] = {0};
class RecvHandler : public mafka::MessageHandler
{
public:
	virtual mafka::ConsumeStatus OnRecvMessage(int partition, const char* buf, int len)
    {
		LockGuard guard(output_mutex);
		if(!output_file)
		{
			exit(EXIT_FAILURE);
		}

		std::string content(buf, len);
		content = StringConverter::Int64ToString(sequence[partition]++) + ":" +
				StringConverter::IntToString(partition) + ":" +
				content;

		output_file << content << std::endl;
		output_file.flush();

		return mafka::CONSUME_SUCCESS;
    }
};
RecvHandler recv_handler;

static void RecvMessage(ConsumerPtr consumer, int consume_time)
{
	consumer->RecvMessage(recv_handler);
	sleep(consume_time);
}

int main(int argc, char* argv[])
{
	Properties args;
	ParseArguments(argc, argv, args);

	Properties::iterator i = args.find("appkey");
	if(i == args.end())
	{
		INFO("no appkey\n");
		return EXIT_FAILURE;
	}
	std::string appkey = i->second;

	i = args.find("topic");
	if(i == args.end())
	{
		INFO("no topic\n");
		return EXIT_FAILURE;
	}
	std::string topic = i->second;

	i = args.find("namespace");
	if(i == args.end())
	{
		INFO("no namespace\n");
		return EXIT_FAILURE;
	}
	std::string name_space = i->second;

	i = args.find("type");
	if(i == args.end())
	{
		INFO("no type\n");
		return EXIT_FAILURE;
	}
	std::string type = i->second;

	if(type == "producer")
	{
		i = args.find("partition");
		int partition = -1;
		if(i != args.end())
		{
			partition = StringConverter::ParseInt(i->second);
		}

		i = args.find("message");
		if(i == args.end())
		{
			INFO("no message content\n");
			return EXIT_FAILURE;
		}
		std::string message = i->second;
		
		i = args.find("count");
		if(i == args.end())
		{
			INFO("no count\n");
			return EXIT_FAILURE;
		}
		int count = StringConverter::ParseInt(i->second);
		
		i = args.find("speed");
		if(i == args.end())
		{
			INFO("no speed\n");
			return EXIT_FAILURE;
		}
		int speed = StringConverter::ParseInt(i->second);
		
		i = args.find("mode");
		if(i == args.end())
		{
			INFO("no mode\n");
			return EXIT_FAILURE;
		}
		std::string mode = i->second;

		ProducerPtr producer = NewProducer(appkey, topic, name_space);
		if(!producer)
		{
			ERROR("producer startup failed");
			return 0;
		}
		if(mode == "sync")
		{
			SendSync(producer, message, count, speed, partition);
		}
		else if(mode == "async")
		{
			SendAsync(producer, message, count, speed, partition);
		}
		else
		{
			INFO("invalid mode, please specify sync or async\n");
			return EXIT_FAILURE;
		}
	}
	else if(type == "consumer")
	{
		i = args.find("group");
		if(i == args.end())
		{
			INFO("no group\n");
			return EXIT_FAILURE;
		}
		std::string group = i->second;

		i = args.find("consumeTime");
		if(i == args.end())
		{
			INFO("no consumeTime\n");
			return EXIT_FAILURE;
		}
		int consume_time = StringConverter::ParseInt(i->second);

		i = args.find("filename");
		if(i == args.end())
		{
			INFO("no filename\n");
			return EXIT_FAILURE;
		}
		std::string const& output_file_name = i->second;
		output_file.open(output_file_name.c_str());

		mafka::ConsumerPtr consumer = mafka::NewConsumer(appkey, topic, group, name_space);
		RecvMessage(consumer, consume_time);
	}

	return EXIT_SUCCESS;
}
