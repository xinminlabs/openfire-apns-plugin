Building
---
Pre-requisite: Use Maven 2.2.1
---
1. Install maven-openfire-plugin from https://github.com/srt/maven-openfire-plugin
2. Obtain a copy of `openfire.jar` and install it into maven. There are serveral ways you can do it, here are two of them:
  * Option 1. Download source code of the version of openfire you need at http://www.igniterealtime.org/downloads/source.jsp

    Then set `$OPENFIRE_PATH` and `$OPENFIRE_VERSION` environment variables according to source code path and version and run:
    ```bash
    cd $OPENFIRE_PATH/build
    ant
    mvn install:install-file -DgroupId=org.igniterealtime.openfire -DartifactId=openfire -Dversion=$OPENFIRE_VERSION -Dpackaging=jar -DgeneratePom=true -Dfile=$OPENFIRE_PATH/target/openfire/lib/openfire.jar
    ```
  * Option 2. If you installed openfire using `openfire*.deb` package on ubuntu, you can find `openfire.jar` inside `/usr/share/openfire/lib`:

    Because this directory is read protected from regular users, you would first have to copy it to somewhere in your home directory and change it's read permissions using sudo, e.g by running:

    ```bash
    sudo cp /usr/share/openfire/lib/openfire.jar .
    sudo chmod a+r openfire.jar
    ```
    
    Now you can install it into maven by running:
    
    ```bash
    mvn install:install-file -DgroupId=org.igniterealtime.openfire -DartifactId=openfire -Dversion=$OPENFIRE_VERSION -Dpackaging=jar -DgeneratePom=true -Dfile=openfire.jar
    ```
3. Inside openfire-apns-plugin directory run: `mvn clean install`
4. If the build was successfull `openfire-apns.jar` should appear in `target` directory.
