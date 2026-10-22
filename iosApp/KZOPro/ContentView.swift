import SwiftUI

struct ContentView: View {
    @State private var selectedTab: AppTab = .soc

    var body: some View {
        NavigationStack {
            ZStack {
                Color("AppBackground")
                    .ignoresSafeArea()

                VStack(spacing: 0) {
                    HeaderView()

                    Divider()
                        .frame(height: 2)
                        .overlay(
                            LinearGradient(
                                colors: [.red, .yellow, .orange],
                                startPoint: .leading,
                                endPoint: .trailing
                            )
                        )

                    TabBarView(selectedTab: $selectedTab)

                    ScrollView {
                        VStack(spacing: 18) {
                            switch selectedTab {
                            case .soc:
                                SocTabView()
                            case .system:
                                SystemTabView()
                            case .battery:
                                BatteryTabView()
                            case .sensors:
                                SensorsTabView()
                            case .about:
                                AboutTabView()
                            }
                        }
                        .padding(.horizontal, 16)
                        .padding(.vertical, 12)
                    }
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
                    .background(Color("PanelBackground"))

                    FooterView()
                }
            }
        }
    }
}

private enum AppTab: Int, CaseIterable {
    case soc
    case system
    case battery
    case sensors
    case about

    var title: String {
        switch self {
        case .soc: return "SOC"
        case .system: return "SISTEMA"
        case .battery: return "BATERIA"
        case .sensors: return "SENSORES"
        case .about: return "SOBRE"
        }
    }
}

private struct HeaderView: View {
    var body: some View {
        HStack(spacing: 16) {
            Image(systemName: "cpu")
                .font(.system(size: 28, weight: .bold))
                .foregroundStyle(.white)
                .frame(width: 48, height: 48)
                .background(Color("AccentSoft"))
                .clipShape(RoundedRectangle(cornerRadius: 12))

            Text("KZO PRO")
                .font(.system(size: 28, weight: .black))
                .foregroundStyle(.white)

            Spacer()

            Button {
                // placeholder de configuração
            } label: {
                Image(systemName: "gearshape.fill")
                    .font(.system(size: 22))
                    .foregroundStyle(.white)
                    .frame(width: 44, height: 44)
                    .background(Color("PanelBackground"))
                    .clipShape(Circle())
            }
        }
        .padding(.horizontal, 20)
        .padding(.vertical, 14)
        .background(Color("HeaderBackground"))
    }
}

private struct TabBarView: View {
    @Binding var selectedTab: AppTab

    var body: some View {
        HStack(spacing: 0) {
            ForEach(AppTab.allCases, id: \ .rawValue) { tab in
                Button {
                    selectedTab = tab
                } label: {
                    VStack(spacing: 8) {
                        Text(tab.title)
                            .font(.system(size: 11, weight: .bold))
                            .foregroundStyle(selectedTab == tab ? .yellow : .white)
                            .padding(.top, 14)
                            .padding(.bottom, 12)

                        Rectangle()
                            .frame(height: 4)
                            .foregroundStyle(selectedTab == tab ? Color("AccentGold") : .clear)
                    }
                    .frame(maxWidth: .infinity)
                }
            }
        }
        .background(Color("HeaderBackground"))
    }
}

private struct FooterView: View {
    var body: some View {
        VStack(spacing: 0) {
            Divider()
                .frame(height: 2)
                .overlay(
                    LinearGradient(
                        colors: [.red, .yellow, .orange],
                        startPoint: .leading,
                        endPoint: .trailing
                    )
                )

            Text("KZO PRO")
                .font(.system(size: 16, weight: .heavy))
                .foregroundStyle(.white)
                .padding(.vertical, 14)
        }
        .background(Color("HeaderBackground"))
    }
}

private struct StatRow: View {
    let label: String
    let value: String
    let emphasized: Bool

    var body: some View {
        VStack(spacing: 0) {
            HStack(alignment: .center) {
                Text(label)
                    .font(.system(size: 13))
                    .foregroundStyle(.white)
                    .frame(maxWidth: .infinity, alignment: .leading)

                Text(value)
                    .font(.system(size: 14, weight: emphasized ? .bold : .regular))
                    .foregroundStyle(emphasized ? Color("AccentGold") : .white)
                    .frame(maxWidth: .infinity, alignment: .trailing)
            }
            .padding(.vertical, 14)

            Divider()
                .background(Color("Divider"))
        }
    }
}

private struct ActionButton: View {
    let title: String
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            Text(title)
                .font(.system(size: 14, weight: .bold))
                .frame(maxWidth: .infinity)
                .padding(.vertical, 12)
                .foregroundStyle(Color("AccentGold"))
                .background(Color("Danger"))
                .clipShape(RoundedRectangle(cornerRadius: 10))
        }
    }
}

private struct Panel<Content: View>: View {
    let title: String
    @ViewBuilder let content: () -> Content

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text(title)
                .font(.system(size: 14, weight: .bold))
                .foregroundStyle(Color("AccentGold"))

            content()
        }
        .padding(16)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(Color("CardBackground"))
        .clipShape(RoundedRectangle(cornerRadius: 14))
    }
}

private struct SocTabView: View {
    private let info = DeviceInfo.current

    var body: some View {
        VStack(alignment: .leading, spacing: 18) {
            Panel(title: "SOC") {
                VStack(spacing: 0) {
                    StatRow(label: "Modelo do processador", value: info.model, emphasized: false)
                    StatRow(label: "Arquitetura", value: info.architecture, emphasized: false)
                    StatRow(label: "Revisão", value: info.revision, emphasized: false)
                    StatRow(label: "Processo de fabricação", value: info.processNode, emphasized: false)
                    StatRow(label: "Faixa do clock", value: "\(info.clockMin) - \(info.clockMax)", emphasized: true)
                }
            }

            Panel(title: "CLOCK POR NÚCLEO") {
                VStack(alignment: .leading, spacing: 10) {
                    Text("Clock médio: \(Int(info.averageClockPct * 100))%")
                        .font(.system(size: 15, weight: .bold))
                        .foregroundStyle(.white)

                    ProgressView(value: info.averageClockPct)
                        .tint(Color("AccentGold"))
                        .scaleEffect(x: 1, y: 2.2, anchor: .center)
                }
            }

            ActionButton(title: "ATIVAR HUD FLUTUANTE") {
                // ação futura do overlay iOS
            }

            ActionButton(title: "EXECUTAR TESTE CPU (3 SEGUNDOS)") {
                // benchmark futuro
            }
        }
    }
}

private struct SystemTabView: View {
    private let info = DeviceInfo.current

    var body: some View {
        Panel(title: "SISTEMA") {
            VStack(alignment: .leading, spacing: 12) {
                Text("• RAM: \(String(format: "%.1f", info.memoryUsageGB)) / \(String(format: "%.1f", info.totalMemoryGB)) GB (\(Int((info.memoryUsageGB / info.totalMemoryGB) * 100))%)")
                Text("• Rede: 54.7 Mbps")
                Text("• IP local: \(info.localIP)")
                Text("• Núcleos lógicos: 8")
                Text("• Sistema: iOS \(info.systemVersion) / Apple Silicon")
            }
            .font(.system(size: 13, weight: .medium))
            .foregroundStyle(.white)
            .frame(maxWidth: .infinity, alignment: .leading)
        }
    }
}

private struct BatteryTabView: View {
    private let info = DeviceInfo.current

    var body: some View {
        Panel(title: "BATERIA") {
            VStack(alignment: .leading, spacing: 10) {
                Text("• Estado: \(info.isCharging ? "Carregando" : "Em uso")")
                Text("• Nível atual: \(info.batteryLevel)%")
                Text("• Corrente atual: 840 mA")
                Text("• Pico da sessão: 1050 mA")
                Text("• Potência estimada: 14.42 W")
                Text("• Tensão: 3890 mV")
                Text("• Temperatura: 31 °C")
                Text("• Saúde: Ótima")
            }
            .font(.system(size: 13, weight: .medium))
            .foregroundStyle(.white)
        }
    }
}

private struct SensorsTabView: View {
    var body: some View {
        VStack(alignment: .leading, spacing: 18) {
            ActionButton(title: "TESTAR CONTROLE / GAMEPAD") {
                // suporte futuro para controle
            }

            ActionButton(title: "TESTAR MULTITOUCH") {
                // suporte futuro para touch
            }

            ActionButton(title: "TESTAR CORES DA TELA") {
                // painel futuro de calibração
            }

            Panel(title: "SENSORES") {
                VStack(alignment: .leading, spacing: 8) {
                    Text("Accelerometer")
                    Text("Gyroscope")
                    Text("Magnetometer")
                    Text("Proximity Sensor")
                }
                .font(.system(size: 13, weight: .medium))
                .foregroundStyle(.white)
            }
        }
    }
}

private struct AboutTabView: View {
    var body: some View {
        Panel(title: "KZO PRO • DRAGON EDITION") {
            VStack(alignment: .leading, spacing: 12) {
                Text("Monitoramento de hardware e telemetria em tempo real.")

                Link("github.com/Anonymous0000001", destination: URL(string: "https://github.com/Anonymous0000001")!)
                    .foregroundStyle(.blue)
            }
            .font(.system(size: 14))
            .foregroundStyle(.white)
        }
    }
}

#Preview {
    ContentView()
}
