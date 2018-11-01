# GraalVM Instructions

## Clone this repo

```
git clone -b iri-native https://github.com/Fabryprog/iri.git
```

## Install JDK 8

## Create JAR

```
mvn clean -DskipTests package
```

## Download and Install GraalVM (rc8)

## Create native image command

Execute follow command into **target** folder

```
native-image -H:+JNI -H:IncludeResources="(.*Version.properties$)|(.*librocksdbjni-linux64.so$)|(.*snapshotMainnet.*$)" -Djava.library.path=$(pwd)/../clibraries/ --report-unsupported-elements-at-runtime --rerun-class-initialization-at-runtime=zmq.util.Utils,com.iota.iri.network.Node --delay-class-initialization-to-runtime=org.rocksdb.RocksDB,org.rocksdb.RocksObject,io.undertow.protocols.alpn.ALPNManager,org.xnio.Xnio,org.xnio.Version -H:-UseServiceLoaderFeature -jar iri-1.5.5.jar
```

## Execute native-iri

```
./iri-1.5.5
```

I have follow error:

```
fabry@server:~/iri-native-fabry/target$ ./iri-1.5.5
Logging - property 'logging-level' set to: [INFO]
Exception in thread "main" com.oracle.svm.core.jdk.UnsupportedFeatureError: Unresolved element found
        at java.lang.Throwable.<init>(Throwable.java:265)
        at java.lang.Error.<init>(Error.java:70)
        at com.oracle.svm.core.jdk.UnsupportedFeatureError.<init>(UnsupportedFeatureError.java:31)
        at com.oracle.svm.core.jdk.Target_com_oracle_svm_core_util_VMError.unsupportedFeature(VMErrorSubstitutions.java:109)
        at com.oracle.svm.core.snippets.SnippetRuntime.unresolved(SnippetRuntime.java:206)
        at org.xnio.Xnio.doGetInstance(Xnio.java:247)
        at org.xnio.Xnio.getInstance(Xnio.java:187)
        at io.undertow.Undertow.start(Undertow.java:116)
        at com.iota.iri.service.API.init(API.java:148)
        at com.iota.iri.IRI$IRILauncher.main(IRI.java:79)
        at com.iota.iri.IRI.main(IRI.java:33)
        at com.oracle.svm.core.JavaMainWrapper.run(JavaMainWrapper.java:164)
```
