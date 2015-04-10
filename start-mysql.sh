#!/bin/bash
docker run --name avdb-mysql -e MYSQL_ROOT_PASSWORD=avdb -e MYSQL_DATABASE=avdb -e MYSQL_USER=avdb -e MYSQL_PASSWORD=avdb -p 3306:3306 -d mysql:5.7
