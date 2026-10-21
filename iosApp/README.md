# KZO PRO iOS App

Este diretório contém um scaffold de app iOS em SwiftUI para abrir uma build separada do KZO PRO.

## Estrutura
- `KZOProApp.swift`: ponto de entrada da app
- `ContentView.swift`: dashboard principal em SwiftUI
- `DeviceInfo.swift`: modelo de metadados do dispositivo

## Como abrir no Xcode
1. Abra o Xcode.
2. Crie um projeto novo do tipo "App".
3. Adicione os arquivos deste diretório ao projeto.
4. Ajuste a paleta de cores em `Assets.xcassets` ou use as cores padrão do sistema.
5. Rode no simulador ou em um iPhone real.

## Observação
Esta é uma build separada para iOS, inspirada no visual do Android, mas recursos específicos do Android (overlay, hardware diagnostics, permissões nativas) precisam ser reimplementados com APIs da Apple ou via Kotlin Multiplatform.
