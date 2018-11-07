/*jshint node: true */
var getConfigSync = require('@mtfe/cos-deploy-config-sync');
var env = process.env.NODE_ENV || 'development';

module.exports = function (grunt) {

    var config = getConfigSync();
    config.appKey = 'mtconfig';
    //任务注册
    var deployConfig = config[env];
    if(!deployConfig) {
        grunt.log.writeln('传入的参数不正确:' + env);
        return;
    }
    deployConfig.appKey = config.appKey;

    var groupInfo = {};
    groupInfo[config.appKey] = {
        cwd: 'static'
    };

    grunt.initConfig({
        build: {
            options: {
                groupInfo: groupInfo
            }
        },
        buildtpl: {
            main: {
                options: {
                    deployConfig: deployConfig
                },
                files: [{
                    expand: true,
                    cwd: '.',
                    src: [
                        '**/*.ftl',
                        '**/*.inc',
                        '!node_modules/**'
                    ]
                }]
            }
        },
        buildCssFile: {
            main: {
                options: {
                    deployConfig: deployConfig,
                    dest: 'dest'
                },
                files: [{
                    expand: true,
                    cwd: '.',
                    src: [
                        'dest/**/*.css'
                    ]
                }]
            }
        },
        rsync: {
            options: deployConfig
        }
    });
    //初始化执行顺序
    grunt.registerTask('default', ['build', 'buildtpl', 'buildCssFile', 'rsync']);

    // Load grunt tasks automatically
    require('load-grunt-tasks')(grunt, {
        pattern: ['@mtfe/grunt-*']
    });
};
