var exec = require('cordova/exec');

var pluginName = "cordova-plugin-nfcv";

exports.enabled = function(success, error) {
    exec(success, error, pluginName, "enabled", []);
};

exports.connect = function(success, error) {
    exec(success, error, pluginName, "connect", []);
};

exports.disconnect = function(success, error) {
    exec(success, error, pluginName, "disconnect", []);
};

exports.isConnected = function(success, error) {
    exec(success, error, pluginName, "isConnected", []);
};

exports.read = function(page, success, error) {
    exec(success, error, pluginName, "read", [page]);
};

exports.write = function(page, data, success, error) {
    exec(success, error, pluginName, "write", [page, data]);
};

exports.unlock = function(pin, success, error) {
    exec(success, error, pluginName, "unlock", [pin]);
};

exports.echo = function(phrase, cb) {
    exec(cb, null, pluginName, 'echo', [phrase]);
};
