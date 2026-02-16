#!/bin/bash
# file: lbinst.sh

# Installation von Liquibase
wget https://github.com/liquibase/liquibase/releases/download/v4.31.1/liquibase-4.31.1.tar.gz
tar -xzf liquibase-4.31.1.tar.gz
sudo mv liquibase /opt/liquibase
sudo ln -s /opt/liquibase/liquibase /usr/local/bin/liquibase
