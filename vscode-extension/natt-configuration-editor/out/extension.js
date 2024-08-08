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
const https = __importStar(require("https"));
const stream = __importStar(require("stream"));
const util_1 = require("util");
const nattviewprovider_1 = __importDefault(require("./nattviewprovider"));
const reportwebviewprovider_1 = __importDefault(require("./reportwebviewprovider"));
const snippets_1 = __importDefault(require("./snippets"));
let testTerminal;
const pipeline = (0, util_1.promisify)(stream.pipeline);
function checkNattJarExists(projectPath) {
    const jarPath = path.join(projectPath, 'NATT.jar');
    return fs.existsSync(jarPath);
}
function activate(context) {
    console.log('Extension "NATT Configuration Editor" is now active!');
    // Web view providers **************************************************************************
    // Create a webview provider instance
    const reportWebviewProvider = new reportwebviewprovider_1.default(context.extensionUri);
    context.subscriptions.push(vscode.window.registerWebviewViewProvider('nattReportView', reportWebviewProvider));
    // Register Webview Panel for Activity Bar icon
    const provider = new nattviewprovider_1.default(context.extensionUri);
    context.subscriptions.push(vscode.window.registerWebviewViewProvider('homeView', provider));
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
            }
            else {
                vscode.window.showInformationMessage('test-config.yaml already exists, skipping copy.');
            }
            // Define the URL and destination path for the JAR file
            const config = vscode.workspace.getConfiguration('natt-configuration-editor');
            const jarUrl = config.get('nattJarUrl', 'https://github.com/0xMartin/NetworkAppTestingTool/releases/download/1.5.0/NATT.jar');
            const destJarPath = path.join(projectPath, 'NATT.jar');
            // Function to download the file
            const downloadFile = async (url, dest) => {
                return new Promise((resolve, reject) => {
                    const handleResponse = (response) => {
                        if (response.statusCode === 200) {
                            pipeline(response, fs.createWriteStream(dest)).then(resolve).catch(reject);
                        }
                        else if (response.statusCode === 302 || response.statusCode === 301) {
                            const redirectUrl = response.headers.location;
                            if (redirectUrl) {
                                downloadFile(redirectUrl, dest).then(resolve).catch(reject);
                            }
                            else {
                                reject(new Error('Redirect location not found'));
                            }
                        }
                        else {
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
            }
            catch (error) {
                vscode.window.showErrorMessage(`Failed to download NATT.jar: ${error}`);
            }
        }
        else {
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
            }
            else {
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
        }
        else {
            vscode.window.showInformationMessage('No running process to stop.');
        }
    });
    // Command: show report
    let disposableShowReport = vscode.commands.registerCommand('extension.nattShow', () => {
        const panel = vscode.window.createWebviewPanel('nattReportView', 'NATT Report', vscode.ViewColumn.Beside, {
            enableScripts: true,
            retainContextWhenHidden: true
        });
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
            }
            else {
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
    const completionProvider = vscode.languages.registerCompletionItemProvider({ scheme: 'file', pattern: '**/test-config*.yaml' }, {
        provideCompletionItems(document, position) {
            return snippets_1.default.map(snippet => {
                const item = new vscode.CompletionItem(snippet.caption, vscode.CompletionItemKind.Snippet);
                item.insertText = snippet.snippet;
                item.detail = snippet.meta;
                return item;
            });
        }
    });
    context.subscriptions.push(completionProvider);
}
function deactivate() { }
//# sourceMappingURL=extension.js.map