#!/bin/bash
docker run --name avdb-mysql -e MYSQL_ROOT_PASSWORD=mypw -e MYSQL_DATABASE=avdb -e MYSQL_USER=avdb -e MYSQL_PASSWORD=avdb -p 6606:3306 -d mysql/mysql-server:5.7
