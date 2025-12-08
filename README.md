# VecTor 📐

**VecTor** is a modern, Java-based desktop application designed for Linear Algebra operations. It combines symbolic mathematics, real-time LaTeX rendering, and interactive 2D / 3D geometric visualizations to help students and engineers understand matrix transformations.

![Project Status](https://img.shields.io/badge/status-active-success.svg)
![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=flat&logo=openjdk&logoColor=white)
[![Installer (.exe)](https://img.shields.io/badge/Installer-.exe-blue?style=flat&logo=windows)](https://github.com/Davd-B/VecTor-Page/releases/latest/download/VecTor-Installer.exe)
[![Web DEMO](https://img.shields.io/badge/Web-Demo-brightgreen?style=flat&logo=html5)](https://davd-b.github.io/VecTor-Page/web/)

## 📑 Table of Contents
- [Quick Start](#-quick-start-download--run)
- [Killer Features](#-killer-features)
- [Development Setup](#-development-setup-intellij-idea)
- [Usage Guide](#-usage-guide)
- [Project Structure](#-project-structure)
- [Contributing](#-contributing)
- [License](#-license)

---

## 🚀 Quick Start (Download & Run)

Don't want to compile the code? You can simply download the application.

1.  Go to the **[Releases Page](../../releases)**.
2.  Download the latest `.exe` file (Windows).
3.  Double-click the file to launch VecTor.

*Note: You might need a Java Runtime Environment (JRE) installed on your machine.*

---

## 🏗 Killer Features

### 🧮 Symbolic & Numeric Calculation
*   **Smart Parsing:** Define variables (e.g., `A`, `x`) and perform operations like `A * x` or `A + B`.
*   **Mixed Mode:** Works with both raw numbers and symbolic variables (e.g., matrices containing `\lambda`).
*   **Transposition:** Supports matrix transposition syntax (e.g., `A^T`).
*   **Implicit Multiplication:** Understands `2x` or `Av` without needing explicit multiplication signs.

### 🎨 Modern UI & LaTeX Rendering
*   **Real-time Rendering:** Converts text input (like `alpha`, `beta`, `matrix`) into beautiful LaTeX formulas.
*   **Dark/Light Mode:** Toggle between themes for comfortable viewing.
*   **Formula History:** Keeps a scrollable log of all your calculations.
*   **Zoomable Interface:** Dynamic font scaling using `Ctrl +` and `Ctrl -`.

### 📊 Interactive 2D and 3D Visualizer
*   **Linear Transformations:** Visualize how a matrix transforms space by manipulating basis vectors $\hat{i}$, $\hat{j}$ and $\hat{k}$.
*   **Animation:** Watch smooth interpolations between the Identity Matrix and your custom transformation.

---

## 💻 Development Setup (IntelliJ IDEA)

If you want to modify the code or contribute, here is how to set up the project in IntelliJ IDEA.

### Prerequisites
1.  **IntelliJ IDEA** (Community or Ultimate).
2.  **JDK 8** or higher (Java 11+ recommended).
3.  **JLaTeXMath Library:** Download the `jlatexmath-x.x.x.jar` from [Scilab Forge](https://github.com/scilab/jlatexmath) or Maven Central.

### Steps to Import
1.  **Clone the Repo:**
    ```bash
    git clone https://github.com/yourusername/VecTor.git
    ```
2.  **Open in IntelliJ:**
    *   Launch IntelliJ IDEA.
    *   Click **Open** (or `File` > `Open`).
    *   Select the `VecTor` folder you just cloned.
3.  **Configure Dependencies (Important):**
    *   Go to `File` > `Project Structure` (or press `Ctrl+Alt+Shift+S`).
    *   Select **Modules** in the left sidebar.
    *   Go to the **Dependencies** tab.
    *   Click the **`+`** icon and select **JARs or Directories**.
    *   Locate and select the `jlatexmath.jar` file you downloaded.
    *   Check the "Export" box (optional, but good practice) and click **Apply/OK**.
4.  **Set Source Folder:**
    *   In the Project view, right-click the `src` folder.
    *   Select `Mark Directory as` > `Sources Root` (if it isn't already marked in blue).
5.  **Run the App:**
    *   Navigate to `src/org/example/MathEditorApp.java`.
    *   Right-click inside the file and select **Run 'MathEditorApp.main()'**.

---

## 📖 Usage Guide

### Defining Matrices
1. Type `A =` (or any variable name followed by `=`) in the input bar.
2. Press **Enter**.
3. A dialog will appear. Enter the dimensions (Rows x Cols) and fill in the grid.
   * *Note: You can leave cells empty for 0, or type text for symbolic variables.*

### Performing Calculations
Once variables are defined, you can perform operations in the input bar:
*   **Addition/Subtraction:** `A + B`
*   **Multiplication:** `A * B` or simply `A B`
*   **Scalar Multiplication:** `2 * A` or `2A`
*   **Transposition:** `A^T`

### The 2D Visualizer
Click the **"2D"** button in the top-right corner to open the Visualizer.
*   **Red Vector:** Basis vector $\hat{i}$ (x-axis).
*   **Green Vector:** Basis vector $\hat{j}$ (y-axis).
*   **Blue Vector:** Result vector $\vec{a}$.
*   **Animation:** Click "Play Animation" to see the transformation happen gradually from the Identity matrix state.

---

## 📂 Project Structure

*   **`MathEditorApp.java`**: The main entry point. Manages the GUI, themes, and history.
*   **`MathEvaluator.java`**: The core logic engine. Parses mathematical expressions and performs matrix operations.
*   **`SmartMathParser.java`**: Handles text-to-LaTeX conversion, variable substitution, and Greek letter mapping.
*   **`VectorVisualizer2D.java`**: A Swing component that draws the interactive Cartesian plane and handles vector animations.
*   **`MatrixAssignerDialog.java`**: The pop-up GUI for defining matrix dimensions and values.

---

## 🤝 Contributing

Contributions are welcome!
1. Fork the project.
2. Create your feature branch (`git checkout -b feature/AmazingFeature`).
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`).
4. Push to the branch (`git push origin feature/AmazingFeature`).
5. Open a Pull Request.

## 📄 License

Distributed under the MIT License. See `LICENSE` for more information.

---

*Made with ❤️ and Java Swing. This legacy version is intended as a student project at the University of Genoa - Italy*

![Application Demo](img/video2.gif)
