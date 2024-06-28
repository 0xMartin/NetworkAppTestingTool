# 🔧 NATT Configuration editor

**[Go Back](../README.md)**

The testing tool has its own configuration language. The language is written in YAML format. You can write the configuration in your own editor or in the editor available in this repository specifically for this black box testing tool. The editor allows for easy configuration writing with code auto-completion and many other features. It is also possible to run tests directly within the editor.

## Python Package Requirements

Ensure the following Python packages are installed. You can install them using pip:

* **Flask:** A lightweight WSGI web application framework.

```bash
    pip install Flask
```

* **Flask-SocketIO:** A Flask extension that enables WebSocket communication.

```bash
    pip install flask-socketio
```

## How to run?

Run configuration editor on **Linux**:

```bash
cd config-editor

chmod +x run.sh

./run.sh
```

Run configuration editor on **Windows**:

```bash
cd config-editor

start run.bat
```

## Preview

> Editor layout with output terminal

<img src="../doc/editor_1.png" alt="Configuration editor">

> Editor layout with final test report

<img src="../doc/editor_2.png" alt="Configuration editor">
