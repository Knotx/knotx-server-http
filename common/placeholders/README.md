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

    ```javascript
    {
      "thumbnail" : {
        "extension" : "png"
      }
    }
    ```

`PlaceholdersResolver` provides two methods for performing substitution: `resolve` which performs a direct substitution, and `resolveAndEncode` which also performs encoding.

When calling `resolveAndEncode`, all placeholders are substituted with encoded values according to the RFC standard. However, there are two exceptions:
- Space character is substituted by `%20` instead of `+`.
- Slash character `/` remains as it is.
