import * as vscode from 'vscode';
import * as path from 'path';
import * as fs from 'fs';

class ReportWebviewProvider implements vscode.WebviewViewProvider {
    private _view?: vscode.WebviewView;
    private _extensionUri: vscode.Uri;

    constructor(extensionUri: vscode.Uri) {
        this._extensionUri = extensionUri;
    }

    resolveWebviewView(webviewView: vscode.WebviewView, context: vscode.WebviewViewResolveContext, token: vscode.CancellationToken) {
        this._view = webviewView;

        // Set up the webview options
        webviewView.webview.options = {
            enableScripts: true,
            localResourceRoots: [this._extensionUri],
        };

        // Load the HTML content
        webviewView.webview.html = this.getHtmlForWebview(webviewView.webview);
    }

    public getHtmlForWebview(webview: vscode.Webview): string {
        const reportPath = path.join(vscode.workspace.workspaceFolders![0].uri.fsPath, 'test_report.html');
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

export default ReportWebviewProvider;