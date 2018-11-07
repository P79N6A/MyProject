/**
 * @author liaozhongwu
 * @description 封装sdk为node package
 */
"use strict";
var SGAgentClient = require("@mtfe/turbo-thrift").SGAgentClient
, Promise = (global && global.Promise) || require("promise")
, DataType = require("data-type")
/**
 * @Class ConfigClient
 * @author liaozhongwu
 * @description 动态配置Client
 * @param options Object
 * @param options.appkey String required
 * @param options.inOnline Boolean default:NODE_ENV === "production"
 * @param options.cacheable Boolean default:true
 * @param options.expire Number default:10000
 * @param options.retry Number default:2
 */
function ConfigClient (options) {
  if (!DataType.isPlainObject(options)) {
    throw new Error("options must be an object.");
  }
  if (!DataType.isString(options.appkey)) {
    throw new Error("options.appkey must be a string.");
  }
  if (options.isOnline === undefined) {
    options.isOnline = process.env.NODE_ENV === 'production';
  } 
  else {
    options.isOnline = options.isOnline ? true : false;
  }
  if (options.cacheable === undefined) {
    options.cacheable = true;
  }
  if (options.expire === undefined) {
    options.expire = 10000;
  }
  if (options.timeout === undefined) {
    options.timeout = 500;
  }
  if (options.retry === undefined) {
    options.retry = 3;
  }
  this.options = options;
  this.client = new SGAgentClient({isOnline: this.options.isOnline, timeout: this.options.timeout});
  if (options.cacheable) {
    this.cache = {};
    this.lastTime = 0;
  }
}
ConfigClient.prototype = {
  /**
   * @Method get
   * @author liaozhongwu
   * @description 动态配置根据key获取value
   * @param key
   * @return Promise
   */
  get: function (key, cacheable) {
    if (!DataType.isString(key)) {
      return Promise.reject(new Error("key must be a string."));
    }
    if (cacheable === undefined) {
      cacheable = this.options.cacheable;
    }
    var self = this;
    if (cacheable) {
      if (Date.now() - this.lastTime > this.options.expire) {
        this.lastTime = Date.now();
        return this.fetch()
          .then(function (data) {
            self.cache = data;
            return self.cache[key];
          });
      } else {
        return Promise.resolve(this.cache[key]);
      }
    }
    else {
      return this.fetch()
        .then(function (data) {
          return data[key];
        });
    }
  },
  /**
   * @Method set
   * @author liaozhongwu
   * @description 动态配置为key设置value
   * @param key, value
   * @return Promise
   */
  set: function (key, value, cacheable) {
    if (!DataType.isString(key)) {
      return Promise.reject(new Error("key must be a string."));
    }
    if (value === undefined) {
      return Promise.reject(new Error("missing value."));
    }
    if (cacheable === undefined) {
      cacheable = this.options.cacheable;
    }
    var self = this;
    return this.push({[key]: value})
      .then(function (ret) {
        if (ret === 0 && cacheable) {
          self.cache[key] = value;
        }
        return ret;
      });
  },
  /**
   * @Method fetch
   * @author liaozhongwu
   * @description 从SGAgent获取数据
   * @param filename
   * @param retry default:1
   * @return Promise
   * @private
   */
  fetch: function (retry) {
    if (retry === undefined) {
      retry = 1;
    }
    var self = this;
    return this.client.getConfig(this.options.appkey)
      .then(function (config) {
        if (config) {
          return config;
        }
        else {
          if (!self.options.isOnline) {
            console.error("fetch config from SGAgent failed, retry " + retry + ".");
          }
          if (retry > self.options.retry) {
            throw new Error("fetch config from SGAgent failed.");
          }
          return self.fetch(retry + 1);
        }
      })
      .catch(function (err) {
        if (!self.options.isOnline) {
          console.error("fetch config from SGAgent failed, retry " + retry + ".");
          console.error("caused by: " + err);
        }
        if (retry > self.options.retry) {
          throw err;
        }
        return self.fetch(retry + 1);
      });
  },
  /**
   * @Method fetch
   * @author liaozhongwu
   * @description 向SGAgent推送数据
   * @param filename
   * @param retry default:1
   * @return Promise
   * @private
   */
  push: function (config, retry) {
    if (retry === undefined) {
      retry = 1;
    }
    var self = this;
    return this.client.setConfig(this.options.appkey, config)
      .then(function (ret) {
        if (ret === 0) {
          return ret;
        }
        else {
          if (!self.options.isOnline) {
            console.error("push config to SGAgent failed, retry " + retry + ".");
          }
          if (retry > self.options.retry) {
            throw new Error(err);
          }
          return self.push(config, retry + 1);
        }
      })
      .catch(function (err) {
        if (!self.options.isOnline) {
          console.error("push config to SGAgent failed, retry " + retry + ".");
          console.error("caused by: " + err);
        }
        if (retry > self.options.retry) {
          throw new Error(err);
        }
        return self.push(config, retry + 1);
      });
  }
}
/**
 * @Class ConfigClient
 * @author liaozhongwu
 * @description 文件配置Client
 * @param options.appkey String required
 * @param options.inOnline Boolean default:NODE_ENV === "production"
 * @param options.cacheable Boolean default:true
 * @param options.expire Number default:10000
 * @param options.retry Number default:2
 */
function FileConfigClient (options) {
  if ( !DataType.isPlainObject(options) ) {
    throw new Error("options must be an object.");
  }
  if ( !DataType.isString(options.appkey) ) {
    throw new Error("options.appkey must be a string.");
  }
  if (options.isOnline === undefined) {
    options.isOnline = process.env.NODE_ENV === 'production';
  } 
  else {
    options.isOnline = options.isOnline ? true : false;
  }
  if (options.cacheable === undefined) {
    options.cacheable = true;
  }
  if (options.expire === undefined) {
    options.expire = 10000;
  }
  if (options.timeout === undefined) {
    options.timeout = 500;
  }
  if (options.retry === undefined) {
    options.retry = 3;
  }
  this.options = options;
  this.client = new SGAgentClient({isOnline: this.options.isOnline, timeout: this.options.timeout});
  if (options.cacheable) {
    this.cache = {};
    this.lastTime = 0;
  }
}
FileConfigClient.prototype = {
  /**
   * @Method get
   * @author liaozhongwu
   * @description 文件配置根据filename获取value
   * @param filename
   * @return Promise
   */
  get: function (filename, cacheable) {
    if (!DataType.isString(filename)) {
      return Promise.reject(new Error("filename must be a string."))
    }
    if (cacheable === undefined) {
      cacheable = this.options.cacheable;
    }
    var self = this;
    if (cacheable) {
      if (Date.now() - this.lastTime > this.options.expire) {
        this.lastTime = Date.now();
        return this.fetch(filename)
          .then(function (data) {
            if (data.length > 0) {
              self.cache[filename] = data[0].filecontent.toString();
              return self.cache[filename];
            }
          });
      } else {
        return Promise.resolve(this.cache[filename]);
      }
    }
    else {
      return this.fetch(filename)
        .then(function (data) {
          if (data.length > 0) {
            return data[0].filecontent.toString();
          }
        });
    }
  },
  /**
   * @Method fetch
   * @author liaozhongwu
   * @description 从SGAgent获取数据
   * @param filename
   * @param retry default:1
   * @return Promise
   * @private
   */
  fetch: function (filename, retry) {
    if (retry === undefined) {
      // 如果不设置重试次数
      retry = 1;
    }
    var self = this;
    return this.client.getFileConfig(this.options.appkey, filename)
      .then(function (config) {
        if (config) {
          return config;
        }
        else {
          if (retry > self.options.retry) {
            throw new Error("fetch config failed.");
          }
          return self.fetch(filename, retry + 1);
        }
      })
      .catch(function (err) {
        if (retry > self.options.retry) {
          throw err;
        }
        return self.fetch(filename, retry + 1);
      });
  }
}
exports.ConfigClient = ConfigClient;
exports.FileConfigClient = FileConfigClient;
