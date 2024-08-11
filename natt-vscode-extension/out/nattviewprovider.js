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
const path = __importStar(require("path"));
const fs = __importStar(require("fs"));
class NattViewProvider {
    _extensionUri;
    _context;
    static viewType = 'homeView';
    view;
    keywordList = [];
    constructor(_extensionUri, _context) {
        this._extensionUri = _extensionUri;
        this._context = _context;
    }
    resolveWebviewView(webviewView, context, token) {
        this.view = webviewView;
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
                case 'extension.nattReload':
                    vscode.commands.executeCommand('extension.nattReload');
                    return;
            }
        }, undefined, []);
        // Listen for visibility changes
        webviewView.onDidChangeVisibility(() => {
            if (webviewView.visible) {
                this.showKeywords(undefined);
            }
        });
    }
    showKeywords(list) {
        if (!list) {
            list = this.keywordList;
        }
        if (this.view && list) {
            this.view.webview.postMessage({ command: 'loadKeywords', keywords: list });
            this.keywordList = list;
        }
    }
    getHtmlForWebview(webview) {
        const htmlPath = path.join(this._context.extensionPath, 'resources', 'home-view.html');
        let htmlContent = fs.readFileSync(htmlPath, 'utf8');
        // Replace placeholder with the actual URI
        const iconUri = webview.asWebviewUri(vscode.Uri.joinPath(this._context.extensionUri, 'resources', 'icon.svg'));
        htmlContent = htmlContent.replace('@@iconUri@@', iconUri.toString());
        return htmlContent;
    }
}
exports.default = NattViewProvider;
//# sourceMappingURL=nattviewprovider.js.map