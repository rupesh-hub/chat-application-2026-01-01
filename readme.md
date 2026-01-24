```shell
docker run -d -p 3306:3306 \
--name mysql \
-e MYSQL_ROOT_PASSWORD=root \
-e MYSQL_DATABASE=chats \
mysql:8.0


docker run -d -p 1080:1080 \
-p 1025:1025 \
--name maildev \
-e MAIL_HOST=localhost \
-e MAIL_PORT=1025 \
-e MAIL_USERNAME=admin \
-e MAIL_PASSWORD=admin \
maildev/maildev
```

#### HELM COMMANDS
```bash
helm repo list
helm create charts
helm template chats ./charts -n chats
helm install fitverse ./helm -n chats
helm list -n chats
helm uninstall -n chats 

kubectl get all -n chats

```