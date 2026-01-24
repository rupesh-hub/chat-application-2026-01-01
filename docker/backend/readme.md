1. Build Product Service Image
   ```shell
    docker build -t rupesh1997/chat-service-backend:1.0.1 \
       -t rupesh1997/chat-service-backend:latest \
       --build-arg ACTIVE_PROFILE=docker \
       --build-arg PROJECT_VERSION=1.0.1 \
       -f ../docker/backend/Dockerfile .
    ``` 

2. Run mysql container

    ```shell
         #network - check if it is already created
         docker network inspect chat-apps >/dev/null 2>&1 \
         || docker network create chat-apps --driver bridge
      
         #mysql container
         docker run -d -p 3306:3306 \
          --name mysql \
          --env-file .env \
          --network chat-apps mysql:8.0
    ```

3. Run Product Service Container

```shell
    docker run -d \
    -p 8181:8181 \
    --name backend-svc \
    --network chat-apps \
   --env-file .env \
    rupesh1997/chat-service-backend:latest
  ```

```shell
    docker kill chat-service-backend chat-service-frontend mysql && 
    docker rm chat-service-backend chat-service-frontend mysql && 
    docker system prune -f && 
    clear
``` 
   