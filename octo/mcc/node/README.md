# 配置中心Node Sdk文档
## Overview
配置中心文档:<http://wiki.sankuai.com/display/DEV/MCC>

配置中心线下网站:<http://octo.test.sankuai.info/service/detail#config>

配置中心线上网站:<http://octo.sankuai.com/service/detail#config>

请在网站注册唯一标识作为Appkey

## Install

	$ npm --registry=http://r.npm.sankuai.com install @mtfe/mt-config
	

## Usage

	var MtConfig = require("@mtfe/mt-config");
	var configClient = new MtConfig.ConfigClient({
		appkey:	appkey
	});

	// 设置配置
	configClient.set(key, value)
	.then(function (result) {	
		// result === 0
	})
	.catch(function (err) {
		// handle err
	});
	// 获取配置
	configClient.get(key)
	.then(function (value) {
		// handle value
	})
	.catch(function (err) {
		// handle err
	});
	
	var fileConfigClient = new MtConfig.FileConfigClient({
		appkey: appkey
	});
	
	// 获取文件配置
	fileConfigClient.get(filename)
	.then(function (value) {
		// handle value
	})
	.catch(function (err) {
		// handle err
	});

## API

### new ConfigClient(options)
	
| key | description | type | optional | default |
|-----|------|-----|-------|-------|
| options.appkey | octo注册的appkey | string | (必需) | |
| options.isOnline | 是否为线上环境 | boolean | true,false | NODE_ENV === "production" |
| options.cacheable | 是否使用缓存 | boolean | true,false | true |
| options.expire | 缓存时间 | number | (ms) | 10000 |
| options.timeout | 超时时间 | number | (ms) | 500 |
| options.retry | 失败重试次数 | number |  | 3 |

### ConfigClient.prototype.set(key, value, cacheable)

| key | description | type | optional | default |
|-----|------|-----|-------|-------|
| key | 键 | string | | |
| value | 值 | any | | |
| value | 是否使用缓存 | boolean | true,false| this.options.cacheable |

### ConfigClient.prototype.get(key, cacheable)

| key | description | type | optional | default |
|-----|------|-----|-------|-------|
| key | 键 | string | | |
| cacheable | 是否使用缓存 | boolean | true,false | this.options.cacheable |


### new FileConfigClient(options)
	
| key | description | type | optional | default |
|-----|------|-----|-------|-------|
| options.appkey | octo注册的appkey | string | (必需) | |
| options.isOnline | 是否为线上环境 | boolean | true,false | NODE_ENV === "production" |
| options.cacheable | 是否使用缓存 | boolean | true,false | true |
| options.expire | 缓存时间 | number | (ms) | 10000 |
| options.timeout | 超时时间 | number | (ms) | 500 |
| options.retry | 失败重试次数 | number |  | 3 |

### FileConfigClient.prototype.get(key, cacheable)

| key | description | type | optional | default |
|-----|------|-----|-------|-------|
| filename | 文件名 | string | | |
| cacheable | 是否使用缓存 | boolean | true,false | this.options.cacheable |

	
## Test

	npm test

## Issue
请大象@liaozhongwu
