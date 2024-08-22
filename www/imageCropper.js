var exec = require('cordova/exec');

var ImageCropper = {
    cropImage: function(success, error, options) {
        var defaultOptions = {
            sourceType: 'local', // 'local' or 'url'
            sourcePath: '',
            aspectRatioX: 1,
            aspectRatioY: 1,
            quality: 100
        };
        var finalOptions = Object.assign({}, defaultOptions, options);
        exec(success, error, "ImageCropper", "cropImage", [finalOptions]);
    }
};

module.exports = ImageCropper;