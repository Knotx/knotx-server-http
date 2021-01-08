# Knot.x Server Common Placeholders
This module contains client request placeholders substitutors.

## Available Request placeholders support
`PlaceholdersResolver` enables to replace any of the following placeholders in the given `ClientRequest`:

- `{header.x}` - is the client requests header value where x is the header name
- `{param.x}` - is the client requests query parameter value. For x = q from `/a/b/c.html?q=knot` it will produce knot
- `{uri.path}` - is the client requests sling path. From `/a/b/c.sel.it.html/suffix.html?query` it will produce `/a/b/c.sel.it.html/suffix.html`
- `{uri.pathpart[x]}` - is the client requests xth sling path part. For `x = 2` from `/a/b/c.sel.it.html/suffix.html?query` it will produce `c.sel.it.html`
- `{uri.extension}` - is the client requests sling extension. From `/a/b/c.sel.it.html/suffix.xml?query` it will produce xml
- `{slingUri.path}` - is the client requests sling path. From `/a/b/c.sel.it.html/suffix.html?query` it will produce `/a/b/c`
- `{slingUri.pathpart[x]}` - is the client requests xth sling path part. For `x = 1` from `/a/b/c.sel.it.html/suffix.html?query` it will produce `b`
- `{slingUri.selectorstring}` - is the client requests sling selector string. From `/a/b/c.sel.it.html/suffix.html?query` it will produce `sel.it`
- `{slingUri.selector[x]}` - is the client requests xth sling selector. For `x = 1` from `/a/b/c.sel.it.html/suffix.html?query` it will produce `it`
- `{slingUri.extension}` - is the client requests sling extension. From `/a/b/c.sel.it.html/suffix.xml?query` it will produce `html`
- `{slingUri.suffix}` - is the client requests sling suffix. From `/a/b/c.sel.it.html/suffix.html?query` it will produce `/suffix.html`
- `{payload.thumbnail.extension}` - is the json value with custom prefix  - for instance `payload` in this example.

    ```json
    {
      "thumbnail" : {
        "extension" : "png"
      }
    }
    ```
  
Additional sources can also be included, see for example usage in `knotx-fragments` repository.

## Configuration
`PlaceholdersResolver` is configurable:
- it is possible to enable/disable clearing unmatched placeholders to an empty string
- it is possible to enable/disable encoding

When clearing unmatched placeholders is enabled, all placeholders that are not matched to a source, e.g. `{not.existent}` are replaced by an empty string.
When this option is disabled, these placeholders are left as-is.
Please note that placeholders that match a source but are not defined within it (like `{params.not-existent}`) are always replaced with an empty string. 

When encoding is enabled, all placeholders are substituted with encoded values according to the RFC standard. However, there are two exceptions:
- Space character is substituted by `%20` instead of `+`.
- Slash character `/` remains as it is.

## Resolving JSON objects

`JsonResolver` is an utility class for resolving placeholders in JSON objects.
It traverses the JSON structure (objects and arrays) in order to replace nested placeholders in string values.
Requires configured `PlaceholdersResolver` to work.
