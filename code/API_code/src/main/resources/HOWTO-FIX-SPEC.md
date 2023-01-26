# How to fix openapi spec for generation

Need to be vigilant on oneOf and allOf

```
allOf:
    - $ref: ...
    - type: object
    properties:
    ...
```

Must be replaced by:

```
allOf:
    - $ref: ...
type: object
properties:
...
```

```
anyOf:
    - string
    - $ref => enum
```

Must be replaced by enum only or ref only (prefer enum)

```
$ref
```

Multi pattern Strings don't work as expected. See ipv6 format.
Comment second pattern

```
    Ipv6Addr:
      type: string
      format: ipv6
      allOf:
        - pattern: '^((:|(0?|([1-9a-f][0-9a-f]{0,3}))):)((0?|([1-9a-f][0-9a-f]{0,3})):){0,6}(:|(0?|([1-9a-f][0-9a-f]{0,3})))(\/(([0-9])|([0-9]{2})|(1[0-1][0-9])|(12[0-8])))?$'
        - pattern: '^((([^:]+:){7}([^:]+))|((([^:]+:)*[^:]+)?::(([^:]+:)*[^:]+)?))(\/.+)?$'
```

Changed into the following (todo combine two patterns into one)

```
    Ipv6Addr:
      type: string
      format: ipv6
      pattern: '^((:|(0?|([1-9a-f][0-9a-f]{0,3}))):)((0?|([1-9a-f][0-9a-f]{0,3})):){0,6}(:|(0?|([1-9a-f][0-9a-f]{0,3})))(\/(([0-9])|([0-9]{2})|(1[0-1][0-9])|(12[0-8])))?$'
      #pattern: '^((([^:]+:){7}([^:]+))|((([^:]+:)*[^:]+)?::(([^:]+:)*[^:]+)?))(\/.+)?$'
```