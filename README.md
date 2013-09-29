
Download openfire source

    cd $OPENFIRE_PATH/build
    ant
    mvn install:install-file -DgroupId=org.igniterealtime.openfire -DartifactId=openfire -Dversion=$OPENFIRE_VERSION -Dpackaging=jar -DgeneratePom=true -Dfile=$OPENFIRE_PATH/target/openfire/lib/openfire.jar
