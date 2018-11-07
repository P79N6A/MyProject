package com.sankuai.meituan.config.test
import com.google.common.base.Throwables
import com.mysql.jdbc.Driver
import org.mybatis.generator.api.MyBatisGenerator
import org.mybatis.generator.config.Configuration
import org.mybatis.generator.config.xml.ConfigurationParser
import org.mybatis.generator.internal.DefaultShellCallback

class MybatisGenerator {
    static void main(String[] args) {
        def warnings = new ArrayList<String>()
        def overwrite = true
        Configuration config = loadFromXml(warnings)
        loadFromProperties(config)
        appendDomainConf(config)
        appendMapperConf(config)
        appendDaoConf(config)
        deleteOriMapperXml(config)
        def callback = new DefaultShellCallback(overwrite)
        def myBatisGenerator = new MyBatisGenerator(config, callback, warnings)
        myBatisGenerator.generate(null)
        println("done")
    }

    /**
     * 生成mapper xml文件的时候,如果已经存在对应的xml文件,则生成后的xml文件会有问题,所以先把原来的给删了
     * 具体为:原有的xml文件不会删除,新的内容会追加到文件的结尾处,导致有重复的方法
     */
    private static void deleteOriMapperXml(Configuration config) {
        def mapperPath = getMapperPath()
        def packagePath = config.contexts[0].sqlMapGeneratorConfiguration.targetPackage
        new File("$mapperPath/$packagePath").listFiles().findAll { it.name.endsWith(".xml") }.each {
            println("delete ${it.name} ${it.delete()}")
        }
    }

    private static void appendDaoConf(Configuration configuration) {
        def mapperPath = loadPropertyConf("conf_path.properties").getProperty("web_path")
        configuration.contexts.javaClientGeneratorConfiguration.each { it.targetProject = mapperPath }
    }

    private static void appendMapperConf(Configuration configuration) {
        def mapperPath = getMapperPath()
        configuration.contexts.sqlMapGeneratorConfiguration.each { it.targetProject = mapperPath }
    }

    private static String getMapperPath() {
        loadPropertyConf("conf_path.properties").getProperty("web_resources_path")
    }

    private static void appendDomainConf(Configuration configuration) {
        def commonPath = loadPropertyConf("conf_path.properties").getProperty("web_path")
        configuration.contexts.javaModelGeneratorConfiguration.each { it.targetProject = commonPath }
    }

    private static void loadFromProperties(Configuration configuration) {
        String webappPath = loadPropertyConf("conf_path.properties").getProperty("webapp_path")
        Properties properties = loadPropertyConf(new File("$webappPath/WEB-INF/conf/database.properties"))
        configuration.contexts.each {
            it.jdbcConnectionConfiguration.driverClass = Driver.class.getName()
            it.jdbcConnectionConfiguration.connectionURL = properties.getProperty("database.url")
            it.jdbcConnectionConfiguration.userId = properties.getProperty("database.username")
            it.jdbcConnectionConfiguration.password = properties.getProperty("database.password")
        }
    }

    private static Configuration loadFromXml(ArrayList<String> warnings) {
        def configFile = new File(MybatisGenerator.class.getClassLoader().getResource("generatorConfig.xml").file)
        def cp = new ConfigurationParser(warnings)
        cp.parseConfiguration(configFile)
    }

    public static Properties loadPropertyConf(String confFileName) {
        def inputStream = MybatisGenerator.class.getClassLoader().getResourceAsStream(confFileName)
        def p = new Properties()
        try {
            p.load(inputStream)
            p
        } catch (IOException e) {
            throw Throwables.propagate(e)
        }
    }

    public static Properties loadPropertyConf(File propertiesFile) throws FileNotFoundException {
        def inputStream = new FileInputStream(propertiesFile)
        def p = new Properties()
        try {
            p.load(inputStream)
            p
        } catch (IOException e) {
            throw Throwables.propagate(e)
        }
    }
}
