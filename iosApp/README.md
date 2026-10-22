# KZO PRO iOS App

Este diretório contém a versão iOS do KZO PRO em SwiftUI, com uma interface inspirada no painel de diagnóstico do Android e estrutura pronta para expansão com monitoramento de hardware, bateria, sensores e dashboard de sistema.

## Estrutura
- `KZOProApp.swift`: ponto de entrada da app
- `ContentView.swift`: dashboard principal em SwiftUI
- `DeviceInfo.swift`: modelo de metadados do dispositivo e dados do ambiente

## Como abrir no Xcode
1. Abra o Xcode.
2. Crie um novo projeto do tipo "App".
3. Adicione os arquivos deste diretório ao projeto.
4. Se preferir, crie um `Assets.xcassets` para definir `AppBackground`, `PanelBackground`, `HeaderBackground`, `CardBackground`, `AccentGold`, `AccentSoft`, `Divider`, `Danger` e `Color` customizadas.
5. Execute no simulador ou em um iPhone real.

## Observação
Esta é a versão iOS da interface principal do KZO PRO. Recursos específicos do Android, como overlay nativo, acesso completo a hardware e permissões de sistema, precisam ser reimplementados com APIs do iOS ou integrações específicas por plataforma.
