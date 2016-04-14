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
    exec( null,null,"mxsdkwrapper", "sendVerification",
        []);
};
exports.dealloc = function () {
    exec( null,null,"mxsdkwrapper", "dealloc",
        []);
};
