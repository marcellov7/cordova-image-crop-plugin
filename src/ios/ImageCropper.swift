import Foundation
import CropViewController

@objc(ImageCropper)
class ImageCropper : CDVPlugin, CropViewControllerDelegate {
    var command: CDVInvokedUrlCommand?
    var tempFileURL: URL?
    
    @objc(cropImage:)
    func cropImage(command: CDVInvokedUrlCommand) {
        self.command = command
        
        guard let options = command.arguments[0] as? [String: Any],
              let sourceType = options["sourceType"] as? String,
              let sourcePath = options["sourcePath"] as? String,
              let aspectRatioX = options["aspectRatioX"] as? CGFloat,
              let aspectRatioY = options["aspectRatioY"] as? CGFloat,
              let quality = options["quality"] as? Int else {
            let pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: "Invalid options")
            self.commandDelegate!.send(pluginResult, callbackId: command.callbackId)
            return
        }
        
        if sourceType == "local" {
            if let image = UIImage(contentsOfFile: sourcePath) {
                self.showCropViewController(image: image, aspectRatioX: aspectRatioX, aspectRatioY: aspectRatioY, quality: quality)
            } else {
                let pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: "Failed to load local image")
                self.commandDelegate!.send(pluginResult, callbackId: command.callbackId)
            }
        } else if sourceType == "url" {
            downloadImage(from: URL(string: sourcePath)!) { [weak self] image in
                guard let self = self, let image = image else {
                    let pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: "Failed to download image from URL")
                    self?.commandDelegate!.send(pluginResult, callbackId: command.callbackId)
                    return
                }
                DispatchQueue.main.async {
                    self.showCropViewController(image: image, aspectRatioX: aspectRatioX, aspectRatioY: aspectRatioY, quality: quality)
                }
            }
        }
    }
    
    func downloadImage(from url: URL, completion: @escaping (UIImage?) -> Void) {
        URLSession.shared.dataTask(with: url) { [weak self] data, response, error in
            guard let data = data, error == nil else {
                completion(nil)
                return
            }
            
            // Save the downloaded image to a temporary file
            let tempDirectoryURL = FileManager.default.temporaryDirectory
            self?.tempFileURL = tempDirectoryURL.appendingPathComponent(UUID().uuidString).appendingPathExtension("jpg")
            
            if let tempFileURL = self?.tempFileURL {
                try? data.write(to: tempFileURL)
            }
            
            completion(UIImage(data: data))
        }.resume()
    }
    
    func showCropViewController(image: UIImage, aspectRatioX: CGFloat, aspectRatioY: CGFloat, quality: Int) {
        let cropViewController = CropViewController(image: image)
        cropViewController.delegate = self
        cropViewController.aspectRatioPreset = .custom
        cropViewController.customAspectRatio = CGSize(width: aspectRatioX, height: aspectRatioY)
        cropViewController.aspectRatioLockEnabled = true
        cropViewController.resetAspectRatioEnabled = false
        cropViewController.title = "Crop Image"
        
        if let viewController = self.viewController {
            viewController.present(cropViewController, animated: true, completion: nil)
        }
    }
    
    func cropViewController(_ cropViewController: CropViewController, didCropToImage image: UIImage, withRect cropRect: CGRect, angle: Int) {
        cropViewController.dismiss(animated: true, completion: nil)
        
        if let imageData = image.jpegData(compressionQuality: CGFloat(quality) / 100.0) {
            let base64String = imageData.base64EncodedString()
            let result: [String: Any] = ["image": base64String]
            let pluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: result)
            self.commandDelegate!.send(pluginResult, callbackId: command?.callbackId)
        } else {
            let pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: "Failed to save cropped image")
            self.commandDelegate!.send(pluginResult, callbackId: command?.callbackId)
        }
        
        if let tempFileURL = self.tempFileURL {
            try? FileManager.default.removeItem(at: tempFileURL)
            self.tempFileURL = nil
        }
    }
}