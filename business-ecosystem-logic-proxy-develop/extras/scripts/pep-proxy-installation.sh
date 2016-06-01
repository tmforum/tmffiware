#!/bin/bash

# Install Ubuntu dependencies
sudo apt-get update && \
	sudo apt-get install make g++ software-properties-common python-software-properties -y && \
	sudo add-apt-repository ppa:chris-lea/node.js -y && \
	sudo apt-get update && \
	sudo apt-get install nodejs git -y && \

# Download latest version of the code and install npm dependencies
git clone --branch 4.4 https://github.com/ging/fiware-pep-proxy.git && \
	cd fiware-pep-proxy && \
	npm install

sudo npm install forever -g

# config.js should be configured when the instance is up and running
cp config.js.template config.js

sudo forever start server.js