#!/bin/bash
cd "$(dirname "$0")"
zip -d TelegramBot.jar 'META-INF/*.SF' 'META-INF/*.RSA' 'META-INF/*.DSA'
