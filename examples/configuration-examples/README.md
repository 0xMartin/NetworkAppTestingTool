# Example configurations

**[Go Back](../../README.md)**

This directory **contains example configuration** files for various testing scenarios using the **NATT black box testing tool**. Below is a list of the available test configuration files:

## List of Configuration Examples:

- [test-config-IM-server.yaml](./test-config-IM-server.yaml) - Configuration for testing Instant Messaging (IM) server interactions.

- [test-config-MQTT.yaml](./test-config-MQTT.yaml) - Configuration for testing MQTT (Message Queuing Telemetry Transport) protocols.

- [test-config-REST.yaml](./test-config-REST.yaml) - Configuration for testing RESTful API services.

- [test-config-SOAP.yaml](./test-config-SOAP.yaml) - Configuration for testing SOAP (Simple Object Access Protocol) web services.

- [test-config-email.yaml](./test-config-email.yaml) - Configuration for testing email protocols and interactions.

- [test-config-telnet-client.yaml](./test-config-telnet-client.yaml) - Configuration for testing Telnet client connections and commands.

- [test-config-telnet-server.yaml](./test-config-telnet-server.yaml) - Configuration for testing Telnet server responses and behaviors.

- [test-config-web-crawler.yaml](./test-config-web-crawler.yaml) - Configuration for testing web crawling functionality and behavior.

  ## 🧪 Test Configuration Samples
    
  ### Testing of simple application for email sending
  
  This configuration defines a set of automatic tests to evaluate the functionality of the email client application for sending email. The tests include scenarios related to sending emails. Each test case is designed to simulate different user interactions and verify that the client behaves as expected.
  
  ```yaml
  test_root:
    max_points: 4.0
    initial_steps: 
      - create_email_server:
          name: "server-1"
          port: 9999
    test_suites: 
      - test_suite:
          name: "Basic Email Sending Test"
          delay: 500
          test_cases: 
            - test_case:
                name: "Email Sending Test"
                description: "Tests the sending of one email. The content of the sent email is not verified, only whether it arrived at the server."
                steps: 
                  - run_app: "java -jar app/build/libs/app.jar localhost 9999 sender@email.com recipient@email.com Subject1 'Text Message'"
                  - wait_until:
                      module_name: "server-1"
                      time_out: 5000
                  - count_and_store:
                      var_name: "var-1"
                      module_name: "server-1"
                  - assert_equals:
                      var_name: "var-1"
                      value: 1
                  - wait: 500
            - test_case:
                name: "Email Content Test 1"
                description: "Tests the sending of one email. After receiving the email, its content is verified."
                steps: 
                  - run_app: "java -jar app/build/libs/app.jar localhost 9999 sender@email.com recipient@email.com Subject1 'Text Message'"
                  - wait_until:
                      module_name: "server-1"
                      time_out: 5000
                  - store_to_var:
                      var_name: "var-1"
                      module_name: "server-1"
                      tag: "Subject1"
                      mode: "equals"
                  - assert_string:
                      var_name: "var-1"
                      expected: "Text Message"
                      mode: "equals"
                      case_sensitive: true
                  - wait: 500
            - test_case:
                name: "Email Content Test 2"
                description: "Tests the sending of one email. After receiving the email, its content is verified."
                steps: 
                  - run_app: "java -jar app/build/libs/app.jar localhost 9999 sender@email.com recipient@email.com Subject1111 'Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Proin mattis lacinia justo. Curabitur bibendum justo non orci. Sed ut perspiciatis unde omnis iste natus error sit voluptatem accusantium doloremque laudantium, totam rem aperiam, eaque ipsa quae ab illo inventore veritatis et quasi architecto beatae vitae dicta sunt explicabo.'"
                  - wait_until:
                      module_name: "server-1"
                      time_out: 5000
                  - store_to_var:
                      var_name: "var-1"
                      module_name: "server-1"
                      tag: "Subject1111"
                      mode: "equals"
                  - assert_string:
                      var_name: "var-1"
                      expected: "Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Proin mattis lacinia justo. Curabitur bibendum justo non orci. Sed ut perspiciatis unde omnis iste natus error sit voluptatem accusantium doloremque laudantium, totam rem aperiam, eaque ipsa quae ab illo inventore veritatis et quasi architecto beatae vitae dicta sunt explicabo."
                      mode: "contains"
                      case_sensitive: true
                  - wait: 500
            - test_case:
                name: "Invalid Email Sending Test"
                description: "An email with invalid data is sent through the tested application. It is assumed that the email will not be sent."
                steps: 
                  - run_app: "java -jar app/build/libs/app.jar localhost 9999 sender@email.com recipient@email.com"
                  - wait: 3000
                  - count_and_store:
                      var_name: "var-1"
                      module_name: "server-1"
                  - assert_equals:
                      var_name: "var-1"
                      value: 0
                  - wait: 500
  ```
  
  ### Testing of telnet client
  
  This configuration describes a series of automated tests designed to assess the functionality of the telnet client application. The tests are structured to verify both the sending and receiving capabilities of the telnet client. Each test case simulates different scenarios to see if the client works correctly under different conditions.
  
  ```yaml
  test_root:
    max_points: 4.0
    initial_steps: 
      # Start a virtual telnet server on port 9999
      - create_telnet_server:
          name: "tel-server-1"
          port: 9999
    test_suites: 
      - test_suite:
          name: "Message Sending Test"
          delay: 700
          initial_steps: 
            - run_app: "java -jar app/build/libs/app.jar localhost 9999"
            - wait: 200
          test_cases: 
            - test_case:
                name: "Message Sending Test 1"
                description: "The tested telnet client sends several test messages to the server and then verifies whether these messages were delivered correctly."
                steps: 
                  # Test application sends messages via std in for sending
                  - standard_stream_send: "This is test message 1"
                  - wait: 500
                  - standard_stream_send: "Next test message"
                  - wait: 1200
                  # Verify received messages
                  - buffer_get:
                      var_name: "var-1"
                      module_name: "tel-server-1"
                      index: 0
                  - buffer_get:
                      var_name: "var-2"
                      module_name: "tel-server-1"
                      index: 1
                  - assert_string:
                      var_name: "var-1"
                      expected: "This is test message 1"
                      mode: "equals"
                      case_sensitive: true
                  - assert_string:
                      var_name: "var-2"
                      expected: "Next test message"
                      mode: "equals"
                      case_sensitive: true
            - test_case:
                name: "Message Sending Test 2"
                description: "The tested telnet client sends many messages to the server in a short time. It will be verified whether all of them were delivered."
                steps:
                  # Test application sends messages via std in for sending
                  - standard_stream_send: "This is test message 1"
                  - standard_stream_send: "This is test message 2"
                  - standard_stream_send: "This is test message 3"
                  - standard_stream_send: "This is test message 4"
                  - standard_stream_send: "This is test message 5"
                  - wait: 1200
                  # Verify received messages
                  - store_to_var:
                      var_name: "var-1"
                      module_name: "tel-server-1"
                      text: "This is test message 3"
                      mode: "equals"
                      case_sensitive: true
                  - assert_string:
                      var_name: "var-1"
                      expected: "This is test message 3"
                      mode: "equals"
                      case_sensitive: true
                  - count_and_store:
                      var_name: "var-2"
                      module_name: "tel-server-1"
                  - assert_equals:
                      var_name: "var-2"
                      value: 5
            - test_case:
                name: "Message Sending Test 3"
                description: "Several messages will be sent. It will be tested whether the list of received messages does not contain messages that were not sent (but were sent by the previous TC)."
                steps:
                  # Test application sends messages via std in for sending
                  - standard_stream_send: "This is test message 1"
                  - standard_stream_send: "This is test message 3"
                  - standard_stream_send: "This is test message 5"
                  - wait: 1200
                  # Verify that the list of received messages does not contain a message that was not sent (but was sent by the previous TC)
                  - store_to_var:
                      var_name: "var-1"
                      module_name: "tel-server-1"
                      text: "This is test message 4"
                      mode: "equals"
                      case_sensitive: true
                  - assert_string:
                      var_name: "var-1"
                      expected: ""
                      mode: "equals"
                      case_sensitive: true
      - test_suite:
          name: "Message Receiving Test"
          delay: 700
          initial_steps: 
            - run_app: "java -jar app/build/libs/app.jar localhost 9999"
            - wait: 200
          test_cases: 
            - test_case:
                name: "Message Receiving Test 1"
                description: "The virtual server sends a message to the connected telnet client, and it will be tested whether the client received this message correctly."
                steps: 
                  # Send a message to the test application
                  - module_send:
                      name: "tel-server-1"
                      message: "This is message from server"
                  - wait: 1200
                  # Verify received content
                  - buffer_get:
                      var_name: "var-1"
                      module_name: "app-std-out"
                      index: 0
                  - assert_string:
                      var_name: "var-1"
                      expected: "This is message from server"
  
  ```
  
