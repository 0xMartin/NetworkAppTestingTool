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
class ReportWebviewProvider {
    _view;
    _extensionUri;
    constructor(extensionUri) {
        this._extensionUri = extensionUri;
    }
    resolveWebviewView(webviewView, context, token) {
        this._view = webviewView;
        // Set up the webview options
        webviewView.webview.options = {
            enableScripts: true,
            localResourceRoots: [this._extensionUri],
        };
        // Load the HTML content
        webviewView.webview.html = this.getHtmlForWebview(webviewView.webview);
    }
    getHtmlForWebview(webview) {
        const reportPath = path.join(vscode.workspace.workspaceFolders[0].uri.fsPath, 'test_report.html');
        vscode.window.showInformationMessage('Showing report: ' + reportPath);
        return `<!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Test Report</title>
            </head>
            <body style="padding: 0px">
                ${fs.readFileSync(reportPath, 'utf8')}
            </body>
            </html>`;
    }
}
exports.default = ReportWebviewProvider;
//# sourceMappingURL=reportwebviewprovider.js.map