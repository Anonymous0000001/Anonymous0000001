import Foundation

struct DeviceInfo {
    let model: String
    let architecture: String
    let revision: String
    let processNode: String
    let clockMin: String
    let clockMax: String
    let averageClockPct: Double
}

enum DeviceProfile {
    static let current = DeviceInfo(
        model: "A17 Pro",
        architecture: "ARM v9",
        revision: "0x00",
        processNode: "4 nm",
        clockMin: "1.2GHz",
        clockMax: "3.4GHz",
        averageClockPct: 0.72
    )
}
