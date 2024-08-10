import * as vscode from 'vscode';
import * as fs from 'fs';
import * as path from 'path';
import * as https from 'https';
import * as stream from 'stream';
import { promisify } from 'util';
import { IncomingMessage } from 'http';

import NattViewProvider from './nattviewprovider';
import ReportWebviewProvider from './reportwebviewprovider';
import keywordSnippets from './snippets';

let testTerminal: vscode.Terminal | undefined;
const pipeline = promisify(stream.pipeline);

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
    let disposableCreate = vscode.commands.registerCommand('extension.nattInit', async () => {
        const workspaceFolders = vscode.workspace.workspaceFolders;
        if (workspaceFolders && workspaceFolders.length > 0) {
            const projectPath = workspaceFolders[0].uri.fsPath;

            vscode.window.showInformationMessage('NATT initialization started!');

            const sourceYamlPath = path.join(context.extensionPath, 'resources', 'test-config.yaml');
            const destYamlPath = path.join(projectPath, 'test-config.yaml');

            // Check if the YAML file already exists
            if (!fs.existsSync(destYamlPath)) {
                fs.copyFileSync(sourceYamlPath, destYamlPath);
                vscode.window.showInformationMessage('test-config.yaml copied successfully!');
            } else {
                vscode.window.showInformationMessage('test-config.yaml already exists, skipping copy.');
            }

            // Define the URL and destination path for the JAR file
            const config = vscode.workspace.getConfiguration('natt-configuration-editor');
            const jarUrl = config.get<string>('nattJarUrl', 'https://github.com/0xMartin/NetworkAppTestingTool/releases/download/1.5.1/NATT.jar');
            const destJarPath = path.join(projectPath, 'NATT.jar');

            // Function to download the file
            const downloadFile = async (url: string, dest: string) => {
                return new Promise<void>((resolve, reject) => {
                    const handleResponse = (response: IncomingMessage) => {
                        if (response.statusCode === 200) {
                            pipeline(response, fs.createWriteStream(dest)).then(resolve).catch(reject);
                        } else if (response.statusCode === 302 || response.statusCode === 301) {
                            const redirectUrl = response.headers.location;
                            if (redirectUrl) {
                                downloadFile(redirectUrl, dest).then(resolve).catch(reject);
                            } else {
                                reject(new Error('Redirect location not found'));
                            }
                        } else {
                            reject(new Error(`Failed to get '${url}' (${response.statusCode})`));
                        }
                    };

                    https.get(url, handleResponse).on('error', reject);
                });
            };

            try {
                // Use vscode.window.withProgress to show a loading bar during the download
                await vscode.window.withProgress({
                    location: vscode.ProgressLocation.Notification,
                    title: "Downloading NATT.jar",
                    cancellable: false
                }, async (progress, token) => {
                    progress.report({ message: "Starting download..." });

                    await downloadFile(jarUrl, destJarPath);

                    progress.report({ message: "Download complete!" });
                });

                vscode.window.showInformationMessage('NATT.jar downloaded successfully. Setup complete!');
            } catch (error) {
                vscode.window.showErrorMessage(`Failed to download NATT.jar: ${error}`);
            }
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
    // Create status bar buttons

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