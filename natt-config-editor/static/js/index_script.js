/*EDITOR********************************************************************************* */
var editor = ace.edit("editor");
editor.setTheme("ace/theme/monokai");
editor.session.setMode("ace/mode/yaml");

// nastavi spravnou velikost editoru
function setEditorHeight() {
    var windowHeight = window.innerHeight;
    var errorPanelHeight = document.getElementById('errorPanel').offsetHeight;
    var menuBarHeight = document.getElementById('menuBar').offsetHeight;
    var editorHeight = windowHeight - errorPanelHeight - menuBarHeight;
    document.getElementById('editor').style.height = editorHeight + 'px';
    editor.resize();
}

// ulozi yaml konfiguracu
function saveFile() {
    var content = editor.getValue();
    var blob = new Blob([content], { type: "text/plain;charset=utf-8" });
    saveAs(blob, "test-config.yaml");
}

// otevre yaml konfiguraci
function openFile() {
    var input = document.createElement('input');
    input.type = 'file';
    input.accept = '.yaml';

    input.onchange = function () {
        var file = input.files[0];
        var reader = new FileReader();

        reader.onload = function () {
            var text = reader.result;
            editor.setValue(text);
        };

        reader.readAsText(file);
    };

    input.click();
}

// vlozi inicializacni strukturu configurace
function insertInitialContent() {
    var savedContent = localStorage.getItem("editorContent");
    if (savedContent) {
        editor.setValue(savedContent);
    } else {
        newFile(false);
    }
}

function newFile(showDialog) {
    if (showDialog) {
        var result = window.confirm("Do you want to create a new file? Current content will be deleted!");
        if (!result) {
            return;
        }
    }

    var initialContent = `test_root:
    max_points: 10.0
    initial_steps: 
        - run_app: "java -jar app.jar"
    test_suites: 
        - test_suite:
            name: "Suite name 1"
            delay: 500
            initial_steps: []
            test_cases: 
                - test_case:
                    name: "Case name 1"
                    description: "Description 1 here"
                    steps: 
                        - standard_stream_send: "Message to send"
                - test_case:
                    name: "Case name 2"
                    description: "Description 2 here"
                    steps:
                        - standard_stream_send: "Message to send"`;
    editor.setValue(initialContent);
}

/*RUN*ON*EXTERNAL*TOOL********************************************************************************* */
// s vyuzitim soketu
var socket = io();
socket.on('connect', function () {
    console.log('Connected to server');
});

socket.on('stopped', function (data) {
    var iframe = document.getElementById('report-iframe');
    // terminal
    var terminal = iframe.contentWindow.document.getElementById('terminal');
    terminal.innerHTML += '<span class="red-text">' + data.message + '</span>\n';
    // info text
    var in_progress = iframe.contentWindow.document.getElementById('in_progress');
    in_progress.style.display = 'none';
    var in_stopped = iframe.contentWindow.document.getElementById('in_stopped');
    in_stopped.style.display = 'block';
});

socket.on('output', function (data) {
    var txtData = data.data.replace(/</g, '&lt;').replace(/>/g, '&gt;');
    // pripise prichozi text z vystupu nastroje do terminalu
    var iframe = document.getElementById('report-iframe');
    var terminal = iframe.contentWindow.document.getElementById('terminal');
    if (data.data.includes("[WARNING]")) {
        terminal.innerHTML += '<span class="yellow-text">' + txtData + "</span>\n";
    } else {
        terminal.innerHTML += txtData + "\n";
    }
    terminal.scrollTop = terminal.scrollHeight;
    console.log(txtData);

    // ve vypisovanem textu hleda klicove slove "Report generating done."
    if (txtData.includes("Report generating done.")) {
        var btn = iframe.contentWindow.document.getElementById('show_report_btn');
        btn.style.display = 'block';
    }

});

socket.on('finished', function (data) {
    var iframe = document.getElementById('report-iframe');

    // info text
    var in_progress = iframe.contentWindow.document.getElementById('in_progress');
    in_progress.style.display = 'none';

    var terminal = iframe.contentWindow.document.getElementById('terminal');
    if (data.status === 'success') {
        // terminal msg
        terminal.innerHTML += '<span class="green-text">Process finished successfully</span>\n';

        // info text
        var in_success = iframe.contentWindow.document.getElementById('in_success');
        in_success.style.display = 'block';
    } else {
        // terminal msg
        terminal.innerHTML += '<span class="red-text">Error: ' + data.message + '</span>\n';

        // info text
        var in_failure = iframe.contentWindow.document.getElementById('in_failure');
        in_failure.style.display = 'block';
    }

    // event pro tlacitko nacteni reportu
    var buttons = iframe.contentWindow.document.querySelectorAll('#show_report_btn');
    buttons.forEach(function (btn) {
        btn.style.display = 'block';
        btn.addEventListener('click', function () {
            loadReport();
        });
    });
});

// odesle konfiguraci s editoru pro vykonani a spusti externi java nastroj pro black box testovani
function sendToServer() {
    // show progress page
    var iframe = document.getElementById('report-iframe');
    iframe.onload = function () {
        // skryje tlactiko pro otevreni reportu
        var btn = iframe.contentWindow.document.getElementById('show_report_btn');
        btn.style.display = 'none';
        // run external program
        var content = editor.getValue();
        socket.emit('run_java', { yaml_content: content });
    };
    iframe.src = 'progress.html';
}

// zastavi spusteny program
function stopProcess() {
    socket.emit('stop_java');
}

// nacte posledni vysledek reportu
function loadReport() {
    var iframe = document.getElementById('report-iframe');
    iframe.src = 'test_report.html';
}

// zobrazi pradny vystup reportu na prave strana
function showEmpty() {
    var iframe = document.getElementById('report-iframe');
    iframe.src = 'empty.html';
}

// validace konfigurace
function validateConfig() {
    document.getElementById('errorPanel').innerHTML = "";
    var content = editor.getValue();
    socket.emit('validate', { yaml_content: content });
}

// prijeti vystupu validace
socket.on('validate-response', function (data) {
    if (data.status === 'success') {
        document.getElementById('errorPanel').innerHTML = '<span class="green-text">' + data.message + '</span>';
    } else {
        document.getElementById('errorPanel').innerHTML = '<span class="red-text">' + data.message + '</span>';
    }
});

/*UI********************************************************************************* */

// UI pomer 7-5
function setPanelTo7AndIframeTo5() {
    document.getElementById('left-panel').className = 'col-md-7 p-0';
    document.getElementById('iframe-container').className = 'col-md-5 p-0';
}

// UI pomer 5-7
function setPanelTo5AndIframeTo7() {
    document.getElementById('left-panel').className = 'col-md-5 p-0';
    document.getElementById('iframe-container').className = 'col-md-7 p-0';
}

// register keyword snippets
function registerKeywordSnippets() {
    // Define the ACE editor completions setup
    var langTools = ace.require("ace/ext/language_tools");
    var customCompleter = {
        getCompletions: function (editor, session, pos, prefix, callback) {
            if (prefix.length === 0) {
                callback(null, []);
                return;
            }
            callback(null, keywordSnippets.filter(function (word) {
                return word.caption.startsWith(prefix);
            }).map(function (e) {
                return { caption: e.caption, snippet: e.snippet, meta: e.meta, type: "snippet" };
            }));
        }
    };

    langTools.setCompleters([customCompleter]);

    // Fetch data from the backend
    fetch('/get-snippets', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' }
    })
        .then(response => response.json())
        .then(data => {
            if (data.error) {
                console.error('Error:', data.error);
                return;
            }

            // Assign the fetched snippets to a global variable
            window.keywordSnippets = data.snippets;

            // Register snippets in ACE editor
            customCompleter.getCompletions = function (editor, session, pos, prefix, callback) {
                if (prefix.length === 0) {
                    callback(null, []);
                    return;
                }
                callback(null, keywordSnippets.filter(function (word) {
                    return word.caption.startsWith(prefix);
                }).map(function (e) {
                    return { caption: e.caption, snippet: e.snippet, meta: e.meta, type: "snippet" };
                }));
            };

            // Load keyword information into the list
            loadKeywordInfoList();
        })
        .catch(error => console.error('Error fetching snippets:', error));
}

function loadKeywordInfoList() {
    var keywordList = document.getElementById('keywordList');
    var keywordDescription = document.getElementById('keywordDescription');
    var lastActive = null;

    // Clear any existing list items
    keywordList.innerHTML = '';

    keywordSnippets.forEach(function (keyword) {
        var item = document.createElement('div');
        item.textContent = keyword.caption;
        item.className = 'keyword-item';
        item.onclick = function () {
            if (lastActive) {
                lastActive.classList.remove('active');
            }
            item.classList.add('active');
            lastActive = item;
        
            displayKeywordDetails(keyword, keywordDescription);
        };
        keywordList.appendChild(item);
    });
}

function displayKeywordDetails(keyword, keywordDescription) {
    // Clear the existing content in the keywordDescription element
    keywordDescription.innerHTML = '';

    // Create the main container for the keyword details
    const keywordItem = document.createElement('div');
    keywordItem.className = 'keyword-item';

    // Create and append the title element (keyword name)
    const keywordTitle = document.createElement('div');
    keywordTitle.className = 'keyword-title';
    keywordTitle.textContent = keyword.caption; 
    keywordItem.appendChild(keywordTitle);

    // Create and append the group element
    const keywordGroup = document.createElement('span');
    keywordGroup.className = 'keyword-group';
    keywordGroup.textContent = keyword.meta; 
    keywordTitle.appendChild(keywordGroup);

    // Create and append the description element
    const keywordDesc = document.createElement('div');
    keywordDesc.className = 'keyword-description';
    keywordDesc.textContent = keyword.description;
    keywordItem.appendChild(keywordDesc);

    // Create and append the parameters element
    const keywordParameters = document.createElement('div');
    keywordParameters.className = 'keyword-parameters';
    const parameters = keyword.params.split(","); 
    keywordParameters.innerHTML = parameters.map(param => {
        const paramParts = param.trim().split(':');
        return `<div class="parameter-pill">
                    <span class="parameter-pill-name">${paramParts[0].trim()}</span>
                    <span class="parameter-pill-type">${paramParts[1].trim()}</span>
                </div>`;
    }).join('<br>');
    keywordItem.appendChild(keywordParameters);

    // Append the constructed keyword item to the description panel
    keywordDescription.appendChild(keywordItem);
}

function gotoHelp() {
    window.location.href = "./help";
}

/*MAIN********************************************************************************* */
// on load akce
window.onload = function () {
    setEditorHeight();
    insertInitialContent();
    showEmpty();
    registerKeywordSnippets();
};
window.onresize = setEditorHeight;

// zprava pri ukoncovani aplikace
function beforeCloseEvent() {
    event.preventDefault();
    return "";
}

// zobrazi splash screen
function hideSplash() {
    setTimeout(function () {
        var splashScreen = document.getElementById("splash-screen");
        splashScreen.classList.add("hidden");
        setTimeout(function () {
            splashScreen.parentNode.removeChild(splashScreen);
        }, 500);
    }, 2000);
}

document.addEventListener('DOMContentLoaded', function () {
    var editorElement = document.getElementById('editor');
    editorElement.addEventListener('focus', function () {
        setPanelTo7AndIframeTo5();
    }, true);

    editorElement.addEventListener('blur', function () {
        setPanelTo5AndIframeTo7();
    }, true);

    hideSplash();
});
