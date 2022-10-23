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

## Wood block sample

Xenakis is infamous for his use wood blocks in compositions - listen e.g. to *Metastaseis*.
Although it is not noted in *Formalized Music* it also appears in the score of *Aachorripsis*, yet it is not explained how it got there.
I assume that it was used in sparse sections to provide some orientation, therefore it is also played back here in sparse sections.

The sample is provided by *lostphosphene* via [freesound.org](https://freesound.org/people/lostphosphene/sounds/250386/).

## License

GPL-2.0
