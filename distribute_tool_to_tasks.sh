#!/usr/bin/env bash

tool_path="app/build/libs/app-all.jar"

out_name="NATT.jar"

cp "$tool_path" ../Tasks/1_Email_Sender/"$out_name" & echo "Inserted into task 1 ..."
cp "$tool_path" ../Solution/1_Email_Sender/"$out_name" & echo "Inserted into solution 1 ..."

cp "$tool_path" ../Tasks/2_Telnet_Client/"$out_name" & echo "Inserted into task 2 ..."
cp "$tool_path" ../Solution/2_Telnet_Client/"$out_name" & echo "Inserted into solution 2 ..."

cp "$tool_path" ../Tasks/3_Server/"$out_name" & echo "Inserted into task 3 ..."
cp "$tool_path" ../Solution/3_Server/"$out_name" & echo "Inserted into solution 3 ..."

cp "$tool_path" ../Tasks/4_IM_Server/"$out_name" & echo "Inserted into task 4 ..."
cp "$tool_path" ../Solution/4_IM_Server/"$out_name" & echo "Inserted into solution 4 ..."

cp "$tool_path" ../Tasks/5_WebCrawler/"$out_name" & echo "Inserted into task 5 ..."
cp "$tool_path" ../Solution/5_WebCrawler/"$out_name" & echo "Inserted into solution 5 ..."

cp "$tool_path" ../Tasks/6_RESTful_API_Server/"$out_name" & echo "Inserted into task 6 ..."
cp "$tool_path" ../Solution/6_RESTful_API_Server/"$out_name" & echo "Inserted into solution 6 ..."

cp "$tool_path" ../Tasks/7_SOAP_Web_Service/"$out_name" & echo "Inserted into task 2 ..."
cp "$tool_path" ../Solution/7_SOAP_Web_Service/"$out_name" & echo "Inserted into solution 2 ..."

cp "$tool_path" ../Tasks/8_MQTT_Client/"$out_name" & echo "Inserted into task 8 ..."
cp "$tool_path" ../Solution/8_MQTT_Client/"$out_name" & echo "Inserted into solution 8 ..."

echo "Done"