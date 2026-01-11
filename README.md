# FGui (Server Gui)

> What does the "F" stand for? _**F**abric? (Neo)**F**orge? **F**un? Who knows..._

It's a small, jij-able library that allows creation of server side guis.

- This project is a fork of [sgui](https://github.com/Patbox/sgui), adapted
  to work with both Fabric and NeoForge alongside continuous support for multiple
  Minecraft versions.

### Adding to your Project

Add it to your dependencies like this:

### Fabric

```kotlin
dependencies {
    modImplementation(include("de.clickism:fgui-fabric:"))
}
```

### NeoForge

```kotlin
dependencies {
    jarJar(implementation("de.clickism:fgui-neoforge:"))
}
```

After that you are ready to go! You can use `SimpleGUI` and other classes directly for simple ones or extend
them for more complex guis.
