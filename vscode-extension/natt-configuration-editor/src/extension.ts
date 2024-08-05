import * as vscode from 'vscode';
import * as fs from 'fs';
import * as path from 'path';

import NattViewProvider from './nattviewprovider';
import ReportWebviewProvider from './reportwebviewprovider';
import keywordSnippets from './snippets';

let testTerminal: vscode.Terminal | undefined;

function checkNattJarExists(projectPath: string): boolean {
    const jarPath = path.join(projectPath, 'NATT.jar');
    return fs.existsSync(jarPath);
}

export function activate(context: vscode.ExtensionContext) {

    console.log('Extension "NATT Configuration Editor" is now active!');

    // Web view providers **************************************************************************

    // Create a webview provider instance
    const reportWebviewProvider = new ReportWebviewProvider(context.extensionUri);
    context.subscriptions.push(
        vscode.window.registerWebviewViewProvider('nattReportView', reportWebviewProvider)
    );

    // Register Webview Panel for Activity Bar icon
    const provider = new NattViewProvider(context.extensionUri);
    context.subscriptions.push(
        vscode.window.registerWebviewViewProvider('homeView', provider)
    );

    // Commands *************************************************************************************

    // Command: init test
    let disposableCreate = vscode.commands.registerCommand('extension.nattInit', () => {
        const workspaceFolders = vscode.workspace.workspaceFolders;
        if (workspaceFolders && workspaceFolders.length > 0) {
            const projectPath = workspaceFolders[0].uri.fsPath;

            const sourceYamlPath = path.join(context.extensionPath, 'resources', 'test-config.yaml');
            const sourceJarPath = path.join(context.extensionPath, 'resources', 'NATT.jar');

            const destYamlPath = path.join(projectPath, 'test-config.yaml');
            const destJarPath = path.join(projectPath, 'NATT.jar');

            // Check if the YAML file already exists
            if (!fs.existsSync(destYamlPath)) {
                fs.copyFileSync(sourceYamlPath, destYamlPath);
                vscode.window.showInformationMessage('test-config.yaml copied successfully!');
            } else {
                vscode.window.showInformationMessage('test-config.yaml already exists, skipping copy.');
            }

            // Copy the JAR file regardless of its existence
            fs.copyFileSync(sourceJarPath, destJarPath);

            vscode.window.showInformationMessage('NATT structure initialized successfully!');
        } else {
            vscode.window.showErrorMessage('No workspace folder is open.');
        }
    });

    // Command: run test
    let disposableRun = vscode.commands.registerCommand('extension.nattRun', () => {
        const workspaceFolders = vscode.workspace.workspaceFolders;
        if (workspaceFolders && workspaceFolders.length > 0) {
            const projectPath = workspaceFolders[0].uri.fsPath;
            if (checkNattJarExists(projectPath)) {
                testTerminal = vscode.window.createTerminal({
                    name: 'NATT Test',
                    cwd: projectPath,
                });
                testTerminal.sendText('java -jar NATT.jar -c test-config.yaml');
                testTerminal.show();
            } else {
                vscode.window.showErrorMessage('NATT structure has not been initialized. Please run the initialization command first.');
            }
        }
    });

    // Command: stop test
    let disposableStop = vscode.commands.registerCommand('extension.nattStop', () => {
        if (testTerminal) {
            testTerminal.sendText('exit'); // Send exit command to terminate the terminal
            testTerminal.dispose(); // Dispose the terminal to ensure it's closed
            testTerminal = undefined;
            vscode.window.showInformationMessage('Test stopped.');
        } else {
            vscode.window.showInformationMessage('No running process to stop.');
        }
    });

    // Command: show report
    let disposableShowReport = vscode.commands.registerCommand('extension.nattShow', () => {
        const panel = vscode.window.createWebviewPanel(
            'nattReportView',
            'NATT Report',
            vscode.ViewColumn.Beside,
            {
                enableScripts: true,
                retainContextWhenHidden: true
            }
        );

        panel.webview.html = reportWebviewProvider.getHtmlForWebview(panel.webview);
    });

    // Command: validate test
    let disposableValidate = vscode.commands.registerCommand('extension.nattValidate', () => {
        const workspaceFolders = vscode.workspace.workspaceFolders;
        if (workspaceFolders && workspaceFolders.length > 0) {
            const projectPath = workspaceFolders[0].uri.fsPath;
            if (checkNattJarExists(projectPath)) {
                const validateTerminal = vscode.window.createTerminal({
                    name: 'NATT Validate',
                    cwd: projectPath,
                });
                validateTerminal.sendText('java -jar NATT.jar -c test-config.yaml -v');
                validateTerminal.show();
            } else {
                vscode.window.showErrorMessage('NATT structure has not been initialized. Please run the initialization command first.');
            }
        }
    });

    // Buttons *************************************************************************************

    // Vytvoření status bar tlačítek
    const runButton = vscode.window.createStatusBarItem(vscode.StatusBarAlignment.Left, 100);
    runButton.text = '$(play) NATT Run';
    runButton.command = 'extension.nattRun';
    runButton.show();

    const stopButton = vscode.window.createStatusBarItem(vscode.StatusBarAlignment.Left, 99);
    stopButton.text = '$(debug-stop) NATT Stop';
    stopButton.command = 'extension.nattStop';
    stopButton.show();

    context.subscriptions.push(disposableCreate, disposableRun, disposableShowReport, disposableStop, disposableValidate, runButton, stopButton);

    // Other *************************************************************************************
    // Register completion item provider for YAML files with name test-config**.yaml

    const completionProvider = vscode.languages.registerCompletionItemProvider(
        { scheme: 'file', pattern: '**/test-config*.yaml' },
        {
            provideCompletionItems(document: vscode.TextDocument, position: vscode.Position) {
                return keywordSnippets.map(snippet => {
                    const item = new vscode.CompletionItem(snippet.caption, vscode.CompletionItemKind.Snippet);
                    item.insertText = snippet.snippet;
                    item.detail = snippet.meta;
                    return item;
                });
            }
        }
    );

    context.subscriptions.push(completionProvider);
}

export function deactivate() { }