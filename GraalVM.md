# GraalVM Instructions

# Create JAR

> mvn clean -DskipTests package

# Download GraalVM

# Native image

Execute follow command into target folder

> native-image -H:+JNI -H:IncludeResources=".*Version.properties$" -Djava.library.path=/home/fabry/iri-native/clibraries/ --report-unsupported-elements-at-runtime --rerun-class-initialization-at-runtime=zmq.util.Utils,com.iota.iri.network.Node --delay-class-initialization-to-runtime=org.rocksdb.RocksDB,org.rocksdb.RocksObject,io.undertow.protocols.alpn.ALPNManager,org.xnio.Xnio,org.xnio.Version -H:-UseServiceLoaderFeature -jar iri-1.5.5.jar