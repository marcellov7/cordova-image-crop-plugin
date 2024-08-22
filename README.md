# Cordova Image Crop Plugin

A Cordova plugin that provides image cropping functionality with a visual editor for both Android and iOS platforms. This plugin allows users to crop images from local storage or URLs with a specified aspect ratio.

## Features

- Visual editor for image cropping
- Support for both local images and image URLs
- Custom aspect ratio
- Image quality control
- Returns cropped image as Base64 string
- Cross-platform support (Android and iOS)

## Installation

```bash
cordova plugin add https://github.com/marcellov7/cordova-image-crop-plugin.git
```

## Usage

```javascript
cordova.plugins.imageCropper.cropImage(
  function(result) {
    var croppedImageBase64 = result.image;
    console.log("Cropped image (Base64): " + croppedImageBase64);
    // Use the cropped image as needed, for example:
    // document.getElementById('croppedImage').src = 'data:image/jpeg;base64,' + croppedImageBase64;
  },
  function(error) {
    console.error("Error during cropping: " + error);
  },
  {
    sourceType: 'local', // or 'url'
    sourcePath: 'path/to/image.jpg', // or URL of the image
    aspectRatioX: 16,
    aspectRatioY: 9,
    quality: 90
  }
);
```

### Options

- `sourceType`: String, either 'local' for device storage or 'url' for remote images
- `sourcePath`: String, the path to the local image or the URL of the remote image
- `aspectRatioX`: Number, the X value of the desired aspect ratio
- `aspectRatioY`: Number, the Y value of the desired aspect ratio
- `quality`: Number (0-100), the quality of the resulting JPEG image

## Platform Specific Details

### Android

This plugin uses the [uCrop](https://github.com/Yalantis/uCrop) library for Android. uCrop provides a customizable activity for image cropping.

### iOS

For iOS, the plugin utilizes [TOCropViewController](https://github.com/TimOliver/TOCropViewController), a view controller that allows users to crop UIImage objects.

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Support

If you're having any problem, please [raise an issue](https://github.com/your-username/cordova-image-crop-plugin/issues/new) on GitHub and we'll be happy to help.

## Acknowledgements

- [uCrop](https://github.com/Yalantis/uCrop) for Android image cropping
- [TOCropViewController](https://github.com/TimOliver/TOCropViewController) for iOS image cropping