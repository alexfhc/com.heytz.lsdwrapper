var exec = require('cordova/exec');

exports.setDeviceWifi = function (wifiSSID,
                                  wifiKey,
                                  username,
                                  easylinkVersion,
                                  activateTimeout,
                                  activatePort,
                                  moduleDefaultUser,
                                  moduleDefaultPass,
                                  success, error) {
    exec(success, error, "lsdwrapper", "setDeviceWifi",
        [
            wifiSSID,
            wifiKey,
            username,
            easylinkVersion,
            activateTimeout,
            activatePort,
            moduleDefaultUser,
            moduleDefaultPass
        ]);
};
exports.sendVerification = function () {
    exec(null, null, "lsdwrapper", "sendVerification",
        []);
};
exports.dealloc = function () {
    exec(null, null, "lsdwrapper", "dealloc",
        []);
};
exports.startUDPServer = function (port, success, error) {
    exec(success, error, "lsdwrapper", "startUDPServer",
        [
            port
        ]);
};
exports.sendUDPData = function (port, data, ip, success, error) {
    exec(success, error, "lsdwrapper", "sendUDPData",
        [
            port,
            data,
            ip
        ]);
};
