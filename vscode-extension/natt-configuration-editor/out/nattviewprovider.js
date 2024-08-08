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
const vscode = __importStar(require("vscode"));
const keywords_1 = __importDefault(require("./keywords"));
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

                .keywordpanel-wrapper {
                    position: relative;
                    border-radius: 0.8rem; 
                    border: 0.125rem solid #333; /* 2px / 16px */
                    margin-top: 0.625rem; /* 10px / 16px */
                    padding: 0.625rem; /* 10px / 16px */
                }

                .keywordInfoPanel {
                    display: flex;
                    flex-direction: column;
                    overflow: auto;
                }

                .keyword-list {
                    overflow-x: auto;
                    white-space: nowrap;
                    border-bottom: 0.125rem solid #333; /* 2px / 16px */
                    padding-bottom: 0.625rem; /* 10px / 16px */
                    margin-bottom: 0.625rem; /* 10px / 16px */
                }

                .keyword-item {
                    display: inline-block;
                    padding: 0.5rem 0.625rem; /* 8px 10px / 16px */
                    margin: 0.125rem; 
                    background-color: #252525;
                    cursor: pointer;
                    border-radius: 1rem; 
                    transition: background-color 0.3s, color 0.3s;
                }

                .keyword-item:hover,
                .keyword-item.active {
                    background-color: #444444;
                    color: #d0d0d0;
                }

                .keyword-description {
                    padding: 0.625rem; /* 10px / 16px */
                    overflow-y: auto;
                }

                .keyword-description ul {
                    list-style-type: disc;
                    color: #d0d0d0;
                }

                .keyword-description ul li {
                    margin-bottom: 0.625rem; /* 10px / 16px */
                }

                .keyword-search {
                    padding: 0.5rem; /* 8px / 16px */
                    border: 0.125rem solid #333; /* 2px / 16px */
                    border-radius: 0.3125rem; /* 5px / 16px */
                    background-color: var(--vscode-input-background);
                    color: var(--vscode-input-foreground);
                    width: 100%;
                    box-sizing: border-box;
                    margin-bottom: 0.625rem; /* 10px / 16px */
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

            <div class="keywordpanel-wrapper">
                <input type="text" class="keyword-search" id="keywordSearch" placeholder="Search for a keyword..." oninput="filterKeywords()">
                <div class="keywordInfoPanel">
                    <div class="keyword-list" id="keywordList">
                        <!-- Keyword items will be injected here -->
                    </div>
                    <div class="keyword-description" id="keywordDetails">
                        <!-- Keyword details will be injected here -->
                    </div>
                </div>
            </div>

            <a class="github-link" href="https://github.com/0xMartin/NetworkAppTestingTool/blob/main/keywords.md" target="_blank">Keyword documentation</a>

            <span class="github-link"><b>NATT version:</b> 1.4.5</span>

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
                            paramItem.innerHTML = \`<strong>\${param.name} (\${param.type})</strong>: \${param.description}\`;
                            paramList.appendChild(paramItem);
                        });

                        detailsDiv.appendChild(paramList);
                    }
                }

                function filterKeywords() {
                    const searchInput = document.getElementById('keywordSearch').value.toLowerCase();
                    const keywordItems = document.getElementsByClassName('keyword-item');

                    for (let i = 0; i < keywordItems.length; i++) {
                        const item = keywordItems[i];
                        if (item.textContent.toLowerCase().includes(searchInput)) {
                            item.style.display = 'inline-block';
                        } else {
                            item.style.display = 'none';
                        }
                    }

                    const firstVisibleKeyword = Array.from(keywordItems).find(item => item.style.display === 'inline-block');
                    if (firstVisibleKeyword) {
                        showKeywordDetails(firstVisibleKeyword.textContent);
                    } else {
                        document.getElementById('keywordDetails').innerHTML = '';
                    }
                }

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

                // show base keyword
                showKeywordDetails('test_root');
            </script>
        </body>
        </html>`;
    }
}
exports.default = NattViewProvider;
//# sourceMappingURL=nattviewprovider.js.map