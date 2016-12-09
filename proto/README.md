## Building from the protobuf file

Download the latest release here: https://github.com/google/protobuf/releases

Unzip it, change to that directory, then run the following to install it on your machine:

```
$ sudo ./configure
$ sudo make
$ sudo make check
$ sudo make install
$ protoc --version
```

Once done, installing, generate the `.java` file with:

```
$ protoc launcher.proto --java_out=./
```
