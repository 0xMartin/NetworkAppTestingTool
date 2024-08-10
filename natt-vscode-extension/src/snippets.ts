/**
 * Content in this file is same as in NATT configuration web editor
 */

var keywordSnippets = [
    {
        caption: 'app-std-out',
        snippet: 'app-std-out',
        meta: 'NATT Values'
    },
    {
        caption: 'ignore',
        snippet: 'ignore: true',
        meta: 'NATT Values'
    },
    {
        caption: 'equals',
        snippet: 'equals',
        meta: 'NATT Values'
    },
    {
        caption: 'contains',
        snippet: 'contains',
        meta: 'NATT Values'
    },
    {
        caption: 'startswith',
        snippet: 'startswith',
        meta: 'NATT Values'
    },
    {
        caption: 'endswith',
        snippet: 'endswith',
        meta: 'NATT Values'
    },
    {
        caption: '$var',
        snippet: '$var',
        meta: 'NATT Values'
    },
    {
        caption: 'last-msg',
        snippet: 'last-msg',
        meta: 'NATT Values'
    },
    {
        caption: 'action-msg',
        snippet: 'action-msg',
        meta: 'NATT Values'
    },
    {
        caption: 'GET',
        snippet: 'GET',
        meta: 'NATT Values'
    },
    {
        caption: 'POST',
        snippet: 'POST',
        meta: 'NATT Values'
    },
    {
        caption: 'PUT',
        snippet: 'PUT',
        meta: 'NATT Values'
    },
    {
        caption: 'DELETE',
        snippet: 'DELETE',
        meta: 'NATT Values'
    },

    {
        caption: 'test_root',
        snippet: `test_root:
    max_points: 10.0
    initial_steps: []
    test_suites: []`,
        meta: "NATT Main"
    },
    {
        caption: 'test_suite',
        snippet: `test_suite:
    name: "suite_name"
    delay: 500
    initial_steps: []
    test_cases: []`,
        meta: "NATT Main"
    },
    {
        caption: 'test_case',
        snippet: `test_case:
    name: "Case name"
    description: "Description here"
    steps: []`,
        meta: "NATT Main"
    },
    {
        caption: 'run_app',
        snippet: `run_app: 
    command: "java -jar app.jar"
    name: "app-1"`,
        meta: "NATT App Controll"
    },
    {
        caption: 'run_app_later',
        snippet: `run_app_later: 
    command: "java -jar app.jar"
    delay: 500
    name: "app-1"`,
        meta: "NATT App Controll"
    },
    {
        caption: 'reload_app',
        snippet: `reload_app: 
    command: "java -jar app.jar"
    name: "app-1"`,
        meta: "NATT App Controll"
    },
    {
        caption: 'standard_stream_send',
        snippet: `standard_stream_send: 
    message: "Message to send"
    name: "app-1"`,
        meta: "NATT App Controll"
    },
    {
        caption: 'wait',
        snippet: `wait: 1000`,
        meta: "NATT General"
    },
    {
        caption: 'wait_until',
        snippet: `wait_until:
    module_name: "module-1 & module-2"
    time_out: 30000`,
        meta: "NATT General"
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
        meta: "NATT General"
    },
    {
        caption: 'count_and_store',
        snippet: `count_and_store:
    var_name: "var-1"
    module_name: "module-1"`,
        meta: "NATT General"
    },
    {
        caption: 'buffer_get',
        snippet: `buffer_get:
    var_name: "var-1"
    module_name: "module-1"
    index: 0`,
        meta: "NATT General"
    },
    {
        caption: 'read_file',
        snippet: `read_file:
    var_name: "var-1"
    file_path: "path/to/file.txt"`,
        meta: "NATT General"
    },
    {
        caption: 'read_net_file',
        snippet: `read_net_file:
    var_name: "var-1"
    file_url: "https://web.com/file.txt"`,
        meta: "NATT General"
    },
    {
        caption: 'set_var',
        snippet: `set_var:
    var_name: "var-1"
    value: "New value"`,
        meta: "NATT General"
    },
    {
        caption: 'replace',
        snippet: `replace:
    to_var: "var-1"
    from_var: "var-2"
    str_from: ["TMP_PARAM_1", "TMP_PARAM_2"]
    str_to: ["Value", "1234"]`,
        meta: "NATT General"
    },
    {
        caption: 'write_file',
        snippet: `write_file:
    file_path: "path/to/file.txt"
    content: "this text will be saved in a file with the value of this variable \\$your-var"`,
        meta: "NATT General"
    },
    {
        caption: 'write_net_file',
        snippet: `write_net_file:
    file_url: "https://web.com/file.txt"
    content: "this text will be saved in a file with the value of this variable \\$your-var"`,
        meta: "NATT General"
    },
    {
        caption: 'clear_buffer',
        snippet: `clear_buffer: "module-1 or *"`,
        meta: "NATT General"
    },
    {
        caption: 'json_get',
        snippet: `json_get:
    to_var: "var-1"
    from_var: "var-2"
    param_name: "parameter-name"`,
        meta: "NATT General"
    },
    {
        caption: 'create_telnet_client',
        snippet: `create_telnet_client:
    name: "module-1"
    host: "localhost"
    port: 23`,
        meta: "NATT Module"
    },
    {
        caption: 'create_telnet_server',
        snippet: `create_telnet_server:
    name: "module-1"
    port: 23`,
        meta: "NATT Module"
    },
    {
        caption: 'create_web_crawler',
        snippet: `create_web_crawler:
    name: "module-1"
    start_url: "https://example.com"
    max_depth: 2
    analyzer: "word-freq:20"`,
        meta: "NATT Module"
    },
    {
        caption: 'create_email_server',
        snippet: `create_email_server:
    name: "module-1"
    port: 25`,
        meta: "NATT Module"
    },
    {
        caption: 'create_rest_tester',
        snippet: `create_rest_tester:
    name: "module-1"
    url: "http://api.example.com"
    request_type: "POST"
    content_type: "application/json"`,
        meta: "NATT Module"
    },
    {
        caption: 'create_soap_tester',
        snippet: `create_soap_tester:
    name: "module-1"
    url: "http://soap.example.com/ws"`,
        meta: "NATT Module"
    },
    {
        caption: 'create_mqtt_client',
        snippet: `create_mqtt_client:
    name: "module-1"
    topics: ["topic1", "topic2"]
    broker_url: "tcp://localhost:1883"`,
        meta: "NATT Module"
    },
    {
        caption: 'create_mqtt_broker',
        snippet: `create_mqtt_broker:
    name: "module-1"
    port: 1883`,
        meta: "NATT Module"
    },
    {
        caption: 'termite_module',
        snippet: `termite_module:
    name: "module-1"`,
        meta: "NATT Module"
    },
    {
        caption: 'module_send',
        snippet: `module_send:
    name: "module-1"
    message: "Message \\$your-var"
    delay: 0`,
        meta: "NATT Module"
    },
    {
        caption: 'create_filter_action',
        snippet: `create_filter_action:
    name: "module-1"
    text: "Text"
    tag: "Message-tag"
    mode: "equals"
    case_sensitive: true`,
        meta: "NATT Module"
    },
    {
        caption: 'clear_filter_actions',
        snippet: `clear_filter_actions:
    name: "module-1"`,
        meta: "NATT Module"
    },
    {
        caption: 'assert_string',
        snippet: `assert_string:
    var_name: "var-1"
    expected: "Expected value"
    mode: "equals"
    case_sensitive: true
    result: true`,
        meta: "NATT Assert"
    },
    {
        caption: 'assert_lower',
        snippet: `assert_lower:
    var_name: "var-1"
    value: 100.0
    result: true`,
        meta: "NATT Assert"
    },
    {
        caption: 'assert_larger',
        snippet: `assert_larger:
    var_name: "var-1"
    value: 100.0
    result: true`,
        meta: "NATT Assert"
    },
    {
        caption: 'assert_equals',
        snippet: `assert_equals:
    var_name: "var-1"
    value: 100.0
    tolerance: 5
    result: true`,
        meta: "NATT Assert"
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
        meta: "NATT Assert"
    },
    {
        caption: 'assert_app_is_running',
        snippet: `assert_app_is_running: true`,
        meta: "NATT Assert"
    },
    {
        caption: 'assert_module_is_running',
        snippet: `assert_module_is_running: 
    module_name: "module-1"
    result: true`,
        meta: "NATT Assert"
    },
    {
        caption: 'assert_json',
        snippet: `assert_json: 
    var_name: "module-1"
    expected: '{"name": "value", "atr1": 0}'
    exact_mode: false
    result: true`,
        meta: "NATT Assert"
    }
];

export default keywordSnippets;