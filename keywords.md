# 🔑 NATT keywords

**[Go Back](./README.md)**

## Main Keywords

This set comprises all the keywords that directly define the testing structure.

1. **`test_root` – Marks the root element of the test configuration. It must be located at the beginning of the testing configuration. Tests start executing from this point.**
   - `max_points`: *Type*: float, *Description*: Maximum points for the test.
   - `initial_steps`: *Type*: list | not required, *Description*: Initial steps for all test suites. These steps are performed only once at the beginning and then the test suites are executed.
   - `test_suites`: *Type*: list, *Description*: List of test suites.
2. **`test_suite` – Used to define a testing suite.**
   - `name`: *Type*: string, *Description*: Name of the test suite.
   - `delay`: *Type*: long | not required, *Description*: Delay between executed test cases. After finishing of the test case, it will wait for the specified time. (default: 500 ms)
   - `initial_steps`: *Type*: list | not required, *Description*: Initial steps for the test suite. These steps are performed only once at the beginning.
   - `test_cases`: *Type*: list, *Description*: List of test cases.
3. **`test_case` – Allows the definition of individual test cases.**
   - `name`: *Type*: string, *Description*: Name of the test case.
   - `description`: *Type*: string, *Description*: Description of the test case.
   - `steps`: *Type*: list, *Description*: Steps to execute in the test case.

---

## Keywords for Controlling External Applications

This set includes keywords that enable working with an external application. It's primarily designed for launching and communicating with the tested application.

1. **`run_app` - Launches the application. At any given time, only one external application can run. It allows the definition of arguments to be passed to the application upon its launch.**
   - `default`: *Type*: string, *Description*: Command to run the application. Can run only one application at a time.
2. **`run_app_later` - Launches the application with a time delay. This operation is asynchronous. Again, only one external application can run at a time.**
   - `command`: *Type*: string, *Description*: Command to run the application. Can run only one application at a time.
   - `delay`: *Type*: long, *Description*: Time after which the application starts.
3. **`reload_app` - Stops the currently running application and restarts it.**
   - `default`: *Type*: string, *Description*: Command to reload the application.
4. **`standard_stream_send` - Sends a message to the running application via standard streaming.**
   - `name`: *Type*: string, *Description*: Message to send through the standard stream.

---

## General Keywords

This set encompasses keywords for working with variables, waiting, conditional waiting, and more.

1. **`wait` - Pauses the test execution for a defined duration.**
   - `default`: *Type*: integer, *Description*: Time to wait in milliseconds.
2. **`wait_until` - Waits until a specific action occurs, triggered by the reception of a message from a certain communication module. It can be extended with filtering conditions. The content of the message that triggered the action is automatically stored in a variable for potential testing.**
   - `module_name`: *Type*: string, *Description*: The module that should trigger the action. You can specify multiple modules this way: module-1 & module-2.
   - `time_out`: *Type*: integer | not required, *Description*: Maximum waiting time in milliseconds. (default: 10 000 ms)
3. **`store_to_var` - Retrieves and stores the content of a specific message from the message buffer into the chosen variable. If multiple messages match the specified search conditions, the variable stores the first one found, i.e., the one received first.**
   - `var_name`: *Type*: string, *Description*: Name of the variable to store the value of the message.
   - `module_name`: *Type*: string, *Description*: The module from which the message was received.
   - `text`: *Type*: string | not required, *Description*: Text that must be included in the message. (default: "")
   - `tag`: *Type*: string | not required, *Description*: Required tag of the message. (default: "")
   - `mode`: *Type*: string | not required, *Description*: Finding mode. There are these modes: "equals", "contains", "startswith", "endswith". (default: equals)
   - `case_sensitive`: *Type*: boolean, *Description*: Whether the filter should be case sensitive.
4. **`count_and_store` - Counts the number of received messages during a single test case and saves this count into a variable.**
   - `var_name`: *Type*: string, *Description*: Variable to store the count.
   - `module_name`: *Type*: string, *Description*: Module whose received messages will be counted.
5. **`read_file` - Reads the content from the specified file on the local device and stores its value into the defined variable.**
   - `var_name`: *Type*: string, *Description*: Variable to store the file content.
   - `file_path`: *Type*: string, *Description*: Path to the file to be read.
6. **`set_var` - Sets the specified variable to the defined content.**
   - `var_name`: *Type*: string, *Description*: Variable to store the value.
   - `value`: *Type*: string, *Description*: The value that will be stored in the variable.
7. **`replace` - Retrieves the content of a specific variable, replacing all desired words with their replacements. The result is stored in another variable.**
   - `to_var`: *Type*: string, *Description*: Variable to store the modified text.
   - `from_var`: *Type*: string, *Description*: Variable containing the original text.
   - `str_from`: *Type*: list, *Description*: String to be replaced in the original text.
   - `str_to`: *Type*: list, *Description*: String to replace occurrences of `str_from` in the original text.
8. **`write_file` - Writes the defined content into a file on the local device.**
   - `file_path`: *Type*: string, *Description*: The path to the file where the content will be written.
   - `content`: *Type*: string, *Description*: The content to be written to the file.
9.  **`clear_buffer` - Clears the content of the message buffer. It's possible to clear the buffer content for all modules or for a specific one.**
    - `default`: *Type*: string, *Description*: Specific module name, or symbol "*" for all modules.
10. **`json_get` - Extracts the value of a specified attribute from the content of a variable in JSON format. It's possible to access array indices or to traverse multiple levels in one step.**
    - `to_var`: *Type*: string, *Description*: Variable to store the extracted value.
    - `from_var`: *Type*: string, *Description*: Variable containing the JSON structure.
    - `param_name`: *Type*: string, *Description*: Name of the parameter to extract from the JSON. If the structure is a list, specify an index to get one element. It is also possible to approach a certain parameter hierarchically in depth, when the parameter names are separated by ":". For example: parent_param:child_param.
11. **`buffer_get` - Retrieves the content of a single message from the message buffer of a specific module. The message is accessed using an index, and its content is stored in the defined variable.**
    - `var_name`: *Type*: string, *Description*: Variable to store the extracted message value.
    - `module_name`: *Type*: string, *Description*: Module name, its message buffer will be accessed.
    - `index`: *Type*: long, *Description*: Index of the message whose value will be extracted from the buffer into a variable. When the index is negative, it indexes from the back of the buffer. -1 is the last index of the buffer. At the last position is the last received message.

---

## Keywords for Assertion Definition

This set comprises keywords that allow the definition of assertions that must be met during testing.

1. **`assert_string` - Verifies if a variable contains the expected string.**
   - `var_name`: *Type*: string, *Description*: Variable to perform the assertion on.
   - `expected`: *Type*: string, *Description*: The expected string.
   - `mode`: *Type*: string | not required, *Description*: Comparison mode. There are these modes: "equals", "contains", "startswith", "endswith". (default: equals)
   - `case_sensitive`: *Type*: boolean | not required, *Description*: Determines if the comparison should be case sensitive. (default: true)
   - `result`: *Type*: boolean | not required, *Description*: It determines the expected outcome of the assertion. (default: true)
2. **`assert_lower` - Checks if a numeric variable is lower than the expected value.**
   - `var_name`: *Type*: string, *Description*: Variable to perform the assertion on.
   - `value`: *Type*: float, *Description*: The upper limit for the variable value.
   - `result`: *Type*: boolean | not required, *Description*: It determines the expected outcome of the assertion. (default: true)
3. **`assert_larger` - Checks if a numeric variable is larger than the expected value.**
   - `var_name`: *Type*: string, *Description*: Variable to perform the assertion on.
   - `value`: *Type*: float, *Description*: The lower limit for the variable value.
   - `result`: *Type*: boolean | not required, *Description*: It determines the expected outcome of the assertion. (default: true)
4. **`assert_equals` - Checks if a variable equals the expected number. It's possible to set a certain tolerance range.**
   - `var_name`: *Type*: string, *Description*: Variable to perform the assertion on.
   - `value`: *Type*: float, *Description*: Expected value of the variable.
   - `tolerance`: *Type*: float, *Description*: Permissible deviation from the expected value.
   - `result`: *Type*: boolean | not required, *Description*: It determines the expected outcome of the assertion. (default: true)
5. **`assert_range` - Verifies if the sequence of received messages from two modules falls within a specified segment. Simple comparison rules can also be defined for comparison.**
   - `module1_name`: *Type*: string, *Description*: First module involved in the assertion.
   - `module2_name`: *Type*: string, *Description*: Second module involved in the assertion.
   - `start`: *Type*: integer, *Description*: Start index for the range check.
   - `count`: *Type*: integer, *Description*: Number of values to check within the range.
   - `rule`: *Type*: string, *Description*: Message content comparison rule. Format is: *(message separator)*|*(X)*;*(X)*;... Where is <X> is # (equals), ? (arbitrary), number 0-100 diff tolerance. **Example: ",|#;?;15"**
   - `result`: *Type*: boolean | not required, *Description*: It determines the expected outcome of the assertion. (default: true)
6. **`assert_app_is_running` - Verifies if an external application is currently running.**
   - `default`: *Type*: boolean | not required, *Description*: It determines the expected outcome of the assertion. (default: true)
7. **`assert_module_is_running` - Verifies if a specific module is currently running.**
   - `module_name`: *Type*: string, *Description*: Name of module.
   - `result`: *Type*: boolean | not required, *Description*: It determines the expected outcome of the assertion. (default: true)
8. **`assert_json` - Allows verification if the JSON object in a variable is identical to the expected JSON object.**
   - `var_name`: *Type*: string, *Description*: The name of the variable containing the JSON object to be asserted.
   - `expected`: *Type*: string, *Description*: The expected JSON object in string format for comparison.
   - `exact_mode`: *Type*: string, *Description*: Specifies a comparison mode for JSON objects that requires strict equality between expected and actual JSON. If is false, it only compares parameters that are in expected JSON. (default: false)
   - `result`: *Type*: boolean | not required, *Description*: It determines the expected outcome of the assertion. (default: true)

---

## Keywords for Working with Modules

This set includes keywords that enable working with communication modules.

1. **`create_telnet_client` - Creates a module that launches a new virtual Telnet client.**
   - `name`: *Type*: string, *Description*: Unique name for the module.
   - `host`: *Type*: string | not required, *Description*: Hostname or IP address to connect to. (default: localhost)
   - `port`: *Type*: integer | not required, *Description*: Port number on the host to connect to. (default: 23)
2. **`create_telnet_server` - Creates a module that launches a virtual Telnet server.**
   - `name`: *Type*: string, *Description*: Unique name for the module.
   - `port`: *Type*: integer | not required, *Description*: Port number to listen on. (default: 23)
3. **`create_web_crawler` - Creates a module that launches a web crawler.**
   - `name`: *Type*: string, *Description*: Unique name for the module.
   - `start_url`: *Type*: string, *Description*: URL to start crawling from.
   - `max_depth`: *Type*: integer, *Description*: Maximum depth to crawl.
   - `analyzer`: *Type*: string, *Description*: Analyzer to use for parsing the web pages.
4. **`create_email_server` - Creates a module that launches a virtual email server.**
   - `name`: *Type*: string, *Description*: Unique name for the module.
   - `port`: *Type*: integer, *Description*: Port number on which the email server will listen.
5. **`create_rest_tester` - Creates a module that launches an HTTP client for testing REST APIs.**
   - `name`: *Type*: string, *Description*: Unique name for the module.
   - `url`: *Type*: string, *Description*: URL of the REST API to test.
   - `request_type`: *Type*: string, *Description*: Type of HTTP request to send (GET, POST, etc.).
   - `content_type`: *Type*: string, *Description*: Data type in request. (default: application/json).
6. **`create_soap_tester` - Creates a module for testing SOAP services.**
   - `name`: *Type*: string, *Description*: Unique name for the module.
   - `url`: *Type*: string, *Description*: URL of the SOAP service to test.
7. **`create_mqtt_client` - Creates a module that launches a virtual MQTT client.**
   - `name`: *Type*: string, *Description*: Unique name for the module.
   - `topics`: *Type*: array | not required, *Description*: List of topics to subscribe to. (default: empty)
   - `broker_url`: *Type*: string | not required, *Description*: URL of the MQTT broker to connect to. (default: tcp://localhost:1883)
8. **`create_mqtt_broker` - Creates a module that launches an MQTT broker.**
   - `name`: *Type*: string, *Description*: Unique name for the module.
   - `port`: *Type*: integer | not required, *Description*: Port number for the MQTT broker. (default: 1883)
9.  **`termite_module` - Terminates a running module that is no longer needed.**
   - `default`: *Type*: string, *Description*: Name of the module to terminate.
10. **`module_send` - Sends a message using a specific module.**
    - `name`: *Type*: string, *Description*: Name of the sending module.
    - `message`: *Type*: string, *Description*: Message to send.
    - `delay`: *Type*: long | not required, *Description*: how long it will take to send a message after calling this key word.
11. **`create_filter_action` - Creates a filter for actions triggered upon message reception. Text content can be filtered.**
    - `name`: *Type*: string, *Description*: Name of module.
    - `text`: *Type*: string, *Description*: Text to filter on.
    - `tag`: *Type*: string | not required, *Description*: Tag of message. (default: "")
    - `mode`: *Type*: string | not required, *Description*: Filter mode. There are this modes: "equals", "contains", "startswith", "endswith". (default: equals)
    - `case_sensitive`: *Type*: boolean, *Description*: Whether the filter should be case sensitive. (default: true)
12. **`clear_filter_actions` - Removes all action filters for a specific module.**
    - `default`: *Type*: string, *Description*: Name of module.
