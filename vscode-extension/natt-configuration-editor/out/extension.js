"use strict";
var __createBinding = (this && this.__createBinding) || (Object.create ? (function(o, m, k, k2) {
    if (k2 === undefined) k2 = k;
    var desc = Object.getOwnPropertyDescriptor(m, k);
    if (!desc || ("get" in desc ? !m.__esModule : desc.writable || desc.configurable)) {
      desc = { enumerable: true, get: function() { return m[k]; } };
    }
    Object.defineProperty(o, k2, desc);
}) : (function(o, m, k, k2) {
    if (k2 === undefined) k2 = k;
    o[k2] = m[k];
}));
var __setModuleDefault = (this && this.__setModuleDefault) || (Object.create ? (function(o, v) {
    Object.defineProperty(o, "default", { enumerable: true, value: v });
}) : function(o, v) {
    o["default"] = v;
});
var __importStar = (this && this.__importStar) || function (mod) {
    if (mod && mod.__esModule) return mod;
    var result = {};
    if (mod != null) for (var k in mod) if (k !== "default" && Object.prototype.hasOwnProperty.call(mod, k)) __createBinding(result, mod, k);
    __setModuleDefault(result, mod);
    return result;
};
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.activate = activate;
exports.deactivate = deactivate;
const vscode = __importStar(require("vscode"));
const fs = __importStar(require("fs"));
const path = __importStar(require("path"));
const cp = __importStar(require("child_process"));
const keywords_1 = __importDefault(require("./keywords"));
let process;
function activate(context) {
    console.log('Extension "NATT Configuration Editor" is now active!');
    // Příkaz pro vytvoření testovacího projektu
    let disposableCreate = vscode.commands.registerCommand('extension.nattInit', () => {
        const workspaceFolders = vscode.workspace.workspaceFolders;
        if (workspaceFolders && workspaceFolders.length > 0) {
            const projectPath = workspaceFolders[0].uri.fsPath;
            const sourceYamlPath = path.join(context.extensionPath, 'resources', 'test.yaml');
            const sourceJarPath = path.join(context.extensionPath, 'resources', 'NATT.jar');
            const destYamlPath = path.join(projectPath, 'test.yaml');
            const destJarPath = path.join(projectPath, 'NATT.jar');
            fs.copyFileSync(sourceYamlPath, destYamlPath);
            fs.copyFileSync(sourceJarPath, destJarPath);
            vscode.window.showInformationMessage('NATT structure initialized successfully!');
        }
    });
    // Příkaz pro spuštění testu
    let disposableRun = vscode.commands.registerCommand('extension.nattRun', () => {
        const workspaceFolders = vscode.workspace.workspaceFolders;
        if (workspaceFolders && workspaceFolders.length > 0) {
            const projectPath = workspaceFolders[0].uri.fsPath;
            const terminal = vscode.window.createTerminal({
                name: 'NATT Test',
                cwd: projectPath,
            });
            terminal.sendText('java -jar NATT.jar -c test.yaml');
            terminal.show();
        }
    });
    // Příkaz pro zastavení testu
    let disposableStop = vscode.commands.registerCommand('extension.nattStop', () => {
        if (process) {
            process.kill();
            vscode.window.showInformationMessage('Process stopped.');
        }
        else {
            vscode.window.showInformationMessage('No running process to stop.');
        }
    });
    // Příkaz pro validaci konfigurace
    let disposableValidate = vscode.commands.registerCommand('extension.nattValidate', () => {
        const workspaceFolders = vscode.workspace.workspaceFolders;
        if (workspaceFolders && workspaceFolders.length > 0) {
            const projectPath = workspaceFolders[0].uri.fsPath;
            cp.exec('java -jar NATT.jar -c test.yaml -v', { cwd: projectPath }, (error, stdout, stderr) => {
                if (error) {
                    vscode.window.showErrorMessage(stderr);
                }
                else {
                    vscode.window.showInformationMessage(stdout);
                }
            });
        }
    });
    // Vytvoření status bar tlačítek
    const runButton = vscode.window.createStatusBarItem(vscode.StatusBarAlignment.Left, 100);
    runButton.text = '$(play) NATT Run';
    runButton.command = 'extension.nattRun';
    runButton.show();
    const stopButton = vscode.window.createStatusBarItem(vscode.StatusBarAlignment.Left, 99);
    stopButton.text = '$(debug-stop) NATT Stop';
    stopButton.command = 'extension.nattStop';
    stopButton.show();
    // Register Webview Panel for Activity Bar icon
    const provider = new NattViewProvider(context.extensionUri);
    context.subscriptions.push(vscode.window.registerWebviewViewProvider('homeView', provider));
    context.subscriptions.push(disposableCreate, disposableRun, disposableStop, disposableValidate, runButton, stopButton);
}
class NattViewProvider {
    _extensionUri;
    static viewType = 'homeView';
    _view;
    constructor(_extensionUri) {
        this._extensionUri = _extensionUri;
    }
    resolveWebviewView(webviewView) {
        this._view = webviewView;
        webviewView.webview.options = {
            enableScripts: true,
            localResourceRoots: [this._extensionUri],
        };
        webviewView.webview.html = this.getHtmlForWebview(webviewView.webview);
        webviewView.webview.onDidReceiveMessage(message => {
            vscode.window.showInformationMessage(message);
            switch (message.command) {
                case 'runCommand':
                    vscode.commands.executeCommand(message.commandName);
                    return;
            }
        }, undefined, []);
    }
    getHtmlForWebview(webview) {
        const buttonStyle = `
            padding: 0.5rem; /* 8px / 16px */
            margin: 0.5rem 0; /* 8px / 16px */
            border: none; 
            background-color: #3376cd; 
            color: white; 
            cursor: pointer; 
            border-radius: 0.625rem; /* 10px / 16px */
            width: 100%; 
            box-sizing: border-box;
            font-weight: bold;
            display: inline-block;
            text-align: center;
        `;
        const iconStyle = `margin-right: 0.625rem; /* 10px / 16px */`;
        return `<!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>NATT Configuration Editor</title>
                <style>
                    body { 
                        font-family: Arial, sans-serif; 
                        padding: 1.25rem; /* 20px / 16px */
                    }

                    .header {
                        display: flex;
                        align-items: center;
                        padding: 0.625rem; /* 10px / 16px */
                        border-radius: 0.3125rem; /* 5px / 16px */
                        border-bottom: 0.125rem solid #333; /* 2px / 16px */
                        background-color: var(--vscode-sideBar-background);
                    }

                    .header img {
                        width: 3.125rem; /* 50px / 16px */
                        height: 3.125rem; /* 50px / 16px */
                        margin-right: 0.625rem; /* 10px / 16px */
                    }

                    .header .title {
                        font-size: 1.5rem; /* 24px / 16px */
                        font-weight: bold;
                    }

                    .header .subtitle {
                        font-size: 0.875rem; /* 14px / 16px */
                        color: #999;
                    }

                    .button-container {
                        padding-top: 0.625rem; /* 10px / 16px */
                    }

                    .button {
                        ${buttonStyle}
                    }

                    .button img {
                        ${iconStyle}
                    }

                    .github-link {
                        display: block;
                        margin-top: 1.25rem; /* 20px / 16px */
                        text-align: center;
                        color: #007acc;
                        text-decoration: none;
                        background-color: #222;
                        border-radius: 0.3125rem; /* 5px / 16px */
                        padding: 0.5rem; /* 8px / 16px */
                    }

                    .keywordpanel-wrapper {
                        border-radius: 0.5625rem; /* 9px / 16px */
                        border: 0.125rem solid #333; /* 2px / 16px */
                        margin-top: 0.625rem; /* 10px / 16px */
                    }

                    .keywordInfoPanel-header {
                        text-align: center;
                        color: #aaa;
                    }

                    .keywordInfoPanel {
                        display: flex;
                        flex-direction: column;
                        margin-top: 0;
                        padding: 0;
                    }

                    .keyword-list {
                        overflow-x: auto;
                        white-space: nowrap;
                        border-bottom: 0.125rem solid #333; /* 2px / 16px */
                        padding-left: 0.625rem; /* 10px / 16px */
                        padding-right: 0.625rem; /* 10px / 16px */
                        padding-bottom: 0.625rem; /* 10px / 16px */
                    }

                    .keyword-item {
                        display: inline-block;
                        padding: 0.5rem 0.625rem; /* 8px 10px / 16px */
                        margin: 0.125rem; /* 2px / 16px */
                        background-color: #3a3d33;
                        cursor: pointer;
                        border-radius: 0.25rem; /* 4px / 16px */
                        transition: background-color 0.3s, color 0.3s;
                    }

                    .keyword-item:hover,
                    .keyword-item.active {
                        background-color: #4a4d43;
                        color: #d0d0d0;
                    }

                    .keyword-description {
                        padding: 0.625rem; /* 10px / 16px */
                        overflow-y: auto;
                    }
                </style>
            </head>
            <body>
                <div class="header">
                    <img src="${webview.asWebviewUri(vscode.Uri.joinPath(this._extensionUri, 'resources', 'icon.svg'))}">
                    <div>
                        <div class="title">NATT</div>
                        <div class="subtitle">Configuration Editor</div>
                    </div>
                </div>

                <a class="github-link" href="https://github.com/0xMartin/NetworkAppTestingTool" target="_blank">Visit the GitHub Repository</a>

                <div class="button-container">
                    <button class="button" onclick="executeCommand('extension.nattInit')">
                        <span class="codicon codicon-git-init"></span>
                        Initialize 
                    </button>
                    <button class="button" onclick="executeCommand('extension.nattRun')">
                        <span class="codicon codicon-play"></span>
                        Run Test
                    </button>
                    <button class="button" onclick="executeCommand('extension.nattStop')">
                        <span class="codicon codicon-debug-stop"></span>
                        Stop Test
                    </button>
                    <button class="button" onclick="executeCommand('extension.nattValidate')">
                        <span class="codicon codicon-check"></span>
                        Validate
                    </button>
                </div>

                <div class="keywordpanel-wrapper">
                    <h3 class="keywordInfoPanel-header">Keyword description</h3>
                    <div class="keywordInfoPanel">
                        <div class="keyword-list" id="keywordList">
                            <!-- Keyword items will be injected here -->
                        </div>
                        <div class="keyword-description" id="keywordDetails">
                            <!-- Keyword details will be injected here -->
                        </div>
                    </div>
                </div>

                <script>
                    const vscode = acquireVsCodeApi();
                    const keywordDetails = ${JSON.stringify(keywords_1.default)};

                    function executeCommand(command) {
                        vscode.postMessage({ command: command });
                    }

                    function showKeywordDetails(keyword) {
                        const detailsDiv = document.getElementById('keywordDetails');
                        detailsDiv.innerHTML = '';

                        if (keyword && keywordDetails[keyword]) {
                            const details = keywordDetails[keyword];
                            const description = document.createElement('p');
                            description.textContent = details.description;
                            detailsDiv.appendChild(description);

                            const paramListTitle = document.createElement('strong');
                            paramListTitle.textContent = 'Parameters:';
                            detailsDiv.appendChild(paramListTitle);

                            const paramList = document.createElement('ul');
                            details.parameters.forEach(param => {
                                const paramItem = document.createElement('li');
                                paramItem.innerHTML = \`<strong>\${ param.name } (\${ param.type })</strong>: \${ param.description } \`;
                                paramList.appendChild(paramItem);
                            });

                            detailsDiv.appendChild(paramList);
                        }
                    }

                    showKeywordDetails("test_root");

                    function createKeywordList() {
                        const keywordListDiv = document.getElementById('keywordList');
                        for (const keyword in keywordDetails) {
                            const keywordItemDiv = document.createElement('div');
                            keywordItemDiv.className = 'keyword-item';
                            keywordItemDiv.textContent = keyword;
                            keywordItemDiv.onclick = () => showKeywordDetails(keyword);
                            keywordListDiv.appendChild(keywordItemDiv);
                        }
                    }

                    // Initialize the keyword list when the page loads
                    window.onload = createKeywordList;
                </script>
            </body>
            </html>`;
    }
}
function deactivate() { }
//# sourceMappingURL=extension.js.map