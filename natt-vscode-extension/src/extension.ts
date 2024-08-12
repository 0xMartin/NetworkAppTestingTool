import * as vscode from 'vscode';
import * as fs from 'fs';
import * as path from 'path';
import * as https from 'https';
import * as stream from 'stream';
import * as cp from 'child_process';
import { promisify } from 'util';
import { IncomingMessage } from 'http';

import NattViewProvider from './nattviewprovider';
import ReportWebviewProvider from './reportwebviewprovider';

let testTerminal: vscode.Terminal | undefined;
const pipeline = promisify(stream.pipeline);
let currentCompletionProvider: vscode.Disposable | undefined;

function checkNattJarExists(projectPath: string): boolean {
    const jarPath = path.join(projectPath, 'NATT.jar');
    return fs.existsSync(jarPath);
}

/**
 * Register keyword snippets for the current version of NATT.jar. First call java -jar NATT.jar -kd. Then from the output read and parse the keywords snippets.
 * @param context VS Code context
 * @param view View where the all keywords will be displayed
 */
function registerKeywordSnippets(context: vscode.ExtensionContext, viewProvider: NattViewProvider) {
    // Remove the previous completion provider if it exists
    if (currentCompletionProvider) {
        currentCompletionProvider.dispose();
    }

    // Get the root path of the currently opened workspace
    const workspaceFolder = vscode.workspace.workspaceFolders?.[0]?.uri?.fsPath;
    if (!workspaceFolder) {
        vscode.window.showErrorMessage('No workspace folder is open.');
        return;
    }

    // Path to NATT.jar
    const jarPath = path.join(workspaceFolder, 'NATT.jar');
    
    // Directory containing NATT.jar
    const jarDirectory = path.dirname(jarPath);

    // Execute the command and capture the output
    cp.exec(`java -jar "${jarPath}" -kd`, { cwd: jarDirectory }, (error, stdout, stderr) => {
        if (error) {
            vscode.window.showErrorMessage(`Error executing command: ${error.message}`);
            return;
        }

        const keywordListMatch = stdout.match(/Documentation for registered keywords:\s*\[(.*)\]/s);
        if (!keywordListMatch) {
            vscode.window.showErrorMessage('Failed to find the keyword list in the output.');
            return;
        }

        const keywordListString = `[${keywordListMatch[1]}]`;
        let keywordList: Array<{ name: string, description: string, parameters: string[], types: string[], kwGroup: string }>;

        try {
            keywordList = JSON.parse(keywordListString);
        } catch (parseError) {
            vscode.window.showErrorMessage(`Error parsing JSON: ${parseError}`);
            return;
        }

        const keywordSnippets = keywordList.map(keyword => ({
            caption: keyword.name,
            snippet: `${keyword.name}:\n${keyword.parameters.map((param, index) => {
                const type = keyword.types[index];
                let exampleValue;
                switch (type) {
                    case 'STRING':
                        exampleValue = `"example"`;
                        break;
                    case 'LONG':
                        exampleValue = `100`;
                        break;
                    case 'DOUBLE':
                        exampleValue = `10.5`;
                        break;
                    case 'BOOLEAN':
                        exampleValue = `true`;
                        break;
                    case 'LIST':
                        exampleValue = `[]`;
                        break;
                    default:
                        exampleValue = `example value`;
                }
                return `    ${param}: ${exampleValue}`;
            }).join('\n')}`,
            meta: `${keyword.kwGroup} - ${keyword.description}`
        }));

        // Update the view with the keyword list
        viewProvider.showKeywords(keywordList);

        // Create and register the completion provider
        currentCompletionProvider = vscode.languages.registerCompletionItemProvider(
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

        // Add the new provider to the subscriptions so it's properly disposed when the extension is deactivated
        context.subscriptions.push(currentCompletionProvider);
        vscode.window.showInformationMessage("Keyword snippets registered successfully.");
    });
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
    const homeWebviewProvider = new NattViewProvider(context.extensionUri, context);
    context.subscriptions.push(
        vscode.window.registerWebviewViewProvider('homeView', homeWebviewProvider)
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

            const sourceGitLabYamlPath = path.join(context.extensionPath, 'resources', '.gitlab-ci.yml');
            const destGitLabYamlPath = path.join(projectPath, '.gitlab-ci.yml');

            // Check if the gitlab-ci file already exists
            if (!fs.existsSync(destGitLabYamlPath)) {
                fs.copyFileSync(sourceGitLabYamlPath, destGitLabYamlPath);
                vscode.window.showInformationMessage('.gitlab-ci.yml copied successfully!');
            } else {
                vscode.window.showInformationMessage('.gitlab-ci.yml already exists, skipping copy.');
            }
            // Define the URL and destination path for the JAR file
            const config = vscode.workspace.getConfiguration('natt-configuration-editor');
            const jarUrl = config.get<string>('nattJarUrl', 'https://github.com/0xMartin/NetworkAppTestingTool/releases/download/1.6.5/NATT.jar');
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

                // reload keywords
                registerKeywordSnippets(context, homeWebviewProvider);
                
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

    // Command: reload
    let disposableReload = vscode.commands.registerCommand('extension.nattReload', () => {
        registerKeywordSnippets(context, homeWebviewProvider);
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

    context.subscriptions.push(disposableCreate, disposableRun, disposableShowReport, disposableStop, disposableValidate, disposableReload, runButton, stopButton);

    // Other *************************************************************************************
    // Register completion item provider for YAML files with name test-config**.yaml

    registerKeywordSnippets(context, homeWebviewProvider);
}

export function deactivate() {
    if (currentCompletionProvider) {
        currentCompletionProvider.dispose();
    }
}