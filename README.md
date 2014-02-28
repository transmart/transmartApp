transmartApp
============

tranSMART is a knowledge management platform that enables scientists to develop
and refine research hypotheses by investigating correlations between genetic and
phenotypic data, and assessing their analytical results in the context of
published literature and other work.

Installation
------------

Some pre-requisites are required in order to run tranSMART. For development,
a copy of [grails][1] is needed in order to run the application. For production
or evaluation purposes, it is sufficient to download a pre-build WAR file, for
instance a snapshot from the [The Hyveʼs][2] or [tranSMART Foundationʼs][3]
CI/build servers, for snapshots of The Hyveʼs or tranSMART Foundationʼs GitHub
repositories, respectively. In order to run the WAR, an application server is
required. The only supported one is [Tomcat][4], either from the 6.x or 7.x
line, though it will most likely work on others.

In addition, a PostgreSQL database installed with the proper schema and data is
required. As of this moment, The Hyveʼs development branches require this to be
propared with the [transmart-data][5] repository. This project also handles
the required configuration, running the Solr instances and, for development
purposes, running an R server and installing sample data.

For details on how to install the tranSMART Foundationʼs versions, refer to
[their wiki][6].


  [1]: http://grails.org/
  [2]: https://ci.ctmtrait.nl/
  [3]: https://ci.transmart.nl/
  [4]: http://tomcat.apache.org/
  [5]: https://github.com/thehyve/transmart-data
  [6]: https://wiki.transmartfoundation.org/
