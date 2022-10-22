# Aachorripsis

![GUI Screenshot](screenshot.png)

Implementation of Iannis Xenakis composition *Aachorripsis* in SuperCollider based on his notes in *Formalized Music*.

## Installation

Install this extension as a [quark](https://doc.sccode.org/Guides/UsingQuarks.html) via

```supercollider
Quarks.install("https://github.com/capital-G/Aachorripsis.git");
// restart sclang
thisProcess.recompile;
```

You can update it via

```supercollider
Quarks.update("Aachorripsis");
thisProcess.recompile;
```

## Usage

Take a look into the provided `aachoripsis-play.scd` on how to run this.
This includes an example how the generated events are used as gates or envelopes in an eurorack environment.

## License

GPL-2.0
