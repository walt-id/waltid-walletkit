# waltid-wallet-backend

[![CI/CD Workflow for Walt.ID Wallet Backend](https://github.com/walt-id/waltid-wallet-backend/actions/workflows/ci.yml/badge.svg?branch=master)](https://github.com/walt-id/waltid-wallet-backend/actions/workflows/ci.yml)

The walt.id wallet backend provides the API and backend business logic for the walt.id web wallet.
Additionally it includes a reference implementation of a Verifier and Issuer (_future_) Portal backend. 

The provided services include:

###### Web wallet backend
* **User management**
    * Authorization is currently mocked and not production ready
    * User-context switching and user-specific encapsulated data storage
* **Basic user data management**
  * List dids
  * List credentials
* **Verifiable Credential and Presentation exchange**
  * Support for credential presentation exchange based on OIDC-SIOPv2 spec

###### Verifier portal backend
* **Wallet configuration**
  * Possibility to configure list of supported wallets (defaults to walt.id web wallet) 
* **Presentation exchange**
  * Support for presentation exchange based on OIDC-SIOPv2 spec

###### Issuer portal backend
* _To be announced_

### Usage

###### Verifier portal and wallet configuration:

```
{
  "verifierUiUrl": "http://localhost:4000",                 # URL of verifier portal UI
  "verifierApiUrl": "http://localhost:8080/verifier-api",   # URL of verifier portal API
  "wallets": {                                              # wallet configuration
    "walt.id": {                                            # wallet configuration key
      "id": "walt.id",                                      # wallet ID
      "url": "http://localhost:3000",                       # URL of wallet UI
      "presentPath": "CredentialRequest",                   # URL subpath for a credential presentation request
      "description": "walt.id web wallet"                   # Wallet description
    }
  }
}
```

###### Wallet backend configuration

User data (dids, keys, credentials) are currently stored under

`./data/<user@email.com>`

It is planned to allow users to define their own storage preferences, in the future.

###### APIs

The APIs are launched on port 8080.

A **swagger documentation** is available under 

`/api/swagger`

**Wallet API** is available under the context path `/api/`

**Verifier portal API** is available under the context path `/verifier-api/`

### Docker Compose

To spawn the backend together with the wallet frontend and verifier frontend, one can make use of the docker-compose configuration in
`./docker/`.

See the following examples for more information: 

This configuration will publish the wallet on **localhost:8080** and the verifier portal on **localhost:8081**

The wallet and verifier UIs will be available on the root context path `/`

The wallet and verifier backend APIs will be available on the context paths`/api/` and `/verifier-api/`

**docker-compose.yaml**
```
version: "3.3"
services:
  wallet-backend:
    image: waltid-wallet-backend:latest                                 # backend docker image
    volumes:
      - ./verifier-config.json:/Webwallet-Backend/verifier-config.json  # verifier and wallet configuration
      - ../data:/Webwallet-Backend/data                                 # data store volume
  wallet-ui:
    image: waltid-wallet-ui:latest                                      # wallet web ui docker image
  verifier-ui:
    image: waltid-verifier:latest                                       # verifier web ui docker image
  ingress:
    image: nginx:1.15.10-alpine
    ports:
      - target: 80
        published: 8080                                                 # wallet ui publish port
        protocol: tcp
        mode: host
      - target: 81
        published: 8081                                                 # verifier ui publish port
        protocol: tcp
        mode: host
    volumes:
      - ./ingress.conf:/etc/nginx/conf.d/default.conf                   # API gateway configuration
```

**ingress.conf**
```
server {
    listen 80;
    location ~* /(api|webjars|verifier-api)/ {
        proxy_pass http://wallet-backend:8080;        # wallet api backend URL
    }
    location / {
        proxy_pass http://wallet-ui:80/;              # wallet web UI URL
    }
}

server {
    listen 81;
    location /verifier-api/ {
        proxy_pass http://wallet-backend:8080;      # verifier API backend URL
    }
    location / {
        proxy_pass http://verifier-ui:80/;          # verifier UI backend URL
    }
}
```

**verifier-config.json**
See also configuration example above.

```
{
  "verifierUiUrl": "http://localhost:8081",
  "verifierApiUrl": "http://localhost:8081/verifier-api",
  "wallets": {
    "walt.id": {
      "id": "walt.id",
      "url": "http://localhost:8080",
      "presentPath": "CredentialRequest/",
      "description": "walt.id web wallet"
    }
  }
}
```