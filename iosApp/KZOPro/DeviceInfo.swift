import Foundation
import UIKit

struct DeviceInfo {
    let model: String
    let architecture: String
    let revision: String
    let processNode: String
    let clockMin: String
    let clockMax: String
    let averageClockPct: Double
    let memoryUsageGB: Double
    let totalMemoryGB: Double
    let localIP: String
    let batteryLevel: Int
    let isCharging: Bool
    let systemVersion: String
}

extension UIDevice {
    var modelName: String {
        if let simulatorModel = ProcessInfo.processInfo.environment["SIMULATOR_MODEL_IDENTIFIER"] {
            return simulatorModel
        }
        return UIDevice.current.model
    }
}

extension DeviceInfo {
    static let current = DeviceInfo(
        model: UIDevice.current.modelName,
        architecture: "ARM v9",
        revision: "0x00",
        processNode: "4 nm",
        clockMin: "1.2 GHz",
        clockMax: "3.4 GHz",
        averageClockPct: 0.72,
        memoryUsageGB: 8.1,
        totalMemoryGB: 12.0,
        localIP: "192.168.0.25",
        batteryLevel: 78,
        isCharging: true,
        systemVersion: UIDevice.current.systemVersion
    )
}
