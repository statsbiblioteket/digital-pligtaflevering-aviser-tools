# digital-pligtaflevering-aviser-tools
Digital Pligtaflevering af Aviser, autonomous preservation tools.

# VeraPDF workaround

There is as of 2016-06-23 no clear "use veraPDF as a library from Java" API,
and therefore the uberjar installed by the VeraPDF installer is included, and
needs to be manually installed with

    mvn install:install-file -Dfile=jars-not-in-maven-central/gui-0.16.1.jar -DgroupId=dk.statsbiblioteket.digital_pligtaflevering_aviser.verapdf -DartifactId=gui -Dversion=0.16.1  -Dpackaging=jar


before the project dependencies can be satisfied.


