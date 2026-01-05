1. Build Product Service Image
   ```shell
    docker build -t rupesh1997/chat-service-backend:1.0.1 \
       -t rupesh1997/chat-service-backend:latest \
       --build-arg ACTIVE_PROFILE=docker \
       --build-arg PROJECT_VERSION=1.0.1 \
       -f ../docker/backend/Dockerfile .
    ```

2. Run Product Service Container
   ```shell
    docker network create chat-apps --driver bridge && 
    docker run -d \
    -p 8181:8181 \
    --name chat-service-backend \
    -e SPRING_PROFILES_ACTIVE=docker \
    --network chat-apps \
    rupesh1997/chat-service-backend:1.0.0
    ```

   ```shell
    #With mysql database dependency
    #network - check if it is already created
    docker network inspect chat-apps >/dev/null 2>&1 \
    || docker network create chat-apps --driver bridge
    docker system prune -f &&
    clear &&
    docker run -d \
    -p 8181:8181 \
    --name backend-svc \
    --network chat-apps \
    -e SPRING_PROFILES_ACTIVE=docker \
    -e SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/chats \
    -e SPRING_DATASOURCE_PASSWORD=root \
    -e MAIL_USERNAME=XXX \
    -e MAIL_PASSWORD=XXX \
    rupesh1997/chat-service-backend:latest
    ```


```shell
    docker stop chat-service-backend chat-service-frontend && 
    docker rm chat-service-backend chat-service-frontend && 
    docker system prune -f && 
    clear
``` 
   