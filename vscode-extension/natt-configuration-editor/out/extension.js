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
const nattviewprovider_1 = __importDefault(require("./nattviewprovider"));
const reportwebviewprovider_1 = __importDefault(require("./reportwebviewprovider"));
const snippets_1 = __importDefault(require("./snippets"));
let testTerminal;
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
            }
            else {
                vscode.window.showInformationMessage('test-config.yaml already exists, skipping copy.');
            }
            // Copy the JAR file regardless of its existence
            fs.copyFileSync(sourceJarPath, destJarPath);
            vscode.window.showInformationMessage('NATT structure initialized successfully!');
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