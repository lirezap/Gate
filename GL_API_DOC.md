# GL API Documentation

## Overview

Gate is a message distribution gateway that provides REST APIs. It gets messages as HTTP requests, converts requests to
appropriate binary format messages and then distributes messages to the general ledger server.

- **Base URL:** `https://gate.example.com/v1` (Depends on environment setup)
- **Content Type:** `application/json`
- **Version:** `v1`

---

## Error Response Format

Any API call failure will return 4xx or 5xx HTTP status code with the following JSON response body:

```json
{
  "code": "request_body.not_valid",
  "message": "the request body format is not valid"
}
```

All error codes are listed at the end of this file.

---

## Digital Signature

Some of the API endpoints require you as API caller to provide the digital signature of your request in signature field.
The digital signature required depends on each endpoint independently, however the general steps is as follows:

If the API endpoint said that `digital signature of x,y,z string` it means:

- Build the string of x,y,z (x, y and z are different values and comma character is a separator)
- Generate the SHA3-512withRSA digital signature of previous string by using the private key
- Use a Base64 encoder to encode the generated digital signature into base64 string representation
- Use the result of step 3 as the value of `signature` field in your JSON body request

---

## Transaction ID Format

The transaction id format required for transactions must be built using the following steps:

- The format is as `ts:string`
- The `ts` part is timestamp, we mean the standard milliseconds since epoch (exp, 1764702130671)
- The `string` part is a string representation of an identifier you provide for the transaction (exp, myTID1)
- The whole `ts:string` must be unique (exp, 1764702130671:myTID1)
- The `ts` part must not be newer than the GL server current time (it must not be a timestamp in future), and must not
  be older than GL server current time minus 10 seconds.

---

## Endpoints

### 1. Fetch Account

Fetches an account and all of its balances in different wallets.

#### URI

```POST /gl/messages?id=103```

#### Request

**Query Parameters**

| Name | Type    | Required | Description             |
|------|---------|----------|-------------------------|
| `id` | integer | Yes      | Message id: must be 103 |

**Body Parameters**

| Field       | Type    | Required | Description                                                              |
|-------------|---------|----------|--------------------------------------------------------------------------|
| `ledger`    | integer | Yes      | Ledger id: must be greater than 0                                        |
| `account`   | long    | Yes      | Account id: must be greater than 0                                       |
| `signature` | string  | Yes      | Digital signature: digital signature of ledger,account string (exp, 1,2) |

#### Successful Response

```json
[
  {
    "ledger": 1,
    "account": 2,
    "wallet": 2,
    "currency": "IRR",
    "balance": 1000
  },
  {
    "ledger": 1,
    "account": 2,
    "wallet": 1,
    "currency": "IRR",
    "balance": 1000
  }
]
```

### 2. Fetch Wallet

Fetches the balance of a wallet of a specific account.

#### URI

```POST /gl/messages?id=104```

#### Request

**Query Parameters**

| Name | Type    | Required | Description             |
|------|---------|----------|-------------------------|
| `id` | integer | Yes      | Message id: must be 104 |

**Body Parameters**

| Field       | Type    | Required | Description                                                                       |
|-------------|---------|----------|-----------------------------------------------------------------------------------|
| `ledger`    | integer | Yes      | Ledger id: must be greater than 0                                                 |
| `account`   | long    | Yes      | Account id: must be greater than 0                                                |
| `wallet`    | integer | Yes      | Wallet id: must not be 0                                                          |
| `signature` | string  | Yes      | Digital signature: digital signature of ledger,account,wallet string (exp, 1,2,3) |

#### Successful Response

```json
{
  "ledger": 1,
  "account": 2,
  "wallet": 1,
  "currency": "IRR",
  "balance": 1000
}
```

### 3. Submit a group of transactions

Submits a group of transactions, with different sources and different destinations. Some transactions may fail and some
may be succeeded.

#### URI

```POST /gl/messages?id=202```

#### Request

**Query Parameters**

| Name | Type    | Required | Description             |
|------|---------|----------|-------------------------|
| `id` | integer | Yes      | Message id: must be 202 |

**Body Parameters**

| Field          | Type  | Required | Description                                 |
|----------------|-------|----------|---------------------------------------------|
| `transactions` | array | Yes      | An array of JSON objects of following table |

| Field                | Type    | Required | Description                                                                                                                                                         |
|----------------------|---------|----------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `ledger`             | integer | Yes      | Ledger id: must be greater than 0                                                                                                                                   |
| `sourceAccount`      | long    | Yes      | Source account id: must be greater than 0                                                                                                                           |
| `sourceWallet`       | integer | Yes      | Source wallet id: must not be 0                                                                                                                                     |
| `destinationAccount` | long    | Yes      | Destination account id: must be greater than 0                                                                                                                      |
| `destinationWallet`  | integer | Yes      | Destination wallet id: must not be 0                                                                                                                                |
| `id`                 | string  | Yes      | Transaction id: must be in "timestamp:string" format                                                                                                                |
| `currency`           | string  | Yes      | Currency (exp, IRR)                                                                                                                                                 |
| `amount`             | long    | Yes      | The amount of transfer                                                                                                                                              |
| `maxOverdraftAmount` | long    | No       | The maximum amount of negative balance that the source wallet can have after this transfer                                                                          |
| `metadata`           | string  | No       | Metadata in string format, can be in JSON format of extra fields you want                                                                                           |
| `signature`          | string  | Yes      | Digital signature: digital signature of ledger,sourceAccount,sourceWallet,destinationAccount,destinationWallet,currency,amount string (exp, 1,100,1,200,1,IRR,5000) |

#### Successful Response

It will return an array of objects of unsuccessful transactions. So if empty array returned it means all transactions
successfully submitted.

```json
[]
```

But if the array was something like the following examples, it means one or more transactions (with provided id) was not
successful.

```json
[
  {
    "id": "1764670488057:TID1",
    "reason": "id.ts_part_not_valid"
  },
  {
    "id": "1764670488057:TID2",
    "reason": "id.ts_part_not_valid"
  }
]
```

```json
[
  {
    "id": "1764670673546:TID1",
    "reason": "balance.not_enough"
  }
]
```

#### Failure Reasons List

Here is the list of all reasons that may fail a transaction:

| Reason                         |
|--------------------------------|
| `id.not_valid`                 |
| `ledger.not_valid`             |
| `sourceAccount.not_valid`      |
| `sourceWallet.not_valid`       |
| `destinationAccount.not_valid` |
| `destinationWallet.not_valid`  |
| `transaction.not_valid`        |
| `currency.not_valid`           |
| `amount.not_valid`             |
| `maxOverdraftAmount.not_valid` |
| `metadata.length_exceeded`     |
| `transaction.not_allowed`      |
| `balance.not_enough`           |
| `transaction.already_exists`   |

### 4. Submit a group of transactions atomically

Submits a group of transactions atomically, with different sources and different destinations. All transactions must be
fail or succeeded (All or None).

#### URI

```POST /gl/messages?id=203```

#### Request

**Query Parameters**

| Name | Type    | Required | Description             |
|------|---------|----------|-------------------------|
| `id` | integer | Yes      | Message id: must be 203 |

**Body Parameters**

| Field          | Type  | Required | Description                                 |
|----------------|-------|----------|---------------------------------------------|
| `transactions` | array | Yes      | An array of JSON objects of following table |

| Field                | Type    | Required | Description                                                                                                                                                         |
|----------------------|---------|----------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `ledger`             | integer | Yes      | Ledger id: must be greater than 0                                                                                                                                   |
| `sourceAccount`      | long    | Yes      | Source account id: must be greater than 0                                                                                                                           |
| `sourceWallet`       | integer | Yes      | Source wallet id: must not be 0                                                                                                                                     |
| `destinationAccount` | long    | Yes      | Destination account id: must be greater than 0                                                                                                                      |
| `destinationWallet`  | integer | Yes      | Destination wallet id: must not be 0                                                                                                                                |
| `id`                 | string  | Yes      | Transaction id: must be in "timestamp:string" format                                                                                                                |
| `currency`           | string  | Yes      | Currency (exp, IRR)                                                                                                                                                 |
| `amount`             | long    | Yes      | The amount of transfer                                                                                                                                              |
| `maxOverdraftAmount` | long    | No       | The maximum amount of negative balance that the source wallet can have after this transfer                                                                          |
| `metadata`           | string  | No       | Metadata in string format, can be in JSON format of extra fields you want                                                                                           |
| `signature`          | string  | Yes      | Digital signature: digital signature of ledger,sourceAccount,sourceWallet,destinationAccount,destinationWallet,currency,amount string (exp, 1,100,1,200,1,IRR,5000) |

#### Successful Response

It will return an array of first unsuccessful transaction. So if empty array returned it means `all` transactions
successfully submitted.

```json
[]
```

But if the array was something like the following example, it means one of transactions (with provided id) was not
successful; Thereby `none` of transactions submitted.

```json
[
  {
    "id": "1764670673546:TID1",
    "reason": "balance.not_enough"
  }
]
```

#### Failure Reasons List

Here is the list of all reasons that may fail a transaction:

| Reason                         |
|--------------------------------|
| `id.not_valid`                 |
| `ledger.not_valid`             |
| `sourceAccount.not_valid`      |
| `sourceWallet.not_valid`       |
| `destinationAccount.not_valid` |
| `destinationWallet.not_valid`  |
| `transaction.not_valid`        |
| `currency.not_valid`           |
| `amount.not_valid`             |
| `maxOverdraftAmount.not_valid` |
| `metadata.length_exceeded`     |
| `transaction.not_allowed`      |
| `balance.not_enough`           |
| `transaction.already_exists`   |

### 5. Transaction inquiry

Inquiries a transaction previously submitted.

#### URI

```POST /gl/messages?id=206```

#### Request

**Query Parameters**

| Name | Type    | Required | Description             |
|------|---------|----------|-------------------------|
| `id` | integer | Yes      | Message id: must be 206 |

**Body Parameters**

| Field       | Type    | Required | Description                                                                          |
|-------------|---------|----------|--------------------------------------------------------------------------------------|
| `ledger`    | integer | Yes      | Ledger id: must be greater than 0                                                    |
| `id`        | string  | Yes      | Transaction id: must be in "timestamp:string" format                                 |
| `signature` | string  | Yes      | Digital signature: digital signature of ledger,id string (exp, 1,1764579312847:tid1) |

#### Successful Response

```json
{
  "ledger": 2,
  "sourceAccount": 1,
  "sourceWallet": 1,
  "destinationAccount": 2,
  "destinationWallet": 1,
  "id": "1764671159484:TID2",
  "currency": "IRR",
  "amount": 1000,
  "maxOverdraftAmount": 4000,
  "metadata": "{\"description\":\"TID2\"}",
  "sourceWalletNewBalance": -4000,
  "destinationWalletNewBalance": 4000,
  "ts": 1764671163646
}
```

---

## Errors List

| Code                            | Message                                                                     |
|---------------------------------|-----------------------------------------------------------------------------|
| `id.not_valid`                  |                                                                             |
| `signature_verification.failed` |                                                                             |
| `gl.connect_timeout`            | GL server connect timeout                                                   |
| `gl.request_timeout`            | GL server request timeout                                                   |
| `gl.not_reachable`              | GL server not reachable                                                     |
| `handler.not_found`             |                                                                             |
| `request_body.not_valid`        |                                                                             |
| `parameter.not_valid`           |                                                                             |
| `resource.not_found`            |                                                                             |
| `too_many_requests`             |                                                                             |
| `forbidden`                     |                                                                             |
| `server.error`                  | internal server error                                                       |
| `message_format.not_valid`      | message's format is not valid                                               |
| `message_length.too_big`        | message's length is too big to handle                                       |
| `message_version.not_supported` | message's version not supported                                             |
| `message_size.not_valid`        | message's size is not valid                                                 |
| `message.not_supported`         | message is not supported                                                    |
| `account.not_found`             | account not found in provided ledger identifier                             |
| `wallet.not_found`              | wallet not found in provided ledger and account identifiers                 |
| `transaction.not_found`         | transaction not found                                                       |
| `batch.is_empty`                | at least one transaction is required to process batch                       |
| `transaction.not_synced`        | it seems transaction not yet synced, to inquiry please try in a few minutes |
| `ledger.not_valid`              |                                                                             |
| `id.ts_part_not_valid`          |                                                                             |
