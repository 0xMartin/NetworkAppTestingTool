/**
 * Content in this file is same as in NATT configuration web editor
 */

var keywordDetails = {
    test_root: {
        description: 'Root test configuration keyword. Must be at the beginning of the test configuration.',
        parameters: [
            { name: 'max_points', type: 'float', description: 'Maximum points for the test.' },
            { name: 'initial_steps', type: 'list | not required', description: 'Initial steps for all test suites. These steps are performed only once at the beginning and then the test suites are executed.' },
            { name: 'test_suites', type: 'list', description: 'List of test suites.' }
        ]
    },
    test_suite: {
        description: 'Keyword for test suite configuration.',
        parameters: [
            { name: 'name', type: 'string', description: 'Name of the test suite.' },
            { name: 'delay', type: 'long | not required', description: 'Delay between executed test cases. After finishing of the test case, it will wait for the specified time. (default: 500 ms)' },
            { name: 'initial_steps', type: 'list | not required', description: 'Initial steps for the test suite. These steps are performed only once at the beginning' },
            { name: 'test_cases', type: 'list', description: 'List of test cases.' }
        ]
    },
    test_case: {
        description: 'This keyword defines a single test case.',
        parameters: [
            { name: 'name', type: 'string', description: 'Name of the test case.' },
            { name: 'description', type: 'string', description: 'Description of the test case.' },
            { name: 'steps', type: 'list', description: 'Steps to execute in the test case.' }
        ]
    },
    run_app: {
        description: 'Run external application for testing. In one moment, only one external application can run.',
        parameters: [
            { name: '', type: 'string', description: 'Command to run the application. Can run only one application at a time.' }
        ]
    },
    run_app_later: {
        description: 'Run external application for testing with delay. This operation is async. In one moment, only one external application can run.',
        parameters: [
            { name: 'command', type: 'string', description: 'Command to run the application. Can run only one application at a time.' },
            { name: 'delay', type: 'long', description: 'Time after which the application starts' },
        ]
    },
    reload_app: {
        description: 'Stop the currently running external application and start a new one. ',
        parameters: [
            { name: '', type: 'string', description: 'Command to reload the application.' }
        ]
    },
    standard_stream_send: {
        description: 'Sends a message to the external application through the standard stream.',
        parameters: [
            { name: '', type: 'string', description: 'Message to send through the standard stream.' }
        ]
    },
    wait: {
        description: 'Pauses the test execution for a specified amount of time.',
        parameters: [
            { name: '', type: 'integer', description: 'Time to wait in milliseconds.' }
        ]
    },
    wait_until: {
        description: 'Waiting for action. The action is triggered by the receipt of a message by a specific communication module. The message that triggered the end of the waiting will be stored in a variable with the name \'<module-name>-action-msg\'',
        parameters: [
            { name: 'module_name', type: 'string', description: 'The module that should trigger the action. You can specify multiple modules this way: module-1 & module-2' },
            { name: 'time_out', type: 'integer | not required', description: 'Maximum waiting time in milliseconds. (default: 10 000 ms)' }
        ]
    },
    store_to_var: {
        description: 'Find and store a specific message from the buffer into a variable. If there is more than one message in the message buffer, it stores the first one.',
        parameters: [
            { name: 'var_name', type: 'string', description: 'Name of the variable to store the value of message.' },
            { name: 'module_name', type: 'string', description: 'The module from which the message was received.' },
            { name: 'text', type: 'string | not required', description: 'Text that must be included in the message. (default: "")' },
            { name: 'tag', type: 'string | not required', description: 'Required tag of message. (default: "")' },
            { name: 'mode', type: 'string | not required', description: 'Finding mode. There are this modes: "equals", "contains", "startswith", "endswith". (default: equals)' },
            { name: 'case_sensitive | not required', type: 'boolean', description: 'Whether the filter should be case sensitive.' }
        ]
    },
    count_and_store: {
        description: 'Counts the number of messages received during the one test case and stores this number in a variable.',
        parameters: [
            { name: 'var_name', type: 'string', description: 'Variable to store the count.' },
            { name: 'module_name', type: 'string', description: 'module whose received messages will be counted.' }
        ]
    },
    read_file: {
        description: 'Reads content from a specified file and stores it in a variable.',
        parameters: [
            { name: 'var_name', type: 'string', description: 'Variable to store the file content.' },
            { name: 'file_path', type: 'string', description: 'Path to the file to be read.' }
        ]
    },
    set_var: {
        description: 'Stores value in to the variable.',
        parameters: [
            { name: 'var_name', type: 'string', description: 'Variable to store the value.' },
            { name: 'value', type: 'string', description: 'The value that will be stored in the variable.' }
        ]
    },
    replace: {
        description: "Stores modified value into the variable.",
        parameters: [
            { name: "to_var", type: "string", description: "Variable to store the modified text." },
            { name: "from_var", type: "string", description: "Variable containing the original text." },
            { name: "str_from", type: "list", description: "String to be replaced in the original text." },
            { name: "str_to", type: "list", description: "String to replace occurrences of str_from in the original text." }
        ]
    },
    write_file: {
        description: 'Writes content to a specified file.',
        parameters: [
            { name: 'file_path', type: 'string', description: 'The path to the file where the content will be written.' },
            { name: 'content', type: 'string', description: 'The content to be written to the file.' }
        ]
    },
    clear_buffer: {
        description: 'Clears the message buffer of a module or all modules. The buffer is always automatically cleared at the end of each test case.',
        parameters: [
            { name: '', type: 'string', description: 'Specific module name, or symbol "*" for all modules.' }
        ]
    },
    json_get: {
        description: 'Extracts a value from a JSON structure and stores it into a variable.',
        parameters: [
            { name: 'to_var', type: 'string', description: 'Variable to store the extracted value.' },
            { name: 'from_var', type: 'string', description: 'Variable containing the JSON structure.' },
            { name: 'param_name', type: 'string', description: 'Name of the parameter to extract from the JSON. If the structure is the list, specify index to get one element. It is also possible to approach a certain parameter hierarchically in depth, when the parameter names are separated by ":", for example: parent_param:child_param.' }
        ]
    },
    buffer_get: {
        description: 'Stores the contents of a message from the message buffer at a specific index into a variable.',
        parameters: [
            { name: 'var_name', type: 'string', description: 'Variable to store the extracted message value.' },
            { name: 'module_name', type: 'string', description: 'Module name, its message buffer will be accessed.' },
            { name: 'index', type: 'long', description: 'Index of the message whose value will be extracted from the buffer into a variable. When index is negative, it indexes from the back of the buffer. -1 is last index of buffer. At last position is last received message.' }
        ]
    },
    create_telnet_client: {
        description: 'Create module that starts a new virtual Telnet client.',
        parameters: [
            { name: 'name', type: 'string', description: 'Unique name for the module.' },
            { name: 'host', type: 'string | not required', description: 'Hostname or IP address to connect to. (default: localhost)' },
            { name: 'port', type: 'integer | not required', description: 'Port number on the host to connect to. (default: 23)' }
        ]
    },
    create_telnet_server: {
        description: 'Create module that starts a virtual Telnet server.',
        parameters: [
            { name: 'name', type: 'string', description: 'Unique name for the module.' },
            { name: 'port', type: 'integer | not required', description: 'Port number to listen on. (default: 23)' }
        ]
    },
    create_web_crawler: {
        description: 'Create module that starts a web crawler with specified parameters.',
        parameters: [
            { name: 'name', type: 'string', description: 'Unique name for the module.' },
            { name: 'start_url', type: 'string', description: 'URL to start crawling from.' },
            { name: 'max_depth', type: 'integer', description: 'Maximum depth to crawl.' },
            { name: 'analyzer', type: 'string', description: 'Analyzer to use for parsing the web pages.' }
        ]
    },
    create_email_server: {
        description: 'Create module that starts a virtual email server.',
        parameters: [
            { name: 'name', type: 'string', description: 'Unique name for the module.' },
            { name: 'port', type: 'integer', description: 'Port number on which the email server will listen.' }
        ]
    },
    create_rest_tester: {
        description: 'Create module that starts a REST API tester with specified configurations.',
        parameters: [
            { name: 'name', type: 'string', description: 'Unique name for the module.' },
            { name: 'url', type: 'string', description: 'URL of the REST API to test.' },
            { name: 'request_type', type: 'string', description: 'Type of HTTP request to send (GET, POST, etc.).' },
            { name: 'content_type', type: 'string', description: 'Data type in request. (default: application/json).' }
        ]
    },
    create_soap_tester: {
        description: 'Creates a SOAP testing module for specified SOAP services.',
        parameters: [
            { name: 'name', type: 'string', description: 'Unique name for the module.' },
            { name: 'url', type: 'string', description: 'URL of the SOAP service to test.' }
        ]
    },
    create_mqtt_client: {
        description: 'Create module that starts a virtual MQTT client.',
        parameters: [
            { name: 'name', type: 'string', description: 'Unique name for the module.' },
            { name: 'topics', type: 'array | not required', description: 'List of topics to subscribe to. (default: empty)' },
            { name: 'broker_url', type: 'string | not required', description: 'URL of the MQTT broker to connect to. (default: tcp://localhost:1883)' }
        ]
    },
    create_mqtt_broker: {
        description: 'Create module that starts an MQTT broker.',
        parameters: [
            { name: 'name', type: 'string', description: 'Unique name for the module.' },
            { name: 'port', type: 'integer | not required', description: 'Port number for the MQTT broker. (default: 1883)' }
        ]
    },
    termite_module: {
        description: 'Terminates a running module that is no longer needed. Each module is automatically terminated when the context within which it was created ends.',
        parameters: [
            { name: '', type: 'string', description: 'Name of the module to terminate.' }
        ]
    },
    module_send: {
        description: 'Sends a message from a specified module.',
        parameters: [
            { name: 'name', type: 'string', description: 'Name of the sending module.' },
            { name: 'message', type: 'string', description: 'Message to send.' },
            { name: 'delay', type: 'long | not required', description: 'How long it will take to send a message after calling this key word.' }
        ]
    },
    create_filter_action: {
        description: 'Creates a filter for module message receive raction based on specified criteria.',
        parameters: [
            { name: 'name', type: 'string', description: 'Name of module.' },
            { name: 'text', type: 'string', description: 'Text to filter on.' },
            { name: 'tag', type: 'string | not required', description: 'Tag of message. (default: "")' },
            { name: 'mode', type: 'string | not required', description: 'Filter mode. There are this modes: "equals", "contains", "startswith", "endswith". (default: equals)' },
            { name: 'case_sensitive | not required', type: 'boolean', description: 'Whether the filter should be case sensitive. (default: true)' }
        ]
    },
    clear_filter_actions: {
        description: 'Clear list of filters for module actions.',
        parameters: [
            { name: 'name', type: 'string', description: 'Name of module.' }
        ]
    },
    assert_string: {
        description: 'Asserts that a variable contains a specific string.',
        parameters: [
            { name: 'var_name', type: 'string', description: 'Variable to perform the assertion on.' },
            { name: 'expected', type: 'string', description: 'The expected string.' },
            { name: 'mode', type: 'string | not required', description: 'Comparison mode. There are this modes: "equals", "contains", "startswith", "endswith". (default: equals)' },
            { name: 'case_sensitive', type: 'boolean | not required', description: 'Determines if the comparison should be case sensitive. (default: true)' },
            { name: 'result', type: 'boolean | not required', description: 'It determines the expected outcome of the assertion. (default: true)' }
        ]
    },
    assert_lower: {
        description: 'Asserts that a numeric variable is lower than a specified value.',
        parameters: [
            { name: 'var_name', type: 'string', description: 'Variable to perform the assertion on.' },
            { name: 'value', type: 'float', description: 'The upper limit for the variable value.' },
            { name: 'result', type: 'boolean | not required', description: 'It determines the expected outcome of the assertion. (default: true)' }
        ]
    },
    assert_larger: {
        description: 'Asserts that a numeric variable is larger than a specified value.',
        parameters: [
            { name: 'var_name', type: 'string', description: 'Variable to perform the assertion on.' },
            { name: 'value', type: 'float', description: 'The lower limit for the variable value.' },
            { name: 'result', type: 'boolean | not required', description: 'It determines the expected outcome of the assertion. (default: true)' }
        ]
    },
    assert_equals: {
        description: 'Asserts that a variable is equal to a specified number within a tolerance range.',
        parameters: [
            { name: 'var_name', type: 'string', description: 'Variable to perform the assertion on.' },
            { name: 'value', type: 'float', description: 'Expected value of the variable.' },
            { name: 'tolerance', type: 'float', description: 'Permissible deviation from the expected value.' },
            { name: 'result', type: 'boolean | not required', description: 'It determines the expected outcome of the assertion. (default: true)' }
        ]
    },
    assert_range: {
        description: 'Asserts that the sequences of received messages from two modules are the same in the defined segment.',
        parameters: [
            { name: 'module1_name', type: 'string', description: 'First module involved in the assertion.' },
            { name: 'module2_name', type: 'string', description: 'Second module involved in the assertion.' },
            { name: 'start', type: 'integer', description: 'Start index for the range check.' },
            { name: 'count', type: 'integer', description: 'Number of values to check within the range.' },
            { name: 'rule', type: 'string', description: 'Message content comparison rule. Format is: <message separator>|<X>;<X>;... Where is <X> is # (equals), ? (arbitrary), number 0-100 diff tolerance.' },
            { name: 'result', type: 'boolean | not required', description: 'It determines the expected outcome of the assertion. (default: true)' }
        ]
    },
    assert_app_is_running: {
        description: 'Asserts that external tested application is running now.',
        parameters: [
            { name: '', type: 'boolean | not required', description: 'It determines the expected outcome of the assertion. (default: true)' }
        ]
    },
    assert_module_is_running: {
        description: 'Asserts that specific module is running now.',
        parameters: [
            { name: 'module_name', type: 'string', description: 'Name of module.' },
            { name: 'result', type: 'boolean | not required', description: 'It determines the expected outcome of the assertion. (default: true)' }
        ]
    },
    assert_json: {
        description: 'Allows to assert that the JSON object in the variable is the same as the expected one.',
        parameters: [
            { name: 'var_name', type: 'string', description: 'The name of the variable containing the JSON object to be asserted.' },
            { name: 'expected', type: 'string', description: 'The expected JSON object in string format for comparison.' },
            { name: 'exact_mode', type: 'string', description: 'Specifies a comparison mode for JSON objects that requires strict equality between expected and actual JSON. If is false, it only compares parameters that are in expected JSON. (default: false)' },
            { name: 'result', type: 'boolean | not required', description: 'It determines the expected outcome of the assertion. (default: true)' }
        ]
    }
};

export default keywordDetails;