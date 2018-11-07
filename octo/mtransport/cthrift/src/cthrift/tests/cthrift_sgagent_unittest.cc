//
// Created by Chao Shu on 17/3/5.
//

//TODO test env value
void TestOfficalEnv(void) {
  static const string str_appenvfile_deployenv_exist_env_miss
      ("deployenv=qa\nzkserver=qa.lion.dp:2181");
  static const string str_appenvfile_deployenv_miss_env_exist
      ("zkserver=qa.lion.dp:2181\nenv=qa");
  static const string
      str_appenvfile_deployenv_env_miss("zkserver=qa.lion.dp:2181");
  static const string str_appenvfile_deployenv_env_exist
      ("deployenv=qa\nzkserver=qa.lion.dp:2181\nenv=prod");

}

//TODO test env value
void TestOctoEnv(void) {
  static const string
      str_sgagentenvfile_mnspath_miss("<SGAgentConf>\n</SGAgentConf>");
  static const string str_sgagentenvfile_mnspath_exist
      ("<SGAgentConf>\n<MnsPath>/mns/sankuai/prod</MnsPath>\n</SGAgentConf>");

}

void TestFetchEnv(void) {
  string str_cmd();
  bool b_octo_env_file_exist = false;
  if (1 - system("test -f " + CthriftSgagent::kStrSgagentEnvFileWithPath
                     + "; echo $?")) {  //exist
    b_octo_env_file_exist = true;

    str_cmd.assign("sudo mv " + CthriftSgagent::kStrSgagentEnvFileWithPath + " "
                       + CthriftSgagent::kStrSgagentEnvFileWithPath + ".bak");

    if (CTHRIFT_UNLIKELY(-1 == system(str_cmd.c_str()))) {
      cerr << str_cmd << " failed" << endl;
      exit(-1);
    }
  }

  //appenv exist, sgagent_env miss






  //deployenv exist, env miss
  //deployenv miss, env exist
  //deployenv/env both exist
  //deployenv/env both miss


  //appenv miss, sgagent_env exist

  //appenv/sgagent_env both exist

  //appenv/sgagent_env both miss

}
