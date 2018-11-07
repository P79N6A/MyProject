var Test = require("test");
var MtConfig = require("../index");
var DataType = require("data-type");

var testData = {
 	appkey: "com.sankuai.fe.mta.parser",
	key: "key",
	value: "value",
	okey: "json",
	ovalue: {value: "value"},
	okResult: 0,
	filename: "production.js",
	fileValue: {
	  host: {
	    main: "meituan.com",
	    cdn: "meituan.net",
	    document: "ismart.meituan.com"
	  },
	  flume: {
	    scribeHost: "127.0.0.1",
	    scribePort: 4252,
	    scribeCategory: "fe_emis_log",
	    logLevel: 5
	  },
	  isDebug: false,
	  isOnline: true,
	  env: "production",
	  maxListeners: 20,
	  port: 8080
	}
}
var configClient = new MtConfig.ConfigClient({
	appkey: testData.appkey,
	isOnline: false
});
var fileConfigClient = new MtConfig.FileConfigClient({
	appkey: testData.appkey,
	isOnline: false
});

exports["test set value[string] of key[string]"] = function (assert, done) {
	configClient.set(testData.key, testData.value)
	.then(function (result) {
		assert.ok(DataType.isNumber(result), "result should be a Number");
		assert.equal(result, testData.okResult, "result should be correct");
		done();
	}, function (err) {
		assert.fail({
			expected: "resolve",
			actual: "reject",
			message: "promise should resolve"
		});
		done();
	});
}

exports["test set value[object] of key[string]"] = function (assert, done) {
	configClient.set(testData.okey, testData.ovalue)
	.then(function (result) {
		assert.ok(DataType.isNumber(result), "result should be a Number");
		assert.equal(result, testData.okResult, "result should be correct");
		done();
	}, function (err) {
		assert.fail({
			expected: "resolve",
			actual: "reject",
			message: "promise should resolve"
		});
		done();
	});
}

exports["test set value[string] of key[object]"] = function (assert, done) {
	configClient.set({}, testData.value)
	.then(function (result) {
		assert.fail({
			expected: "reject",
			actual: "resolve",
			message: "promise should reject"
		});
		done();
	}, function (err) {
		assert.ok(DataType.isError(err), "promise should reject");
		done();
	});
}

exports["test set value[none] of key[string]"] = function (assert, done) {
	configClient.set(testData.key)
	.then(function (result) {
		assert.fail({
			expected: "reject",
			actual: "resolve",
			message: "promise should reject"
		});
		done();
	}, function (err) {
		assert.ok(DataType.isError(err), "promise should reject");
		done();
	});
}

exports["test get value of key[string]"] = function (assert, done) {
	configClient.get(testData.key)
	.then(function (value) {
		assert.ok(DataType.isString(value), "value should be a String");
		assert.equal(value, testData.value, "value should be correct");
		done();
	}, function (err) {
		assert.fail({
			expected: "resolve",
			actual: "reject",
			message: "promise should resolve"
		});
		done();
	});
}

exports["test get value of key[object]"] = function (assert, done) {
	configClient.get(testData.errKey)
	.then(function (value) {
		assert.fail({
			expected: "reject",
			actual: "resolve",
			message: "promise should reject"
		});
		done();
	}, function (err) {
		assert.ok(DataType.isError(err), "promise should reject");
		done();
	});
}

exports["test get value of key[none]"] = function (assert, done) {
	configClient.get()
	.then(function (value) {
		assert.fail({
			expected: "reject",
			actual: "resolve",
			message: "promise should reject"
		});
		done();
	}, function (err) {
		assert.ok(DataType.isError(err), "promise should reject");
		done();
	});
}

exports["test get value of filename[string]"] = function (assert, done) {
	fileConfigClient.get(testData.filename)
	.then(function (value) {
		assert.ok(DataType.isString(value), "value should a String");
		assert.deepEqual(JSON.parse(value), testData.fileValue, "value should be corrent");
		done();
	}, function (err) {
		assert.fail({
			expected: "resolve",
			actual: "reject",
			message: "promise should resolve"
		});
		done();
	});
}

exports["test get value of filename[object]"] = function (assert, done) {
	fileConfigClient.get({})
	.then(function (value) {
		assert.fail({
			expected: "reject",
			actual: "resolve",
			message: "promise should reject"
		});
		done();
	}, function (err) {
		assert.ok(DataType.isError(err), "promise should reject");
		done();
	});
}

exports["test get value of filename[none]"] = function (assert, done) {
	fileConfigClient.get()
	.then(function (value) {
		assert.fail({
			expected: "reject",
			actual: "resolve",
			message: "promise should reject"
		});
		done();
	}, function (err) {
		assert.ok(DataType.isError(err), "promise should reject");
		done();
	});
}

exports["test cache"] = function (assert, done) {
	configClient.options.expire = 1500;
	var cache;
	setTimeout(function () {
		configClient.get("key")
		.then(function (value) {
			cache = value
		});
	}, 0);
	setTimeout(function () {
		configClient.set("key", "cache", false)
	}, 500)
	setTimeout(function () {
		configClient.get("key")
		.then(function (value) {
			assert.equal(value, cache, "value should be cached value");
		})
	}, 1000)
	setTimeout(function () {
		configClient.get("key")
		.then(function (value) {
			assert.equal(value, "cache", "value should be updated value");
		})
	}, 2000)
	setTimeout(function () {
		configClient.set("key", "value")
		.then(function () {
			done();
			configClient.options.expire = 10000;
		})
	}, 2500)
}

if (module === require.main) { 
	Test.run(exports);
}
