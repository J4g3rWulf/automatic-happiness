# RecycleApp ♻️

> Este repositório (`automatic-happiness`) contém o código do **RecycleApp**, um aplicativo Android que usa **rede neural (TensorFlow Lite)** para identificar o tipo de lixo a partir de uma foto e orientar o descarte correto do material.

---

## 📱 Visão geral

O RecycleApp permite que o usuário:

1. Tire uma foto do lixo **na hora** ou escolha uma imagem da **galeria**;
2. Confirme se a foto está correta;
3. Aguarde a análise da imagem por uma rede neural embarcada (TensorFlow Lite);
4. Veja o resultado com o tipo de material detectado (**Vidro, Papel, Plástico, Metal ou Indefinido**) e uma tela educativa de descarte.

Toda a classificação é feita **localmente no aparelho**, sem enviar a imagem para servidores externos e **sem necessidade de internet**.

---

## ✨ Funcionalidades principais

- Tela inicial com duas ações principais:
  - **Tirar foto** (câmera);
  - **Usar da galeria** (seletor nativo de imagens).
- Fluxo completo de captura:
  - Permissão de câmera quando necessário;
  - Criação de arquivo temporário via `FileProvider`;
  - Tratamento do cancelamento (volta para a Home e apaga arquivos temporários).
- Tela de **confirmação da foto** antes de enviar para a IA.
- Tela de **carregamento** com animação, enquanto o modelo de rede neural é executado em background.
- Integração com modelo **TensorFlow Lite (`model_v03.tflite`)**:
  - Redimensiona a imagem para 256×256;
  - Converte pixels para `ByteBuffer`;
  - Executa o modelo e obtém as probabilidades;
  - Agrupa as classes finas em 4 materiais (vidro, papel, plástico, metal).
- Tela de **resultado**:
  - Frase “O material é…” com o tipo identificado;
  - Paleta de cores e ícone de lixeira específicos para cada material;
  - Mensagem de orientação para descarte correto;
  - Layout de “mapa” (mock) para futuros pontos de descarte;
  - Tratamento de erro com o material **“Indefinido”**.
- Botão **“Novo Lixo”** que limpa o fluxo e retorna para a tela inicial.

---

## 🧠 Como funciona a IA (resumo)

- O modelo `model_v03.tflite` recebe uma imagem **256×256 RGB**.
- Ele foi treinado para 10 classes finas, por exemplo:
  - `glass_bottle`, `glass_cup`, `metal_can`, `paper_bag`, `paper_ball`, `plastic_bottle`, etc.
- No app, a classe de saída é convertida em um dos **4 materiais**:

  - Vidro  
  - Metal  
  - Papel  
  - Plástico  

- O arquivo `TrashClassifier.kt` cuida de:
  - Ler a imagem a partir da URI;
  - Redimensionar e montar o `ByteBuffer`;
  - Rodar o `Interpreter` do TensorFlow Lite;
  - Mapear o índice de maior probabilidade para o texto exibido na interface (em português).

---

## 🧱 Tecnologias e bibliotecas utilizadas

- **Linguagem:** Kotlin  
- **Interface:** Jetpack Compose + Material 3  
  - Tema em `ui/theme/Theme.kt`  
  - Telas em `ui/screens/*`
- **Navegação:** Navigation Compose  
  - Arquivo `navigation/AppNav.kt` com o `NavHost` e as rotas.
- **Carregamento de imagens:** [Coil](https://coil-kt.github.io/coil/)  
  - Ex.: exibição da foto em `ConfirmPhotoScreen.kt` com `rememberAsyncImagePainter`.
- **IA local:** TensorFlow Lite  
  - Dependências `tensorflow-lite` e `tensorflow-lite-support` declaradas em `build.gradle.kts`.
  - Integração em `util/TrashClassifier.kt`.
- **Câmera e galeria:** Activity Result API  
  - Câmera com `ActivityResultContracts.TakePicture()` em `CameraCaptureScreen.kt`;  
  - Galeria com `ActivityResultContracts.PickVisualMedia()` (Photo Picker) em `GalleryPickerScreen.kt`.
- **Gerenciamento de arquivos temporários:** `FileProvider` + funções utilitárias  
  - Configuração em `AndroidManifest.xml` + `xml/file_paths.xml`;  
  - Helpers em `util/UriUtils.kt`.
- **Splash screen nativa:** `androidx.core.splashscreen`  
  - Tema configurado em `res/values/themes.xml`.

---

## 🗂 Estrutura simplificada do projeto

```text
app/
 ├─ src/main/
 │   ├─ java/com/example/recycleapp/
 │   │   ├─ MainActivity.kt           # Activity única do app
 │   │   ├─ navigation/AppNav.kt      # NavHost + rotas
 │   │   ├─ ui/
 │   │   │   ├─ screens/              # Telas em Compose
 │   │   │   │   ├─ HomeScreen.kt
 │   │   │   │   ├─ CameraCaptureScreen.kt
 │   │   │   │   ├─ GalleryPickerScreen.kt
 │   │   │   │   ├─ ConfirmPhotoScreen.kt
 │   │   │   │   ├─ LoadingScreen.kt
 │   │   │   │   └─ ResultScreen.kt
 │   │   │   └─ theme/                # Cores e tipografia
 │   │   └─ util/
 │   │       ├─ TrashClassifier.kt    # Integração com TensorFlow Lite
 │   │       └─ UriUtils.kt           # Manipulação de URIs/arquivos temporários
 │   ├─ res/
 │   │   ├─ drawable/                 # Ícones, ilustrações e logo
 │   │   ├─ font/                     # Fontes Poppins
 │   │   ├─ values/                   # strings.xml, colors.xml, themes.xml
 │   │   └─ xml/                      # file_paths.xml, backup rules
 │   └─ assets/
 │       └─ model_v03.tflite          # Modelo de rede neural
 └─ build.gradle.kts                  # Configuração do módulo app
```

---

## 🚀 Como executar o projeto localmente

1. Pré-requisitos
   - Android Studio (Hedgehog/Koala ou superior);
   - JDK 11;
   - Emulador Android ou dispositivo físico (API 24+).
     
2. Clonar o repositório
   ➜ git clone https://github.com/SEU-USUARIO/automatic-happiness.git
   ➜ cd automatic-happiness
   
3. Abrir no Android Studio
   - File > Open... e selecione a pasta do projeto;
   - Aguarde o Gradle sincronizar.
     
7. Executar
   - Escolha um dispositivo (emulador ou físico);
   - Clique em Run ▶ na MainActivity ou no módulo app.

---

## 📦 Download do APK

Para instalar diretamente no celular, sem precisar abrir o projeto no Android Studio:

👉 [Download do APK (Release v1.0.0)](../../releases/latest)

Baixe o arquivo app-release.apk da última release e instale no dispositivo Android.

---

## 🧩 Melhorias futuras
   - Implementar o mapa da tela de resultado com pontos reais de descarte/reciclagem;
   - Armazenar histórico de classificações no dispositivo;
   - Suporte a mais tipos de resíduos e modelos de IA mais robustos;
   - Internacionalização (tradução para outros idiomas).

---

## 👥 Equipe

Projeto desenvolvido como parte do TCC do curso de Cinência da Computação – Universidade Veiga de Almeida.

- **Desenvolvimento do aplicativo (Android + IA)**
  🧑‍💻 Gianluca do Nascimento Paz

- **Apoio ao projeto (documentação, testes, validação e revisões)**
  🧑‍💻 Caio Marcelino Gomes
  🧑‍💻 Davi Millan Alves
  🧑‍💻 Diogo Garofe Tumiati
  🧑‍💻 Gabriel Mesquita Gusmão
