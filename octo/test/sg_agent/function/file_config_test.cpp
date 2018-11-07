#include "interface_base.h"
#include "test_sg_agent.h"
#include "md5.h"
#include <gtest/gtest.h>
#include <fstream>
#include <unistd.h>
#include <iostream>
#include <errno.h>

using namespace std;

static int loadFile(std::string &filecontent, const std::string &filename, const std::string &filepath)
{
    int ret = -1;
    const std::string fullname = filepath + "/" + filename;
    std::ifstream in(fullname.c_str());
    if(in.is_open())
    {
        in.seekg(0, std::ios::end);  
        filecontent.resize(in.tellg());  
        in.seekg(0, std::ios::beg);  
        in.read(&filecontent[0], filecontent.size());  
        in.close();  
        ret = 0;
    }

    return ret;
}

class SgFileConfig: public InterfaceBase
{
    public:
        virtual void SetUp()
        {
            start_time_ = time(NULL);

            initArgs();

            sg_agent_handler_.init(appkey_s, ip_, port_);
        }

        virtual void TearDown()
        {
            const time_t end_time = time(NULL);
            EXPECT_TRUE(end_time - start_time_ <= 1) << "FILE Config testcase took too long.";
        }

        void RemoveCache(const char* filepath) {
            // remove cache first if existed
            if (-1 != access(filepath, 0)) {
              if (0 != remove(filepath)) {
                cout << "errno happen when removing directory, errno: " << errno  
                    << "file path: " << filepath << endl;
              }
            }
        }

        void initArgs()
        {
            RemoveCache("/opt/meituan/apps/mcc/com.sankuai.inf.sg_agent");

            file.__set_appkey("com.sankuai.inf.sg_agent");
            file.__set_env("prod");
            file.__set_path("/");

            filename = "sg_agent_mutable.xml";
            _cf.__set_filename(filename);
            EXPECT_EQ(0, loadFile(content, filename, "/opt/meituan/apps/sg_agent/"));
            _cf.__set_filecontent(content);

            MD5 md5string(content);
            _cf.__set_md5(md5string.md5());

            std::vector<ConfigFile> files;
            files.push_back(_cf);
            file.__set_configFiles(files);

            RemoveCache("/opt/meituan/apps/mcc/com.sankuai.octo.tmy");
            fileDefault.__set_appkey("com.sankuai.octo.tmy");
            filename = "test.txt";
            _cf.__set_filename(filename);
            std::vector<ConfigFile> filesDefault;
            filesDefault.push_back(_cf);
            fileDefault.__set_configFiles(filesDefault);

            RemoveCache("/opt/meituan/apps/mcc/com.sankuai.inf.mcc_test");
            fileG1.__set_appkey("com.sankuai.inf.mcc_test");
            fileG1.__set_env("prod");
            fileG1.__set_path("/");

            filename = "g1.conf";
            _cf.__set_filename(filename);

            std::vector<ConfigFile> filesg1;
            filesg1.push_back(_cf);
            fileG1.__set_configFiles(filesg1);
        }


        SGAgentHandler sg_agent_handler_;
        file_param_t file;
        file_param_t fileDefault;
        file_param_t fileG1;
        ConfigFile _cf;
        string content;
        string filename;
};

TEST_F(SgFileConfig, notifyIssue)
{
    EXPECT_EQ(0, sg_agent_handler_.client_->notifyFileConfigIssued(file)); 
}

TEST_F(SgFileConfig, notifyWork)
{
    EXPECT_EQ(0, sg_agent_handler_.client_->notifyFileConfigWork(file)); 
}

TEST_F(SgFileConfig, ScanFile)
{
   cout << file.configFiles.size() << endl;
   file_param_t resultFile;
   sg_agent_handler_.client_->getFileConfig(resultFile, file); 
   EXPECT_EQ(0, resultFile.err);
   EXPECT_EQ(1, resultFile.configFiles.size());
   cout << file.configFiles[0].filecontent << endl;
}

TEST_F(SgFileConfig, GetFile)
{
    file_param_t resultFile;
    sg_agent_handler_.client_->getFileConfig(resultFile, file); 
    EXPECT_EQ(1, resultFile.configFiles.size());
    cout << resultFile.configFiles[0].filecontent << endl;

    // second time, get from cache
    sg_agent_handler_.client_->getFileConfig(resultFile, file); 
    EXPECT_EQ(1, resultFile.configFiles.size());
    cout << resultFile.configFiles[0].filecontent << endl;
}

TEST_F(SgFileConfig, GetFileGroupDefault)
{
    file_param_t resultFile;
    sg_agent_handler_.client_->getFileConfig(resultFile, fileDefault); 
    EXPECT_EQ(0, resultFile.err);
    cout << "Default errCode: " << resultFile.err << endl;

    file_param_t resultFileG1;
    sg_agent_handler_.client_->getFileConfig(resultFileG1, fileG1); 
    EXPECT_EQ(-201501, resultFileG1.err);
    cout << "G1 errCode: " << resultFileG1.err << endl;
}
