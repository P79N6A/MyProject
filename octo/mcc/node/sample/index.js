var MTConfig = require("../index")
var appkey = "com.sankuai.fe.mta.parser";
var configClient = new MTConfig.ConfigClient({
	appkey: appkey,
	isOnline: true
});

configClient.get("key")
.then(function (value) {
	console.log(value);
})
.catch(function (err) {
	console.error(err);
});

configClient.set("key", "value")
.then(function (result) {	
	console.log(result);
})
.catch(function (err) {
	console.error(err);
});

var fileConfigClient = new MTConfig.FileConfigClient({
	appkey: appkey
});

fileConfigClient.get("production.js")
.then(function (value) {
	console.log(value);
})
.catch(function (err) {
	console.error(err);
});