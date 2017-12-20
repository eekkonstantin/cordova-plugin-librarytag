var exec = require('cordova/exec');

var pluginName = "cordova-plugin-librarytag";

exports.enabled = function(success, error) {
    exec(success, error, pluginName, "enabled", []);
};
