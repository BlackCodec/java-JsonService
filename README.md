# java-JsonService
A simple json string parser and builder in less then **400** lines (without comments).

Current release: 20230425.1300

---
## Description

The parse string method read the input string and process the regex in the order:

 - Numbers
 - Booleans
 - Nulls
 - Strings
 - Strings with escapes
 - Arrays
 - Objects


then return a JsonObject.

Is supposed that all input string are JsonObjects.

---
### Releases:

- **20230425.1300:**

    New implementation with regex.

- **20220528.1825:**

    This is the very first release.

---
### License: 

**GPL v3.0**, see file license.txt

