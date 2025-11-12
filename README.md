# Bus Booking System Backend

### 1. 

```
./gradlew clean build -x test
```

### 1. Build Config Server Image
```
docker build --build-arg JAR_FILE=config-server-0.0.1-SNAPSHOT.jar -t com.booking/config-server:0.0.1-SNAPSHOT ./config-server
```

### 2. Build User Service Image
```
docker build --build-arg JAR_FILE=user-service-0.0.1-SNAPSHOT.jar -t com.booking/user-service:0.0.1-SNAPSHOT ./services/user-service
```

### 3. Build API Gateway Image
```
docker build --build-arg JAR_FILE=api-gateway-0.0.1-SNAPSHOT.jar -t com.booking/api-gateway:0.0.1-SNAPSHOT ./api-gateway
```

### 4. Deploy with Docker Swarm
```
docker stack deploy -c docker-compose.yml bus-booking-stack
```

### Remove old containers
```
docker stack rm bus-booking-stack
```

### 1. Generate SSH Key

``` Powershell
ssh-keygen -t rsa -b 4096 -C "your_address@gmail.com"
```

Enter file in which to save the key (C:\Users\username/.ssh/id_rsa): vexesieure_config_server_repo_rsa
Enter passphrase (empty for no passphrase): YourPassphrase
Enter same passphrase again: YourPassphrase

### 2. Locate your key

The command will save two files in the .ssh directory

| File Name           | Key Type         | Purpose                                                                            |  
|---------------------|------------------|------------------------------------------------------------------------------------|  
| ==github_rsa==      | **Private Key**  | **Keep this secret and never share it.** Used by your computer to authenticate.    |  
| ==github_rsa.pub==  | **Public Key**   | **This is the content you share** with services like GitHub.                       |  

You can quickly view the contents of the public key using this command (replace the filename if needed):
``` Bash
cat ~/vexesieure_config_server_repo_rsa.pub
```
~ is usually ==C:/Users/username/== or ==C:/Users/username/.ssh==


# 3. Add the key to the repository on GitHub