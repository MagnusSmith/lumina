

## Instructions

First install docker

### Run using Docker image from GitHub Registry.


As part of the GitHub build process when we merge a PR to main then a docker image is built and pushed to GitHub Container Registry.

You can use this image to run the service locally without having to build the project.

To pull the image you will need to use the personal access token to login to the docker repository 
```
export CRPAT=<the pat token>
echo $CRPAT | docker login ghcr.io -u USERNAME --password-stdin
```
This should return Login Succeeded!

Now you can start then local docker stack that will pull the meter-config-serice from the container registry and startup the application with a local mongodb


In the same directory as the compose.yaml file enter
```
docker compose --profile=repository up
```

Open a browser and go to
```
http://localhost:8080/swagger-ui/index.html
```




### Local Build 

Install Java 25

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



### The Catalogue

The holds information about a Meter and its data constraints.    Some data constraints are added automatically by default as presets based upon the `level` and `type` of Meter.    We can add and remove further constraints for different models.

#### Constraints

There are three types of constraint


- Text
- Numeric
- Pattern

Each Constraint has `type`, `name` and `description` fields

The fields `isRequired` and `stage` are used to determine how the constraint should be validated.

`stage` represents the first stage that this constraint will be enforced.   All stages that come after the given `stage` will perform validation.

`isRequired` determines id the constraint is optional.  So that isRequired: true and stage:INTAKE would say that this field must be present and validated at stage INTAKE and after.  isRequired:false would mean that the field is only validated at stage INTAKE and after if it is present.  If it is missing then validation would pass at those stages.


##### Text Constraint


```json
{
"type": "TEXT",
"name": "publicKey",
"description": "Some textural configuration",
"minLength": 0,
"maxLength": 256,
"isRequired": false,
"stage": "INTAKE"
}
```

Adding `minLength` or `maxLength` will enforce validating the field length.  Note both these fields are optional and leaving them both off will leave the TEXT value unconstrained.


##### Numeric Constraint

```json
 {
      "type": "NUMERIC",
      "name": "age",
      "description": "Some numerical configuration",
      "numberType": "INTEGER",
      "min": 0,
      "max": 100,
      "isRequired": true,
      "stage": "INTAKE"
    }
```

The `numberType` can be `INTEGER` or `FLOAT` values.
`min` and `max` will be enforced as either integer or floating point bounds.  Again these bounds are optional and leaving them both off will leave the NUMERIC value unconstrained.

##### Pattern Constraint

```json
 {
      "type": "PATTERN",
      "name": "specialId",
      "description": "A special id split into 4 digit words separated by a '-'",
      "pattern": "\\d{4}-\\d{4}-\\d{4}-\\d{4}",
      "isRequired": true,
      "stage": "CONNECTION"
    }
```

The `pattern` field is a Java escaped regex used to validate the constraint

<pre>
Construct	Description
.	Any character (may or may not match line terminators)
\d	A digit: [0-9]
\D	A non-digit: [^0-9]
\s	A whitespace character: [ \t\n\x0B\f\r]
\S	A non-whitespace character: [^\s]
\w	A word character: [a-zA-Z_0-9]
\W	A non-word character: [^\w]
</pre>

[see full details](https://docs.oracle.com/en/java/javase/22/docs/api/java.base/java/util/regex/Pattern.html)
Create a new catalogue item and define some configuration constraints.




```http request
curl -X 'POST' \
  'http://localhost:8080/api/catalogueItem' \
  -H 'accept: */*' \
  -H 'Content-Type: application/json' \
  -d '{
  "model": "MAG001",
  "level": "GATEWAY",
  "type": "LORAWAN",
  "description": "The catalogue item description",
  "manufacturer": "The item manufacturer"
}'
```

In this example two default preset constraints are added

```http request
{
  "id": "012345467",
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
      "isRequired": true,
      "stage": "One"
    },
    {
      "type": "TEXT",
      "name": "publicKey",
      "description": "Some textural configuration",
      "minLength": 0,
      "maxLength": 256,
      "isRequired": false
      "stage": "Two"
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

These will then be fully validated against the catalogue with appropriate error messages for violations returned at the appropriate stage.

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


