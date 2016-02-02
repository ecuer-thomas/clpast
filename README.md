# clopaste


```shell
Availables commands:
====================




POST /       => upload a document. Returns url in plain text.


GET /up/:id  => download a document





$ ls -lh public/ | curl -XPOST -H "Content-type: application/octet-stream"  --data-binary @- http://localhost:8080

$ cat somefile | curl -XPOST -H "Content-type: application/octet-stream"  --data-binary @- http://localhost:8080


$ alias internet="curl -XPOST -H 'Content-type: application/octet-stream'  --data-binary @- http://localhost:8080"

$ cat somefile | internet

[...]
```

## Installation

* Clone this repo
* Configure resources/log4j.properties
* lein should do the rest
  * `lein run`

## Usage

### configuration

see `src/clopaste/conf.clj`

### devel

```
$ lein run
[...]
```

### java

```
$ java -jar clopaste-0.1.0-standalone.jar
```

