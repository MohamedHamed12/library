# REST Error Model

All Patron Profile API failures use the same JSON structure.

```json
{
  "code": "HOLD_NOT_ALLOWED",
  "message": "The patron cannot place this book on hold.",
  "path": "/profiles/{patronId}/holds",
  "timestamp": "2026-07-18T07:00:00Z",
  "details": []
}
```

## Response fields

Field Meaning
`code` Stable machine-readable error code
`message` Safe client-facing description
`path` Request URI
`timestamp` Time the error response was created
`details` Field-level validation details

## Error mappings

Situation HTTP status Code
Request validation fails 400 `VALIDATION_FAILED`
Request body cannot be parsed 400 `MALFORMED_REQUEST`
UUID or path parameter is invalid 400 `INVALID_PATH_PARAMETER`
Patron does not exist 404 `PATRON_NOT_FOUND`
Book does not exist 404 `BOOK_NOT_FOUND`
Hold does not exist 404 `HOLD_NOT_FOUND`
Checkout does not exist 404 `CHECKOUT_NOT_FOUND`
Hold placement is rejected 409 `HOLD_NOT_ALLOWED`
Hold cancellation is rejected 409 `HOLD_CANCELLATION_NOT_ALLOWED`
Unexpected technical failure 500 `INTERNAL_ERROR`

## Security

API responses must not expose:

- stack traces;
- SQL statements;
- database details;
- internal Java class names;
- raw unexpected exception messages.

Unexpected exceptions are logged on the server and returned as a generic `INTERNAL_ERROR` response.