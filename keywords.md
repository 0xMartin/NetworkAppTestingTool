# 🔑 NATT keywords

**[Go Back](./README.md)**

> **Note 1:** When keyword have only one parameter, it is not necessary to specify the parameter name. In documentation parameters without name are named as `default`.

> **Note 2:** Each keyword can be ignored by adding the parameter `ignore: true`

> **Note 3:** The variable `(module-name)-last-msg` holds the content of the message received last by a given module.

> **Note 4:** The variable `(module-name)-action-msg` contains the content of the message that triggered the termination of waiting (`wait_until` keyword).

## Main Keywords

This set contains all the keywords that directly define the testing structure.

### test_root

Marks the root element of the test configuration. It must be located at the beginning of the testing configuration. Tests start executing from this point.

| **Parameter**   | **Type**                | **Description**                                                                                                                |
| --------------- | ----------------------- | ------------------------------------------------------------------------------------------------------------------------------ |
| `max_points`    | float \| *not required* | Maximum points for the test. Used in the evaluation of graded assignments.                                                     |
| `initial_steps` | list \| *not required*  | Initial steps for all test suites. These steps are performed only once at the beginning and then the test suites are executed. |
| `test_suites`   | list                    | List of test all suites.                                                                                                       |


```yaml
test_root:
    max_points: 10.0
    initial_steps: []
    test_suites: []
```

### test_suite

Used to define a testing suite.

| **Parameter**   | **Type**               | **Description**                                                                                                             |
| --------------- | ---------------------- | --------------------------------------------------------------------------------------------------------------------------- |
| `name`          | string                 | Name of the test suite.                                                                                                     |
| `delay`         | long \| *not required* | Delay between executed test cases. After finishing of the test case, it will wait for the specified time. (default: 500 ms) |
| `initial_steps` | list \| *not required* | Initial steps for the test suite. These steps are performed only once at the beginning.                                     |
| `test_cases`    | list                   | List of test cases.                                                                                                         |

  
```yaml
test_suite:
    name: "suite_name"
    delay: 500
    initial_steps: []
    test_cases: []
```

### test_case

Allows the definition of individual test cases.

| **Parameter** | **Type** | **Description**                    |
| ------------- | -------- | ---------------------------------- |
| `name`        | string   | Name of the test case.             |
| `description` | string   | Description of the test case.      |
| `steps`       | list     | Steps to execute in the test case. |


```yaml
test_case:
    name: "Case name"
    description: "Description here"
    steps: []
```

---

## Keywords for Controlling External Applications

This set includes keywords that enable working with an external application. It's primarily designed for launching and communicating with the tested applications.

### run_app

Launches the application. At any given time, only one external application can run! It allows the definition of arguments to be passed to the application upon its launch.

| **Parameter**            | **Type** | **Description**                                                         |
| ------------------------ | -------- | ----------------------------------------------------------------------- |
| `command`                | string   | Command to run the application. Can run only one application at a time. |
| `name` \| *not required* | string   | Name of program runner. *(default: "default")*                          |
  
```yaml
run_app: "java -jar app.jar -arg1 111 -arg2 222"

run_app: 
    command: "java -jar app.jar -arg1 111 -arg2 222"
    name: "app-1"
```

### run_app_later

Launches the application with a time delay. This operation is asynchronous. Again, only one external application can run at a time.

| **Parameter**            | **Type** | **Description**                                                         |
| ------------------------ | -------- | ----------------------------------------------------------------------- |
| `command`                | string   | Command to run the application. Can run only one application at a time. |
| `delay`                  | long     | Time after which the application starts.                                |
| `name` \| *not required* | string   | Name of program runner. *(default: "default")*                          |


```yaml
run_app_later: 
    command: "java -jar app.jar -arg1 111 -arg2 222"
    delay: 500
    name: "app-1"
```

### reload_app

Stops the currently running application and launches the new application.
   
| **Parameter**            | **Type** | **Description**                                |
| ------------------------ | -------- | ---------------------------------------------- |
| `command`                | string   | Command to reload the application.             |
| `name` \| *not required* | string   | Name of program runner. *(default: "default")* |


```yaml
reload_app: "java -jar app.jar"

reload_app: 
    command: "java -jar app.jar"
    name: "app-1"
```

### standard_stream_send

Sends a message to the running application via standard streaming.

| **Parameter**            | **Type** | **Description**                                |
| ------------------------ | -------- | ---------------------------------------------- |
| `message`                | string   | Message to send through the standard stream.   |
| `name` \| *not required* | string   | Name of program runner. *(default: "default")* |


```yaml
standard_stream_send: "Message to send"

standard_stream_send: 
    message: "Message to send"
    name: "app-1"
```

---

## General Keywords

This set includes keywords for working with variables, waiting, conditional waiting, and more.

### wait

Pauses the test execution for a defined duration.

| **Parameter** | **Type** | **Description**               |
| ------------- | -------- | ----------------------------- |
| `default`     | integer  | Time to wait in milliseconds. |
  
```yaml
wait: 1000
```

### wait_until

It waits until a message is received from a certain communication module. Messages can be filtered using the keyword `create_filter_action`. The content of the message that triggered the action is automatically saved in the `(module-name)-action-msg` variable for possible testing.

| **Parameter** | **Type**                  | **Description**                                                                                            |
| ------------- | ------------------------- | ---------------------------------------------------------------------------------------------------------- |
| `module_name` | string                    | The module that should trigger the action. You can specify multiple modules this way: module-1 & module-2. |
| `time_out`    | integer \| *not required* | Maximum waiting time in milliseconds. *(default: 10 000 ms)*                                               |


```yaml
wait_until:
    module_name: "module-1 & module-2"
    time_out: 30000
```

### store_to_var

Retrieves and stores the content of a specific message from the message buffer into the chosen variable. If multiple messages match the specified search conditions, the variable stores the first one found, i.e., the one received first.

| **Parameter**    | **Type**                  | **Description**                                                                                                  |
| ---------------- | ------------------------- | ---------------------------------------------------------------------------------------------------------------- |
| `var_name`       | string                    | Name of the variable to store the value of the message.                                                          |
| `module_name`    | string                    | The module from which the message was received.                                                                  |
| `text`           | string \| *not required*  | Text that must be included in the message. *(default: "")*                                                       |
| `tag`            | string \| *not required*  | Required tag of the message. *(default: "")*                                                                     |
| `mode`           | string \| *not required*  | Finding mode. There are these modes: `"equals"`, `"contains"`, `"startswith"`, `"endswith"`. *(default: equals)* |
| `case_sensitive` | boolean \| *not required* | Whether the filter should be case sensitive.                                                                     |


```yaml
store_to_var:
    var_name: "var-1"
    module_name: "module-1"
    text: "Search text"
    tag: "message-tag"
    mode: "equals"
    case_sensitive: true
```

### count_and_store

Counts the number of received messages during a single test case and saves this count into a variable.

| **Parameter** | **Type** | **Description**                                 |
| ------------- | -------- | ----------------------------------------------- |
| `var_name`    | string   | Variable to store the count.                    |
| `module_name` | string   | Module whose received messages will be counted. |

```yaml
count_and_store:
    var_name: "var-1"
    module_name: "module-1
```

### read_file

Reads the content from the specified file on the local device and stores its value into the defined variable.

| **Parameter** | **Type** | **Description**                     |
| ------------- | -------- | ----------------------------------- |
| `var_name`    | string   | Variable to store the file content. |
| `file_path`   | string   | Path to the file to be read.        |

```yaml
read_file:
    var_name: "var-1"
    file_path: "path/to/file.txt"
```

### read_net_file

Reads the content from the specified file on the network device and stores its value into the defined variable.

| **Parameter** | **Type** | **Description**                     |
| ------------- | -------- | ----------------------------------- |
| `var_name`    | string   | Variable to store the file content. |
| `file_url`    | string   | URL of the file to be read.         |

```yaml
read_net_file:
    var_name: "var-1"
    file_url: "path/to/file.txt"
```

### set_var

Sets the specified variable to the defined content.

| **Parameter** | **Type** | **Description**                                |
| ------------- | -------- | ---------------------------------------------- |
| `var_name`    | string   | Variable to store the value.                   |
| `value`       | string   | The value that will be stored in the variable. |

```yaml
set_var:
    var_name: "var-1"
    value: "New value"
```

### replace

Retrieves the content of a specific variable, replacing all desired words with their replacements. The result is stored in another variable.

| **Parameter** | **Type** | **Description**                                                   |
| ------------- | -------- | ----------------------------------------------------------------- |
| `to_var`      | string   | Variable to store the modified text.                              |
| `from_var`    | string   | Variable containing the original text.                            |
| `str_from`    | list     | String to be replaced in the original text.                       |
| `str_to`      | list     | String to replace occurrences of `str_from` in the original text. |

```yaml
replace:
    to_var: "var-1"
    from_var: "var-2"
    str_from: ["TMP_PARAM_1", "TMP_PARAM_2"]
    str_to: ["Value", "1234"]
```

### write_file

Writes the defined content into a file on the local device.

| **Parameter** | **Type** | **Description**                                         |
| ------------- | -------- | ------------------------------------------------------- |
| `file_path`   | string   | The path to the file where the content will be written. |
| `content`     | string   | The content to be written to the file.                  |
 
```yaml
write_file:
    file_path: "path/to/file.txt"
    content: "this text will be saved in a file with the value of this variable $your-var"
```

### write_net_file

Writes the defined content into a file on the network device.

| **Parameter** | **Type** | **Description**                                    |
| ------------- | -------- | -------------------------------------------------- |
| `file_url`    | string   | URL of the file where the content will be written. |
| `content`     | string   | The content to be written to the file.             |
 
```yaml
write_net_file:
    file_url: "https://web.com/file.txt"
    content: "this text will be saved in a file with the value of this variable $your-var"
```

### clear_buffer

Clears the content of the message buffer. It's possible to clear the buffer content for all modules or for a specific one.

| **Parameter** | **Type** | **Description**                                      |
| ------------- | -------- | ---------------------------------------------------- |
| `default`     | string   | Specific module name, or symbol `*` for all modules. |

```yaml
clear_buffer: "module-1"
```

### json_get

Extracts the value of a specified attribute from the content of a variable in JSON format. It's possible to access array indices or to traverse multiple levels in one step.

| **Parameter** | **Type** | **Description**                                                                                                                                                                                                                                                                     |
| ------------- | -------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `to_var`      | string   | Variable to store the extracted value.                                                                                                                                                                                                                                              |
| `from_var`    | string   | Variable containing the JSON structure.                                                                                                                                                                                                                                             |
| `param_name`  | string   | Name of the parameter to extract from the JSON. If the structure is a list, specify an index to get one element. It is also possible to approach a certain parameter hierarchically in depth, when the parameter names are separated by ":". For example: parent_param:child_param. |

```yaml
json_get:
    to_var: "var-1"
    from_var: "var-2"
    param_name: "id"

json_get:
    to_var: "var-1"
    from_var: "var-2"
    param_name: "createAuthorResponse:author:id"
```

### buffer_get

Retrieves the content of a single message from the message buffer of a specific module. The message is accessed using an index, and its content is stored in the defined variable.

| **Parameter** | **Type** | **Description**                                                                                                                                                                                                                                  |
| ------------- | -------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| `var_name`    | string   | Variable to store the extracted message value.                                                                                                                                                                                                   |
| `module_name` | string   | Module name; its message buffer will be accessed.                                                                                                                                                                                                |
| `index`       | long     | Index of the message whose value will be extracted from the buffer into a variable. When the index is negative, it indexes from the back of the buffer. `-1` is the last index of the buffer. At the last position is the last received message. |

```yaml
buffer_get:
    var_name: "var-1"
    module_name: "module-1"
    index: 0
```

### custom_keyword

Allows for the definition of a custom keyword within the system. The custom keyword can include a series of steps and input parameters.

| **Parameter** | **Type**         | **Description**                                                                                                                                          |
| ------------- | ---------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `name`        | string           | The unique name assigned to the custom keyword. This name is required for invoking the keyword later.                                                    |
| `params`      | list \| optional | A list of parameters that the custom keyword expects to receive when invoked. These parameters can be used within the steps. Value of params must be string type. |
| `steps`       | list             | A list of keywords that will be executed as part of this custom keyword. Each step is executed in sequence when invoked.                                 |

```yaml
custom_keyword:
    name: "my_keyword"
    params: ["param_1", "param_2"]
    steps:
        - standard_stream_send: "This is message 1: $param_1"
        - standard_stream_send: "This is message 2: $param_2"
```

### call_keyword

This keyword is used to invoke a custom keyword that has been previously defined. You can also define input parameters for the custom keyword, which can be used within the keyword's steps.

| **Parameter** | **Type** | **Description**                               |
| ------------- | -------- | --------------------------------------------- |
| `name`        | string   | The name of the custom keyword to be invoked. |

```yaml
call_keyword:
    name: "my_keyword"
    param_1: "Variable 1"
    param_2: "123456"

call_keyword: "my_keyword"
```

---

## Keywords for Assertion Definition

This set comprises keywords that allow the definition of assertions that must be met during testing.

### assert_string

Verifies if a variable contains the expected string.

| **Parameter**    | **Type**                  | **Description**                                                                                                     |
| ---------------- | ------------------------- | ------------------------------------------------------------------------------------------------------------------- |
| `var_name`       | string                    | Variable to perform the assertion on.                                                                               |
| `expected`       | string                    | The expected string.                                                                                                |
| `mode`           | string \| *not required*  | Comparison mode. There are these modes: `"equals"`, `"contains"`, `"startswith"`, `"endswith"`. *(default: equals)* |
| `case_sensitive` | boolean \| *not required* | Determines if the comparison should be case sensitive. *(default: true)*                                            |
| `result`         | boolean \| *not required* | It determines the expected outcome of the assertion. *(default: true)*                                              |
  
```yaml
assert_string:
    var_name: "var-1"
    expected: "Expected value"
    mode: "equals"
    case_sensitive: true
    result: true
```

### assert_lower

Checks if a numeric variable is lower than the expected value.

| **Parameter** | **Type**                  | **Description**                                                        |
| ------------- | ------------------------- | ---------------------------------------------------------------------- |
| `var_name`    | string                    | Variable to perform the assertion on.                                  |
| `value`       | float                     | The upper limit for the variable value.                                |
| `result`      | boolean \| *not required* | It determines the expected outcome of the assertion. *(default: true)* |

```yaml
assert_lower:
    var_name: "var-1"
    value: 100.0
    result: true
```

### assert_larger

Checks if a numeric variable is larger than the expected value.

| **Parameter** | **Type**                  | **Description**                                                        |
| ------------- | ------------------------- | ---------------------------------------------------------------------- |
| `var_name`    | string                    | Variable to perform the assertion on.                                  |
| `value`       | float                     | The lower limit for the variable value.                                |
| `result`      | boolean \| *not required* | It determines the expected outcome of the assertion. *(default: true)* |

```yaml
assert_larger:
    var_name: "var-1"
    value: 100.0
    result: true
```

### assert_equals

Checks if a variable equals the expected number. It's possible to set a certain tolerance range.

| **Parameter** | **Type**                  | **Description**                                                        |
| ------------- | ------------------------- | ---------------------------------------------------------------------- |
| `var_name`    | string                    | Variable to perform the assertion on.                                  |
| `value`       | float                     | Expected value of the variable.                                        |
| `tolerance`   | float                     | Permissible deviation from the expected value.                         |
| `result`      | boolean \| *not required* | It determines the expected outcome of the assertion. *(default: true)* |

```yaml
assert_equals:
    var_name: "var-1"
    value: 100.0
    tolerance: 5
    result: true
```

### assert_range

Verifies if the sequence of received messages from two modules falls within a specified segment. Simple comparison rules can also be defined for comparison.

| **Parameter**  | **Type**                  | **Description**                                                                                                                                                                          |
| -------------- | ------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `module1_name` | string                    | First module involved in the assertion.                                                                                                                                                  |
| `module2_name` | string                    | Second module involved in the assertion.                                                                                                                                                 |
| `start`        | integer                   | Start index for the range check.                                                                                                                                                         |
| `count`        | integer                   | Number of values to check within the range.                                                                                                                                              |
| `rule`         | string                    | Message content comparison rule. Format is: `(message separator)\|(X);(X);...` Where `(X)` is `#` `(equals)`, `?` `(arbitrary)`, number `0-100 diff tolerance`. **Example:** ",\|#;?;15" |
| `result`       | boolean \| *not required* | It determines the expected outcome of the assertion. *(default: true)*                                                                                                                   |

```yaml
assert_range:
    module1_name: "module-1"
    module2_name: "module-2"
    start: 0
    count: 10
    rule: ";|#;10"
    result: true
```

### assert_app_is_running

Verifies if an external application is currently running.

| **Parameter**            | **Type**                  | **Description**                                                        |
| ------------------------ | ------------------------- | ---------------------------------------------------------------------- |
| `result`                 | boolean \| *not required* | It determines the expected outcome of the assertion. *(default: true)* |
| `name` \| *not required* | string                    | Name of program runner. *(default: "default")*                         |

```yaml
assert_app_is_running: true

assert_app_is_running: 
    result: true
    name: "app-1"
```

### assert_module_is_running

Verifies if a specific module is currently running.

| **Parameter** | **Type**                  | **Description**                                                        |
| ------------- | ------------------------- | ---------------------------------------------------------------------- |
| `module_name` | string                    | Name of module.                                                        |
| `result`      | boolean \| *not required* | It determines the expected outcome of the assertion. *(default: true)* |

```yaml
assert_module_is_running: 
    module_name: "module-1"
    result: true
```

### assert_json

Allows verification if the JSON object in a variable is identical to the expected JSON object.

| **Parameter** | **Type**                  | **Description**                                                                                                                                                                                      |
| ------------- | ------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `var_name`    | string                    | The name of the variable containing the JSON object to be asserted.                                                                                                                                  |
| `expected`    | string                    | The expected JSON object in string format for comparison.                                                                                                                                            |
| `exact_mode`  | string                    | Specifies a comparison mode for JSON objects that requires strict equality between expected and actual JSON. If false, it only compares parameters that are in the expected JSON. *(default: false)* |
| `result`      | boolean \| *not required* | It determines the expected outcome of the assertion. *(default: true)*                                                                                                                               |

```yaml
assert_json: 
    var_name: "module-1"
    expected: '{"name": "value", "atr1": 0}'
    exact_mode: false
    result: true
```

---

## Keywords for Working with Modules

This set includes keywords that enable working with communication modules.

### create_telnet_client

Creates a module that launches a new virtual Telnet client.

| **Parameter** | **Type**                  | **Description**                                              |
| ------------- | ------------------------- | ------------------------------------------------------------ |
| `name`        | string                    | Unique name for the module.                                  |
| `host`        | string \| *not required*  | Hostname or IP address to connect to. *(default: localhost)* |
| `port`        | integer \| *not required* | Port number on the host to connect to. *(default: 23)*       |
  
```yaml
create_telnet_client:
    name: "module-1"
    host: "localhost"
    port: 23
```

### create_telnet_server

Creates a module that launches a virtual Telnet server.

| **Parameter** | **Type**                  | **Description**                           |
| ------------- | ------------------------- | ----------------------------------------- |
| `name`        | string                    | Unique name for the module.               |
| `port`        | integer \| *not required* | Port number to listen on. *(default: 23)* |
  
```yaml
create_telnet_server:
    name: "module-1"
    port: 23
```

### create_web_crawler

Creates a module that launches a web crawler.

| **Parameter** | **Type** | **Description**                            |
| ------------- | -------- | ------------------------------------------ |
| `name`        | string   | Unique name for the module.                |
| `start_url`   | string   | URL to start crawling from.                |
| `max_depth`   | integer  | Maximum depth to crawl.                    |
| `analyzer`    | string   | Analyzer to use for parsing the web pages. |
  
```yaml
create_web_crawler:
    name: "module-1"
    start_url: "https://example.com"
    max_depth: 2
    analyzer: "word-freq:20"
```

### create_email_server

Creates a module that launches a virtual email server.

| **Parameter** | **Type** | **Description**                                    |
| ------------- | -------- | -------------------------------------------------- |
| `name`        | string   | Unique name for the module.                        |
| `port`        | integer  | Port number on which the email server will listen. |

```yaml
create_email_server:
    name: "module-1"
    port: 25
```

### create_rest_tester

Creates a module that launches an HTTP client for testing REST APIs.

| **Parameter**  | **Type** | **Description**                                     |
| -------------- | -------- | --------------------------------------------------- |
| `name`         | string   | Unique name for the module.                         |
| `url`          | string   | URL of the REST API to test.                        |
| `request_type` | string   | Type of HTTP request to send (GET, POST, etc.).     |
| `content_type` | string   | Data type in request. *(default: application/json)* |
  
```yaml
create_rest_tester:
    name: "module-1"
    url: "http://api.example.com"
    request_type: "POST"
    content_type: "application/json"
```

### create_soap_tester

Creates a module for testing SOAP services.

| **Parameter** | **Type** | **Description**                  |
| ------------- | -------- | -------------------------------- |
| `name`        | string   | Unique name for the module.      |
| `url`         | string   | URL of the SOAP service to test. |

```yaml
create_soap_tester:
    name: "module-1"
    url: "http://soap.example.com/ws"
```

### create_mqtt_client

Creates a module that launches a virtual MQTT client.

| **Parameter** | **Type**                 | **Description**                                                         |
| ------------- | ------------------------ | ----------------------------------------------------------------------- |
| `name`        | string                   | Unique name for the module.                                             |
| `topics`      | array \| *not required*  | List of topics to subscribe to. *(default: empty)*                      |
| `broker_url`  | string \| *not required* | URL of the MQTT broker to connect to. *(default: tcp://localhost:1883)* |
  
```yaml
create_mqtt_client:
    name: "module-1"
    topics: ["topic1", "topic2"]
    broker_url: "tcp://localhost:1883"
```

### create_mqtt_broker

Creates a module that launches an MQTT broker.

| **Parameter** | **Type**                  | **Description**                                    |
| ------------- | ------------------------- | -------------------------------------------------- |
| `name`        | string                    | Unique name for the module.                        |
| `port`        | integer \| *not required* | Port number for the MQTT broker. *(default: 1883)* |

```yaml
create_mqtt_broker:
    name: "module-1"
    port: 1883
```

### termite_module

Terminates a running module that is no longer needed.

| **Parameter** | **Type** | **Description**                  |
| ------------- | -------- | -------------------------------- |
| `default`     | string   | Name of the module to terminate. |

```yaml
termite_module:
    name: "module-1"
```

### module_send

Sends a message using a specific module.

| **Parameter** | **Type**               | **Description**                                                                             |
| ------------- | ---------------------- | ------------------------------------------------------------------------------------------- |
| `name`        | string                 | Name of the sending module.                                                                 |
| `message`     | string                 | Message to send. For different modules, the content may have different format requirements. |
| `delay`       | long \| *not required* | How long it will take to send a message after calling this keyword.                         |

```yaml
module_send:
    name: "module-1"
    message: "Message $your-var"
    delay: 0
```

### create_filter_action

Creates a filter for actions triggered upon message reception. Text content can be filtered.

| **Parameter**    | **Type**                 | **Description**                                                                                         |
| ---------------- | ------------------------ | ------------------------------------------------------------------------------------------------------- |
| `name`           | string                   | Name of module.                                                                                         |
| `text`           | string                   | Text to filter on.                                                                                      |
| `tag`            | string \| *not required* | Tag of message. *(default: "")*                                                                         |
| `mode`           | string \| *not required* | Filter mode. There are these modes: "equals", "contains", "startswith", "endswith". *(default: equals)* |
| `case_sensitive` | boolean                  | Whether the filter should be case sensitive. *(default: true)*                                          |

```yaml
create_filter_action:
    name: "module-1"
    text: "Text"
    tag: "Message-tag"
    mode: "equals"
    case_sensitive: true
```

### clear_filter_actions

Removes all action filters for a specific module.

| **Parameter** | **Type** | **Description** |
| ------------- | -------- | --------------- |
| `default`     | string   | Name of module. |

```yaml
clear_filter_actions:
    name: "module-1"
```
