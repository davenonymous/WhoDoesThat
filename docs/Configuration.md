# Data Source Configuration

Besides the usual Mod configuration that can be accessed through the Mod Configuration Menu,
there is another configuration source that is used by WhoDoesThat to determine what's interesting
about a mod.

## Location

These "Description" configurations are stored in the `config/whodoesthat.d/` directory of your
instance. All yaml files in this directory are loaded, parsed and merged into a single
configuration that is used by WhoDoesThat.

## Source Types

There are currently five types of information sources that can be utilized in these files:

1. [Events](#events)
2. [Inheritance](#inheritance)
3. [Mixins](#mixins)
4. [Annotations](#annotations)
5. [Files](#files)

All of these are pretty straight forward to configure and are described in more detail below.
Their `description` fields are used to generate the Summary Categories and should be as concise
but accurate as possible.

### Events

Many of the systems in Minecraft are event driven. This means that when something happens, an event
is fired that can be listened to by other parts of the system. Tracking what events are listened to
by a mod can give a good indication of what the mod is doing.

Event entries need two fields to be defined: `id` and `description`. The `id` field is the fully
qualified class name of the event that should be described. The `description` field is a
human-readable
description of what the event is about.

There is no official list of events that can be listened to in Minecraft/NeoForge, so the best
way to find out what events are available is to look at the source of neoforge. All classes
inheriting from `net.neoforged.neoforge.event.Event` are considered events.

Example of an event source:
```yaml
events:
    -   id: net.neoforged.neoforge.event.AnvilUpdateEvent
        description: Custom Anvil Recipes
```

### Inheritance

When a mod tries to change the behavior of a custom block or item, it usually does so by extending
the `Block` or `Item` classes and overriding some of their methods. Tracking what classes are being
extended can give a good indication of what the mod is doing.

Inheritance descriptions are equally simple as event descriptions. They need two fields to be
defined: `id` and `description`. The `id` field is the fully qualified class name of the class that
is being extended. The `description` field is a human-readable description of what the class is
about.

Example of an inheritance source:
```yaml
inheritance:
    -   id: net.minecraft.world.level.block.Block
        description: Custom Blocks
```

### Mixins

Mixins are a way to inject code into existing classes. This is a powerful tool that can be used to
completely modify the behavior of a core Minecraft class. It is also very invasive and can break
other mods that rely on the original behavior of the class.

Figuring out which Minecraft classes are being mixed into can give a vague indication of what
the mod is up to. In the end the mod could just add logging to the class, but it could also be
completely changing the behavior of the class. Interpreting these descriptions is a bit more
advanced than the other sources and can lead to false positives.

The same as with the previous sources, mixins need two fields to be defined: `id` and `description`.
The `id` field is the fully qualified class name of the class that is being mixed into.

```yaml
mixins:
    -   id: net.minecraft.client.gui.screens.Screen
        description: Modifies GUI Screens (invasive)
```

### Annotations

Annotations are a way to add metadata to classes, methods or fields. They can for example be
used to mark classes as being part of a certain system or to mark methods as being event
handlers. Annotations are often used by mods to mark classes as Plugin entry points.

Annotations need three fields to be defined: `id`, `description` and `type`. The `id` field is the
fully qualified class name of the annotation that is being used. The `description` field is a
human-readable description of what the annotation is about. The `type` field is the type of the
element that is being annotated. This is usually either `class`, `method` or `field`.

Other valid types are: `parameter`, `constructor`, `local_variable`, `annotation_type`, `package`,
`type_parameter`, `type_use`, `module`, and `record_component` (See java.lang.annotation.
ElementType).

If you are also interested in the parameters of the annotated element, you can add a `params` field
with a list of parameter names. Their values will be included in the full output.

```yaml
annotations:
    -   id: net.minecraft.gametest.framework.GameTest
        description: Automatic Testing
        type: method
        params:
            - template
```

### Files

While the previous sources are all based on the code of a mod, the file source is based on the
contents of the jar file the mod is distributed in. Since Minecraft is a very data-driven game,
this is a very powerful source of information.

That being said Who Does That is currently(tm) only interested in the file paths, not the contents.
This means that more detailed inspection of the files is not possible at the moment. Still, the
paths alone give a very good indication of custom content that is being added by a mod.

Instead of using fully regex compatible patterns, the file source uses a simplified pattern syntax
called "glob". This syntax is similar to regex, but much simpler. More details can be found on the
wikipedia page for [glob (programming)](https://en.wikipedia.org/wiki/Glob_(programming)).

```yaml
files:
    -   glob: "assets/*/textures/block/*.png"
        description: Custom Block Textures
    -   glob: "data/*/tags/**/*.json"
        description: Custom Tag Assignments
```
