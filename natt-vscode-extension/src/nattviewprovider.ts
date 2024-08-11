import * as vscode from 'vscode';
import * as path from 'path';
import * as fs from 'fs';

class NattViewProvider implements vscode.WebviewViewProvider {
    public static readonly viewType = 'homeView';
    public view?: vscode.WebviewView;

    private keywordList: any[] = [];

    constructor(private readonly _extensionUri: vscode.Uri, private readonly _context: vscode.ExtensionContext) { }

    resolveWebviewView(webviewView: vscode.WebviewView, context: vscode.WebviewViewResolveContext, token: vscode.CancellationToken) {
        this.view = webviewView;

        webviewView.webview.options = {
            enableScripts: true,
            localResourceRoots: [this._extensionUri],
        };

        webviewView.webview.html = this.getHtmlForWebview(webviewView.webview);

        webviewView.webview.onDidReceiveMessage(
            message => {
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
            },
            undefined,
            []
        );

        // Listen for visibility changes
        webviewView.onDidChangeVisibility(() => {
            if (webviewView.visible) {
                this.showKeywords(undefined);
            }
        });
    }

    public showKeywords(list?: any[]) {
        if(!list) {
            list = this.keywordList; 
        }
        if (this.view && list) {
            this.view.webview.postMessage({ command: 'loadKeywords', keywords: list });
            this.keywordList = list;
        }
    }

    private getHtmlForWebview(webview: vscode.Webview): string {
        const htmlPath = path.join(this._context.extensionPath, 'resources', 'home-view.html');
        let htmlContent = fs.readFileSync(htmlPath, 'utf8');

        // Replace placeholder with the actual URI
        const iconUri = webview.asWebviewUri(vscode.Uri.joinPath(this._context.extensionUri, 'resources', 'icon.svg'));
        htmlContent = htmlContent.replace('@@iconUri@@', iconUri.toString());

        return htmlContent;
    }

}

export default NattViewProvider;