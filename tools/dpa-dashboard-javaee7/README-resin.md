Resin 4.0.55 Open Source edition gives an error when deployed.

```sh
/home/tra/Hentet/resin-4.0.55/conf/app-default.xml:55: @Named('dpa') is
a duplicate name for
  ProducesMethodBean[String, IngesterModule.provideDestinationPath(), {@javax.inject.Named(value=dpa.putfile.destinationpath), @Default(), @Any()}, name=dpa.putfile.destinationpath]
  ProducesMethodBean[String, IngesterModule.provideDeliveriesFolder(), {@javax.inject.Named(value=dpa.deliveries.folder), @Default(), @Any()}, name=dpa.deliveries.folder]
```


Question asked at https://stackoverflow.com/q/48817314/53897

/tra 2018-02-16
