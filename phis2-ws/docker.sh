docker create --name phis-ws \
    -p 8080:8080 \
    -v $(pwd)/target/phis2ws.war:/usr/local/tomcat/webapps/phis2ws.war \
    tomcat | xargs docker start
