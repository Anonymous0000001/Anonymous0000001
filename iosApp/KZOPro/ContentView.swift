import SwiftUI

struct ContentView: View {
    @State private var selectedTab = 0

    var body: some View {
        NavigationStack {
            ZStack {
                Color("AppBackground").ignoresSafeArea()

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
                            case 0:
                                SocTabView()
                            case 1:
                                SystemTabView()
                            case 2:
                                BatteryTabView()
                            case 3:
                                SensorsTabView()
                            default:
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
                // placeholder
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
    @Binding var selectedTab: Int
    let tabs = ["SOC", "SISTEMA", "BATERIA", "SENSORES", "SOBRE"]

    var body: some View {
        HStack(spacing: 0) {
            ForEach(Array(tabs.enumerated()), id: \ .offset) { index, title in
                Button {
                    selectedTab = index
                } label: {
                    VStack(spacing: 8) {
                        Text(title)
                            .font(.system(size: 11, weight: .bold))
                            .foregroundStyle(selectedTab == index ? .yellow : .white)
                            .padding(.top, 14)
                            .padding(.bottom, 12)

                        Rectangle()
                            .frame(height: 4)
                            .foregroundStyle(selectedTab == index ? Color("AccentGold") : .clear)
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
    let emphasized: Bool = false

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

private struct Panel: View {
    let title: String
    @ViewBuilder let content: () -> any View

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text(title)
                .font(.system(size: 14, weight: .bold))
                .foregroundStyle(Color("AccentGold"))

            AnyView(content())
        }
        .padding(16)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(Color("CardBackground"))
        .clipShape(RoundedRectangle(cornerRadius: 14))
    }
}

private struct SocTabView: View {
    var body: some View {
        VStack(alignment: .leading, spacing: 18) {
            Panel(title: "SOC") {
                VStack(spacing: 0) {
                    StatRow(label: "Modelo do processador", value: "A17 Pro")
                    StatRow(label: "Arquitetura", value: "ARM v9")
                    StatRow(label: "Revisão", value: "0x00")
                    StatRow(label: "Processo de fabricação", value: "4 nm")
                    StatRow(label: "Faixa do clock", value: "1.2GHz - 3.4GHz", emphasized: true)
                }
            }

            Panel(title: "CLOCK POR NÚCLEO") {
                VStack(alignment: .leading, spacing: 10) {
                    Text("Clock médio: 72%")
                        .font(.system(size: 15, weight: .bold))
                        .foregroundStyle(.white)

                    ProgressView(value: 0.72)
                        .tint(Color("AccentGold"))
                        .scaleEffect(x: 1, y: 2.2, anchor: .center)
                }
            }

            ActionButton(title: "ATIVAR HUD FLUTUANTE") {
                // ação futura do overlay iOS
            }

            ActionButton(title: "EXECUTAR TESTE CPU (3 SEGUNDOS)") {
                // ação futura de benchmark
            }
        }
    }
}

private struct SystemTabView: View {
    var body: some View {
        Panel(title: "SISTEMA") {
            VStack(alignment: .leading, spacing: 12) {
                Text("• RAM: 8.1 / 12.0 GB (68%)")
                Text("• Rede: 54.7 Mbps")
                Text("• IP local: 192.168.0.25")
                Text("• Núcleos lógicos: 8")
                Text("• Sistema: iOS 18 / Apple Silicon")
            }
            .font(.system(size: 13, weight: .medium))
            .foregroundStyle(.white)
            .frame(maxWidth: .infinity, alignment: .leading)
        }
    }
}

private struct BatteryTabView: View {
    var body: some View {
        Panel(title: "BATERIA") {
            VStack(alignment: .leading, spacing: 10) {
                Text("• Estado: Carregando")
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
                // future controller support
            }

            ActionButton(title: "TESTAR MULTITOUCH") {
                // future touch testing
            }

            ActionButton(title: "TESTAR CORES DA TELA") {
                // future palette testing
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
