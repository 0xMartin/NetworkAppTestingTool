var editor = ace.edit("editor");
editor.setTheme("ace/theme/monokai");
editor.session.setMode("ace/mode/yaml");
editor.setFontSize(16);
editor.setOptions({
    wrap: true,
    wrapBehavioursEnabled: true,
    wrapBehaviours: "end",
    enableBasicAutocompletion: true,
    enableLiveAutocompletion: true
});

var keywordSnippets = [
    {
        caption: 'app-std-out',
        snippet: 'app-std-out',
        meta: 'Values'
    },
    {
        caption: 'ignore',
        snippet: 'ignore: true',
        meta: 'Values'
    },
    {
        caption: 'equals',
        snippet: 'equals',
        meta: 'Values'
    },
    {
        caption: 'contains',
        snippet: 'contains',
        meta: 'Values'
    },
    {
        caption: 'startswith',
        snippet: 'startswith',
        meta: 'Values'
    },
    {
        caption: 'endswith',
        snippet: 'endswith',
        meta: 'Values'
    },
    {
        caption: '$var',
        snippet: '$var',
        meta: 'Values'  
    },
    {
        caption: 'last-msg',
        snippet: 'last-msg',
        meta: 'Values'  
    },
    {
        caption: 'action-msg',
        snippet: 'action-msg',
        meta: 'Values'  
    },
    {
        caption: 'GET',
        snippet: 'GET',
        meta: 'Values'
    },
    {
        caption: 'POST',
        snippet: 'POST',
        meta: 'Values'
    },
    {
        caption: 'PUT',
        snippet: 'PUT',
        meta: 'Values'
    },
    {
        caption: 'DELETE',
        snippet: 'DELETE',
        meta: 'Values'
    },

    {
        caption: 'test_root',
        snippet: `test_root:
    max_points: 10.0
    initial_steps: []
    test_suites: []`,
        meta: "Main"
    },
    {
        caption: 'test_suite',
        snippet: `test_suite:
    name: "suite_name"
    delay: 500
    initial_steps: []
    test_cases: []`,
        meta: "Main"
    },
    {
        caption: 'test_case',
        snippet: `test_case:
    name: "Case name"
    description: "Description here"
    steps: []`,
        meta: "Main"
    },
    {
        caption: 'run_app',
        snippet: `run_app: 
    command: "java -jar app.jar"
    name: "app-1"`,
        meta: "App Controll"
    },
    {
        caption: 'run_app_later',
        snippet: `run_app_later: 
    command: "java -jar app.jar"
    delay: 500
    name: "app-1"`,
        meta: "App Controll"
    },
    {
        caption: 'reload_app',
        snippet: `reload_app: 
    command: "java -jar app.jar"
    name: "app-1"`,
        meta: "App Controll"
    },
    {
        caption: 'standard_stream_send',
        snippet: `standard_stream_send: 
    message: "Message to send"
    name: "app-1"`,
        meta: "App Controll"
    },
    {
        caption: 'wait',
        snippet: `wait: 1000`,
        meta: "General"
    },
    {
        caption: 'wait_until',
        snippet: `wait_until:
    module_name: "module-1 & module-2"
    time_out: 30000`,
        meta: "General"
    },
    {
        caption: 'store_to_var',
        snippet: `store_to_var:
    var_name: "var-1"
    module_name: "module-1"
    text: "Search text"
    tag: "message-tag"
    mode: "equals"
    case_sensitive: true`,
        meta: "General"
    },
    {
        caption: 'count_and_store',
        snippet: `count_and_store:
    var_name: "var-1"
    module_name: "module-1"`,
        meta: "General"
    },
    {
        caption: 'buffer_get',
        snippet: `buffer_get:
    var_name: "var-1"
    module_name: "module-1"
    index: 0`,
        meta: "General"
    },
    {
        caption: 'read_file',
        snippet: `read_file:
    var_name: "var-1"
    file_path: "path/to/file.txt"`,
        meta: "General"
    },
    {
        caption: 'read_net_file',
        snippet: `read_net_file:
    var_name: "var-1"
    file_url: "https://web.com/file.txt"`,
        meta: "General"
    },
    {
        caption: 'set_var',
        snippet: `set_var:
    var_name: "var-1"
    value: "New value"`,
        meta: "General"
    },
    {
        caption: 'replace',
        snippet: `replace:
    to_var: "var-1"
    from_var: "var-2"
    str_from: ["TMP_PARAM_1", "TMP_PARAM_2"]
    str_to: ["Value", "1234"]`,
        meta: "General"
    },
    {
        caption: 'write_file',
        snippet: `write_file:
    file_path: "path/to/file.txt"
    content: "this text will be saved in a file with the value of this variable \\$your-var"`,
        meta: "General"
    },
    {
        caption: 'write_net_file',
        snippet: `write_net_file:
    file_url: "https://web.com/file.txt"
    content: "this text will be saved in a file with the value of this variable \\$your-var"`,
        meta: "General"
    },
    {
        caption: 'clear_buffer',
        snippet: `clear_buffer: "module-1 or *"`,
        meta: "General"
    },
    {
        caption: 'json_get',
        snippet: `json_get:
    to_var: "var-1"
    from_var: "var-2"
    param_name: "parameter-name"`,
        meta: "General"
    },
    {
        caption: 'custom_keyword',
        snippet: `custom_keyword:
    name: "my_keyword"
    params: ["param_1", "param_2"]
    steps:
        - standard_stream_send: "This is message 1: \$param_1"
        - standard_stream_send: "This is message 2: \$param_2"`,
        meta: "General"
    },
    {
        caption: 'call_keyword',
        snippet: `call_keyword:
    name: "my_keyword"
    param_1: "Variable 1"
    param_2: "123456"`,
        meta: "General"
    },
    {
        caption: 'create_telnet_client',
        snippet: `create_telnet_client:
    name: "module-1"
    host: "localhost"
    port: 23`,
        meta: "Module"
    },
    {
        caption: 'create_telnet_server',
        snippet: `create_telnet_server:
    name: "module-1"
    port: 23`,
        meta: "Module"
    },
    {
        caption: 'create_web_crawler',
        snippet: `create_web_crawler:
    name: "module-1"
    start_url: "https://example.com"
    max_depth: 2
    analyzer: "word-freq:20"`,
        meta: "Module"
    },
    {
        caption: 'create_email_server',
        snippet: `create_email_server:
    name: "module-1"
    port: 25`,
        meta: "Module"
    },
    {
        caption: 'create_rest_tester',
        snippet: `create_rest_tester:
    name: "module-1"
    url: "http://api.example.com"
    request_type: "POST"
    content_type: "application/json"`,
        meta: "Module"
    },
    {
        caption: 'create_soap_tester',
        snippet: `create_soap_tester:
    name: "module-1"
    url: "http://soap.example.com/ws"`,
        meta: "Module"
    },
    {
        caption: 'create_mqtt_client',
        snippet: `create_mqtt_client:
    name: "module-1"
    topics: ["topic1", "topic2"]
    broker_url: "tcp://localhost:1883"`,
        meta: "Module"
    },
    {
        caption: 'create_mqtt_broker',
        snippet: `create_mqtt_broker:
    name: "module-1"
    port: 1883`,
        meta: "Module"
    },
    {
        caption: 'termite_module',
        snippet: `termite_module:
    name: "module-1"`,
        meta: "Module"
    },
    {
        caption: 'module_send',
        snippet: `module_send:
    name: "module-1"
    message: "Message \\$your-var"
    delay: 0`,
        meta: "Module"
    },
    {
        caption: 'create_filter_action',
        snippet: `create_filter_action:
    name: "module-1"
    text: "Text"
    tag: "Message-tag"
    mode: "equals"
    case_sensitive: true`,
        meta: "Module"
    },
    {
        caption: 'clear_filter_actions',
        snippet: `clear_filter_actions:
    name: "module-1"`,
        meta: "Module"
    },
    {
        caption: 'assert_string',
        snippet: `assert_string:
    var_name: "var-1"
    expected: "Expected value"
    mode: "equals"
    case_sensitive: true
    result: true`,
        meta: "Assert"
    },
    {
        caption: 'assert_lower',
        snippet: `assert_lower:
    var_name: "var-1"
    value: 100.0
    result: true`,
        meta: "Assert"
    },
    {
        caption: 'assert_larger',
        snippet: `assert_larger:
    var_name: "var-1"
    value: 100.0
    result: true`,
        meta: "Assert"
    },
    {
        caption: 'assert_equals',
        snippet: `assert_equals:
    var_name: "var-1"
    value: 100.0
    tolerance: 5
    result: true`,
        meta: "Assert"
    },
    {
        caption: 'assert_range',
        snippet: `assert_range:
    module1_name: "module-1"
    module2_name: "module-2"
    start: 0
    count: 10
    rule: ";|#;10"
    result: true`,
        meta: "Assert"
    },
    {
        caption: 'assert_app_is_running',
        snippet: `assert_app_is_running: 
    result: true
    name: "app-1"`,
        meta: "Assert"
    },
    {
        caption: 'assert_module_is_running',
        snippet: `assert_module_is_running: 
    module_name: "module-1"
    result: true`,
        meta: "Assert"
    },
    {
        caption: 'assert_json',
        snippet: `assert_json: 
    var_name: "module-1"
    expected: '{"name": "value", "atr1": 0}'
    exact_mode: false
    result: true`,
        meta: "Assert"
    }
];

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

function validateConfig() {
    const text = editor.getValue();
    fetch('/validate', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ text: text })
    }).then(response => response.json())
        .then(data => {
            updateErrorPanel(data.errors);
        });
}

function updateErrorPanel(errors) {
    const errorPanel = document.getElementById('errorPanel');
    if (errors.length === 0) {
        errorPanel.innerHTML = 'No errors found ✔️';
    } else {
        errorPanel.innerHTML = errors.join('<br>');
    }
}

function undoEdit() {
    editor.undo();
}

function redoEdit() {
    editor.redo();
}

function changeFontSize(size) {
    editor.setFontSize(parseInt(size));
}

editor.commands.addCommand({
    name: "Endo",
    bindKey: { win: "Ctrl-Z", mac: "Command-Z" },
    exec: undoEdit
});

editor.commands.addCommand({
    name: "Redo",
    bindKey: { win: "Ctrl-Y", mac: "Command-Y" },
    exec: redoEdit
});

editor.getSession().on("change", function() {
    var content = editor.getValue(); 
    localStorage.setItem("editorContent", content);
});