# ODSS Platform Timeline back-end prototype #

## Build & Run ##

```sh
$ cp odssplatim.conf.template odssplatim.conf
$ vi odssplatim.conf  # to fill in the application configuration (MongoDB connection parameters)
$ sudo ln -s `pwd`/odssplatim.conf /etc/  # or edit web.xml to indicate a different config file location
$ ./sbt
> package            # to create war file
> container:start    # to run locally in development mode:
```

Open for example:  http://localhost:8181/tokens

To explore the REST API:
- Open http://petstore.swagger.wordnik.com/
- Enter `http://localhost:8181/api-docs` in the URL field

ODSS Platform Timeline Editor prototype: https://github.com/carueda/odssplatim-ui
