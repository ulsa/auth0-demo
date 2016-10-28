# auth0-demo

Simple Clojure authentication example for [Auth0][], using [Buddy][].

[Auth0]: https://auth0.com
[Buddy]: https://github.com/funcool/buddy

## Prerequisites

You will need [Leiningen][] 2.0.0 or above installed.

[leiningen]: https://github.com/technomancy/leiningen

You need to create an account on [Auth0][], and a client to use for
authentication. The client should have Google authentication enabled.

## Running

To build a standalone jar, run:

    lein uberjar

Run it like this:

    $ java [-Doption=value ...] -jar auth0-demo.standalone.jar
    $ [OPTION=value ...] java -jar auth0-demo.standalone.jar

or using Leiningen (requires a `profiles.clj` with options, more about that
below):

    $ lein ring server

or without opening a browser window:

    $ lein ring server-headless

## Options

Some options are only possible to set using environment variables or Java system
variables.
The options in the table below are shown as Clojure keywords, eg `:auth-domain`.
Those are used in the `profiles.clj` file, which is useful when running the
system using Leiningen, running a REPL using Leiningen, or running tests using
Leiningen. The file should looks like this:

    {:dev-overrides
      {:env {:auth-domain "cljdemo.eu.auth0.com"
             :auth-client-id "someid"
             :auth-client-secret "thesecretkey"}}
     ...}

The `:auth-return-to-uri` and `:auth-callback-uri` keys are optional, but can be specified like this:

    {:dev-overrides
      {:env {:auth-domain "cljdemo.eu.auth0.com"
             :auth-client-id "someid"
             :auth-client-secret "thesecretkey"
             :auth-callback-uri "http://localhost:3000/callback"
             :auth-return-to-uri "http://localhost:3000/login"}}}

If Java system variables are used, remove the colon and replace dashes with dots,
as in `-Dauth.domain`:

    $ java -Dauth.domain=cljdemo.eu.auth0.com \
    -Dauth.client.id=someid \
    -Dauth.client.secret='thesecretkey' \
    -jar auth0demo.standalone.jar

If environment variables are used, remove the colon, replace dashes with underscore,
and change to uppercase, as in `AUTH_DOMAIN`:

    $ AUTH_DOMAIN=cljdemo.eu.auth0.com \
    AUTH_CLIENT_ID=someid \
    AUTH_CLIENT_SECRET='thesecretkey' ... \
    java -jar auth0-demo.standalone.jar

The web server port can also be changed:

    $ PORT=3333 ... java -jar auth0-demo.standalone.jar
    $ java -Dport=4444 ... -jar auth0-demo.standalone.jar

<p>

<table>
  <tr>
    <th>keyword</th>
    <th>env var</th>
    <th>default</th>
    <th>required</th>
    <th>description</th>
  </tr>
  <tr>
    <td>:auth-domain</td>
    <td>AUTH_DOMAIN</td>
    <td></td>
    <td>yes</td>
    <td>Auth0 client domain, for example: cljdemo.eu.auth0.com</td>
  </tr>
  <tr>
    <td>:auth-client-id</td>
    <td>AUTH_CLIENT_ID</td>
    <td></td>
    <td>yes</td>
    <td>Auth0 client id.</td>
  </tr>
  <tr>
    <td>:auth-client-secret</td>
    <td>AUTH_CLIENT_SECRET</td>
    <td></td>
    <td>yes</td>
    <td>Auth0 client secret.</td>
  </tr>
  <tr>
    <td>:auth-callback-uri</td>
    <td>AUTH_CALLBACK_URI</td>
    <td>http://localhost:&lt;PORT&gt;/callback</td>
    <td>no</td>
    <td>The URI which will be called after successful authentication.</td>
  </tr>
  <tr>
    <td>:auth-return-to-uri</td>
    <td>AUTH_RETURN_TO_URI</td>
    <td>http://localhost:&lt;PORT&gt;/login</td>
    <td>no</td>
    <td>The URI to redirect to after logout.</td>
  </tr>
  <tr>
    <td>:port</td>
    <td>PORT</td>
    <td>3000</td>
    <td>no</td>
    <td>Web server port. Note that the 'Allowed Callback URLs' and 'Allowed Logout URLs' on Auth0 need to include this port.</td>
  </tr>
</table>

The reason the profiles should be named `*-overrides` is that in order to merge
profiles correctly, they are defined like this in `project.clj`:

```clojure
  :profiles {:dev         [:dev-common :dev-overrides]
             :dev-common  {:dependencies [[javax.servlet/servlet-api "2.5"]
                                          [ring/ring-mock "0.3.0"]]}}
```

## License

Copyright © 2016 Ulrik Sandberg
