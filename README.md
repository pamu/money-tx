# money-tx
Money transfers API backend.

## Overall summary

1. Controller behind HTTP endpoints can receive commands or queries.
2. Command or query is then forwarded to actor.
3. Actor replies with results for the query if query is sent to it.
4. When command is sent to actor,
    1. Actor validates command.
    2. Command can be rejected with error
    3. Accepted commands become events.
    4. Events trigger state updates.
     
## Features

1. Create account
2. Deposit
3. Withdraw
4. Transfer

## Language

Kotlin

## Libraries used

1. Javalin (HTTP)
2. Akka actor (State management)
3. Kotlin test (Testing)
4. Jackson (Json)
5. Slf4j (Logging)

## Code structure

Base package is `com.moneytx`.

#### Application entry point

`MoneyTxApp.kt` is the entry point of this app and has `main` method declared.

#### Domain

Domain models, exceptions, validations, events and commands are
declared in `domain` package.

#### logic

Application logic and state management goes into `logic` package.

## Dataflow

#### Controller

Controller receives command/query objects from routes.

#### Command actor

Command/Query objects are forwarded from controller
to command actor.

1. Queries get answered at this stage.
2. Commands get validated, commands can get rejected.
3. Accepted commands become events and are sent to event handler.

#### EventHandler

Event handler interprets the event and updates the state which is inside the actor
in a thread-safe manner. 


## Docker

### Build

```bash
 docker build -t moneytx .
```

### Run

```bash
docker run -p 8080:8080 --rm moneytx
```

## Build and run (no docker)
In the project root, use following commands

### Compile

```
./gradlew compileKotlin
```
### build 

```bash
./gradlew build
```

### Test

```bash
./gradlew test
```

### Run

```bash
./gradlew run --args=9090
```

Note: First value from args is considered as port number.
Default port number is `8080`.

## REST API

### Create Account

```bash
curl -X GET \
  http://localhost:8080/createAccount \
  -H 'Accept: */*' \
  -H 'Accept-Encoding: gzip, deflate' \
  -H 'Cache-Control: no-cache' \
  -H 'Connection: keep-alive' \
  -H 'Content-Type: application/json' \
  -H 'Host: localhost:8080' \
  -H 'cache-control: no-cache'
```
Result:
```
{"id":"3a93fe91-12e2-4741-bd7f-63fa398c4a74","currentBalance":{"value":0}}%
```

### Deposit

```
curl -X POST \
  http://localhost:8080/deposit \
  -H 'Accept: */*' \
  -H 'Accept-Encoding: gzip, deflate' \
  -H 'Cache-Control: no-cache' \
  -H 'Connection: keep-alive' \
  -H 'Content-Length: 103' \
  -H 'Content-Type: application/json' \
  -H 'Host: localhost:8080' \
  -H 'Postman-Token: 4fcb1074-0722-4cd1-9c47-2c7c15e3d390,0d4433b2-3f49-4af5-8c0a-7f4cfeaae3b9' \
  -H 'User-Agent: PostmanRuntime/7.20.1' \
  -H 'cache-control: no-cache' \
  -d '{
        "accountId": {
                "value": "3a93fe91-12e2-4741-bd7f-63fa398c4a74"
        },
        "amount": {
                "value": 20
        }
}'
```
Result:

```
{"id":"3a93fe91-12e2-4741-bd7f-63fa398c4a74","currentBalance":{"value":20}}%
```

### Withdraw

```
curl -X POST \
  http://localhost:8080/withdraw \
  -H 'Accept: */*' \
  -H 'Accept-Encoding: gzip, deflate' \
  -H 'Cache-Control: no-cache' \
  -H 'Connection: keep-alive' \
  -H 'Content-Length: 103' \
  -H 'Content-Type: application/json' \
  -H 'Host: localhost:8080' \
  -H 'cache-control: no-cache' \
  -d '{
        "accountId": {
                "value": "3a93fe91-12e2-4741-bd7f-63fa398c4a74"
        },
        "amount": {
                "value": 10
        }
}'
```

Result:

```
{"id":"3a93fe91-12e2-4741-bd7f-63fa398c4a74","currentBalance":{"value":10}}%
```

### Transfer

```
[Documents] curl -X POST \
  http://localhost:8080/transfer \
  -H 'Accept: */*' \
  -H 'Accept-Encoding: gzip, deflate' \
  -H 'Cache-Control: no-cache' \
  -H 'Connection: keep-alive' \
  -H 'Content-Length: 169' \
  -H 'Content-Type: application/json' \
  -H 'Host: localhost:8080' \
  -H 'cache-control: no-cache' \
  -d '{
        "accountId": {
                "value": "3a93fe91-12e2-4741-bd7f-63fa398c4a74"
        },
        "amount": {
                "value": 10
        },
        "payee": {
                "value": "9bd85943-7cac-4e3f-907a-cfa97f621daf"
        }
}'
```

Result:

```
{"id":"3a93fe91-12e2-4741-bd7f-63fa398c4a74","currentBalance":{"value":0}}%
```