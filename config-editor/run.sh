#!/bin/bash

if ! command -v python3 &> /dev/null
then
    echo "Python3 is not installed. Please install Python3."
    exit 1
fi

python3 main.py