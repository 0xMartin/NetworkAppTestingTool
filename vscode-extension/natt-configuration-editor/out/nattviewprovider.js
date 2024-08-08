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
Object.defineProperty(exports, "__esModule", { value: true });
const vscode = __importStar(require("vscode"));
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
            switch (message.command) {
                case 'extension.nattInit':
                    vscode.commands.executeCommand('extension.nattInit');
                    return;
                case 'extension.nattRun':
                    vscode.commands.executeCommand('extension.nattRun');
                    return;
                case 'extension.nattStop':
                    vscode.commands.executeCommand('extension.nattStop');
                    return;
                case 'extension.nattShow':
                    vscode.commands.executeCommand('extension.nattShow');
                    return;
                case 'extension.nattValidate':
                    vscode.commands.executeCommand('extension.nattValidate');
                    return;
            }
        }, undefined, []);
    }
    getHtmlForWebview(webview) {
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
                    margin: 0;
                    box-sizing: border-box;
                }

                .header {
                    display: flex;
                    align-items: center;
                    padding: 0.625rem; /* 10px / 16px */
                    border-radius: 0.3125rem; /* 5px / 16px */
                    border-bottom: 0.125rem solid #333; /* 2px / 16px */
                    background-color: var(--vscode-sideBar-background);
                    margin-bottom: 1.25rem; /* 20px / 16px */
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
                    margin-bottom: 1.25rem; /* 20px / 16px */
                }

                .button {
                    padding: 0.5rem; /* 8px / 16px */
                    margin: 0.5rem 0; /* 8px / 16px */
                    border: none; 
                    background-color: #006abb; 
                    color: white; 
                    cursor: pointer; 
                    border-radius: 0.625rem; /* 10px / 16px */
                    width: 100%; 
                    box-sizing: border-box;
                    font-weight: bold;
                    display: inline-block;
                    text-align: center;
                }

                .button:hover {
                    background-color: #209aef; 
                }

                .button img {
                    margin-right: 0.625rem; /* 10px / 16px */
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
                <button class="button" onclick="executeCommand('extension.nattShow')">
                    <span class="codicon codicon-debug-stop"></span>
                    Show Report
                </button>
                <button class="button" onclick="executeCommand('extension.nattValidate')">
                    <span class="codicon codicon-check"></span>
                    Validate
                </button>
            </div>

            <a class="github-link" href="https://github.com/0xMartin/NetworkAppTestingTool/blob/main/keywords.md" target="_blank">Keyword documentation</a>

            <script>
                const vscode = acquireVsCodeApi();

                function executeCommand(command) {
                    vscode.postMessage({ command: command });
                }

                // Initialize the keyword list when the page loads
                window.onload = createKeywordList;
            </script>
        </body>
        </html>`;
    }
}
exports.default = NattViewProvider;
//# sourceMappingURL=nattviewprovider.js.map