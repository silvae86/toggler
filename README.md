# Toggles Service

A demonstration of the development of a secured API using modern technology. 


## Installation

- Allow chrome to accept a self-signed certificate for tests. In production, a certificate would have to be parametrized in the `conf/application.conf` file. 
    - Go to `chrome://flags/#allow-insecure-localhost`
    - Enable the setting
    - Restart Chrome



## Usage

## Testing


## Architecture

### Scalability

- Since the application does not keep any state information , it can be scaled to any number of replicas using Docker Swarm.
- MongoDB can run in cluster mode if necessary by adding more containers as needed 

### Security and authentication

- Only HTTPS is allowed to prevent MITM attacks
- JWT is used to secure the API against undesired access

### Tech Stack

- OpenAPI 2.0 and Swagger-UI
- Java (Play Framework 2.7.2), SBT, Gradle
- MongoDB database
- Docker