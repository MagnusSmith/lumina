

## Instructions

First install docker

Start up the mongodb docker container.   users will be automatically created.  Run the following in the lumina directory 
``` 
docker compose up
```

Next run the spring application with

```
./gradlew bootRun
```

if you need to override the mongo uri then 

```
SPRING_DATA_MONGODB_URI=mongodb://tester:tester@localhost:28017/test ./gradlew bootRun 

```

Open a browser and go to
```
http://localhost:8080/swagger-ui/index.html
```



## NOTE: Validation is currently minimal so need to take care entering data


### Create a new Catalogue

Create a new catalogue item and define some configuration constraints.

We can set constraints that can be validated when a new meter is created later.


```http request
curl -X 'POST' \
  'http://localhost:8080/api/catalogueItem' \
  -H 'accept: */*' \
  -H 'Content-Type: application/json' \
  -d '{
  "model": "MAG001",
  "level": "GATEWAY",
  "type": "LORAWAN",
  "name": "string",
  "description": "The catalogue item description",
  "manufacturer": "The item manufacturer",
  "constraints": [
    {
      "type": "NUMERIC",
      "name": "age",
      "description": "Some numerical configuration",
      "numberType": "INTEGER",
      "min": 0,
      "max": 100,
      "isRequired": true
    },
    {
      "type": "TEXT",
      "name": "publicKey",
      "description": "Some textural configuration",
      "minLength": 0,
      "maxLength": 256,
      "isRequired": false
    }
  ]
}'
```

```http request
{
  "model": "MAG001",
  "level": "GATEWAY",
  "type": "LORAWAN",
  "name": "The name of the item",
  "description": "The catalogue item description",
  "manufacturer": "The item manufacturer",
  "constraints": [
    {
      "type": "NUMERIC",
      "name": "age",
      "description": "Some numerical configuration",
      "numberType": "INTEGER",
      "min": 0,
      "max": 100,
      "isRequired": true
    },
    {
      "type": "TEXT",
      "name": "publicKey",
      "description": "Some textural configuration",
      "minLength": 0,
      "maxLength": 256,
      "isRequired": false
    }
  ]
}
```





### Create a new client

```http request
curl -X 'POST' \
  'http://localhost:8080/api/client' \
  -H 'accept: */*' \
  -H 'Content-Type: application/json' \
  -d '{
  "name": "Client One"
}'
```

This will give you a response containing an id

### Create a project for the client

replace the id below with the one you got creating a new client

```http request
curl -X 'POST' \
  'http://localhost:8080/api/project' \
  -H 'accept: */*' \
  -H 'Content-Type: application/json' \
  -d '{
  "clientId": "660ae9a0c1e5a312013963ef",
  "name": "Project One"
}'

```
### Create a Location for the Project


replace the id below with the one you got creating a new project

```http request
curl -X 'POST' \
  'http://localhost:8080/api/location' \
  -H 'accept: */*' \
  -H 'Content-Type: application/json' \
  -d '{
  "name": "Location One",
  "projectId": "660aea55c1e5a312013963f0"
}'
```
The response will contain a location id.  This will be needed when defining meters for this location.


If we now do a GET on the client controller we will see these all together

```http request
curl -X 'GET' \
  'http://localhost:8080/api/client' \
  -H 'accept: */*'
```

returns

```http request
[
  {
    "id": "660ae9a0c1e5a312013963ef",
    "name": "Client One",
    "projects": [
      {
        "id": "660aea55c1e5a312013963f0",
        "clientId": "660ae9a0c1e5a312013963ef",
        "name": "Project One",
        "locations": [
          {
            "id": "660aeb0fc1e5a312013963f1",
            "name": "Location One",
            "projectId": "660aea55c1e5a312013963f0",
            "meterIds": [
              "660aee80c1e5a312013963f3"
            ]
          }
        ]
      }
    ]
  }
]
```


### Add a new meter to a location

Define a meter based upon the `MAG001` catalogue item.

We added the two configuration values required for the item.   

@TODO: These can then be fully validated against the catalogue with appropriate error messages for violations returned

```http request
curl -X 'POST' \
'http://localhost:8080/api/meter' \
  -H 'accept: */*' \
  -H 'Content-Type: application/json' \
  -d '{
  "locationId": "660aeb0fc1e5a312013963f1",
  "model": "MAG001",
  "lines": [
    {
      "type": "NUMERIC",
      "name": "age",
      "numberType": "INTEGER",
      "value": 50
    },
    {
      "type": "TEXT",
      "name": "publicKey",
      "value": "SHA-256:mypublickeyvalue"
    }
  ]
}'
```

We get the following response which gives additional aggregated information from the catalogue
```http request
{
  "id": "660aee80c1e5a312013963f3",
  "locationId": "660aeb0fc1e5a312013963f1",
  "model": "MAG001",
  "level": "GATEWAY",
  "type": "LORAWAN",
  "name": "The name of the item",
  "description": "The catalogue item description",
  "manufacturer": "The item manufacturer",
  "lines": [
    {
      "line": {
        "type": "NUMERIC",
        "name": "age",
        "numberType": "INTEGER",
        "value": 50
      },
      "constraint": {
        "type": "NUMERIC",
        "name": "age",
        "description": "Some numerical configuration",
        "numberType": "INTEGER",
        "min": 0,
        "max": 100,
        "isRequired": true
      }
    },
    {
      "line": {
        "type": "TEXT",
        "name": "publicKey",
        "value": "SHA-256:mypublickeyvalue"
      },
      "constraint": {
        "type": "TEXT",
        "name": "publicKey",
        "description": "Some textural configuration",
        "minLength": 0,
        "maxLength": 256,
        "isRequired": false
      }
    }
  ]
}
```

If we want to see the meters associated with a location we would call

```http request
curl -X 'GET' \
  'http://localhost:8080/api/location/660aeb0fc1e5a312013963f1' \
  -H 'accept: */*'
```

This gives us the configurations of any meters at that location

```http request
{
  "id": "660aeb0fc1e5a312013963f1",
  "name": "Location One",
  "projectId": "660aea55c1e5a312013963f0",
  "meters": [
    {
      "id": "660aee80c1e5a312013963f3",
      "locationId": "660aeb0fc1e5a312013963f1",
      "model": "MAG001",
      "lines": [
        {
          "type": "NUMERIC",
          "name": "age",
          "numberType": "INTEGER",
          "value": 50
        },
        {
          "type": "TEXT",
          "name": "publicKey",
          "value": "SHA-256:mypublickeyvalue"
        }
      ]
    }
  ]
}
```


