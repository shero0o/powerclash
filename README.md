# Getting Started
## 1.	Installation process
Install docker desktop.

## 2.	Software dependencies
## 3.	Latest releases
## 4.	API references


# Build and Test
````
start "C:\Program Files\Docker\Docker\Docker Desktop.exe"
docker network create shared-backend
cd Spiel/wallet
docker-compose up -d --build
cd ../shop_catalogue
docker-compose up -d --build
cd ../account
docker-compose up -d --build
cd ../spiel_frontend
npm install
npm run dev
cd ../spiel_service
./mvnw spring-boot:run
````
Hinweis: Manchmal schlägt docker-compose up --build fehl, dann muss man das einfach nochmal ausführen


# Contribute
TODO: Explain how other users and developers can contribute to make your code better. 

If you want to learn more about creating good readme files then refer the following [guidelines](https://docs.microsoft.com/en-us/azure/devops/repos/git/create-a-readme?view=azure-devops). You can also seek inspiration from the below readme files:
- [ASP.NET Core](https://github.com/aspnet/Home)
- [Visual Studio Code](https://github.com/Microsoft/vscode)
- [Chakra Core](https://github.com/Microsoft/ChakraCore)