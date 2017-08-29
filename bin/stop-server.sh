#!/bin/bash
kill -9 `ps -ef | grep "DavinciStarter" | grep -v "grep" | awk '{print $2}'`
