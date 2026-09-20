# API Product Canvas Bounded Context name version of canvas

## Value Propositions

- Value proposition of the Bounded Context
- Value proposition of the Bounded Context
- Value proposition of the Bounded Context

## Core functions

- Core function of the Bounded Context
- Core function of the Bounded Context
- Core function of the Bounded Context

## Contact

%email address of the team responsible for the Bounded Context%

## Synchronous API

### Synchronous protocol

e.g. https with REST gRPC, GraphQL

### Architectural approach

e.g. RESTful API

### Server

%Base server name%

### Endpoints

#### /%aggregates%
<!--use aggregate name of the Bounded Context in plural-->

| Method | Parameters      | Request body | Responses                                      |
|--------|-----------------|--------------|------------------------------------------------|
| GET    | Search criteria |              | 200: aggregate list, 401, 403, 500, default    |
| POST   |                 | aggregate    | 201: Link to aggregate, 401, 403, 500, default |

#### /%aggregates%/{aggregateId}
<!--use aggregate name of the Bounded Context in plural, aggregateId usually a UUID-->

| Method | Parameters  | Request body                       | Responses                                           |
|--------|-------------|------------------------------------|-----------------------------------------------------|
| GET    | aggregateId |                                    | 200: aggregate, 401, 403, 404, 500, default         |
| PUT    | aggregateId | aggregate                          | 200: Link to aggregate, 401, 403, 404, 500, default |
| PATCH  | aggregateId | aggregate with optional properties | 200: Link to aggregate, 401, 403, 404, 500, default |
| DELETE | aggregateId |                                    | 204, 401, 403, 404, 500, default                    |

#### /%aggregates%/%subentities
<!--use aggregate name of the Bounded Context in plural, use subentity in plural-->

| Method | Parameters      | Request body | Responses                                      |
|--------|-----------------|--------------|------------------------------------------------|
| GET    | Search criteria |              | 200: subentity list, 401, 403, 500, default    |
| POST   |                 | subentity    | 201: Link to subentity, 401, 403, 500, default |

#### /%aggregates%/{aggregateId}/%subentities%/{subentityId}
<!--use aggregate name of the Bounded Context in plural, aggregateId usually a UUID, use subentity in plural, use subentityId-->

| Method | Parameters                | Request body                       | Responses                                           |
|--------|---------------------------|------------------------------------|-----------------------------------------------------|
| GET    | aggregateId, subentityId  |                                    | 200: subentity, 401, 403, 404, 500, default         |
| PUT    | aggregateId , subentityId | subentity                          | 200: Link to aggregate, 401, 403, 404, 500, default |
| PATCH  | aggregateId, subentityId  | subentity with optional properties | 200: Link to aggregate, 401, 403, 404, 500, default |
| DELETE | aggregateId, subentityId  |                                    | 204, 401, 403, 404, 500, default                    |

#### /%aggregates%/{aggregateId}/%valueobjects%
<!--use aggregate name of the Bounded Context in plural, aggregateId usually a UUID, use name of the value object in plural-->

| Method | Parameters  | Request body | Responses                                           |
|--------|-------------|--------------|-----------------------------------------------------|
| GET    | aggregateId |              | 200: value objects, 401, 403, 404, 500, default     |
| PUT    | aggregateId | value object | 201: Link to aggregate, 401, 403, 404, 500, default |