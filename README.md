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

