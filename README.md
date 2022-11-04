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

`AachorripsisGUI` allows you to generate and examine a matrix (according to *Formalized Music*) as well as starting a [Tdef](https://doc.sccode.org/Classes/Tdef.html) with the name `\aachorripsis`.
Take a look into the provided `aachoripsis-play.scd` which also includes an example how to use the matrix events as gates or envelopes in an eurorack environment.

## Wood block sample

Xenakis is infamous for his use of wood blocks in compositions - listen e.g. to *Metastaseis*.
Although it is not noted in *Formalized Music* the wood block appears in parts with low or no density in the score of *Aachorripsis*.
I assume that it was used in sparse sections to provide some orientation, therefore it is also played back here in sparse sections as it also is a trademark of Xenakis.

Also the orchestral score released by *Bote & Bock* does not give any hints of the origins of the woodblock hits.

The included sample is provided by *lostphosphene* via [freesound.org](https://freesound.org/people/lostphosphene/sounds/250386/).

## Performance

The source code was written for a concert on 2022-10-25 in honor of Xenakis 100th birthday.
Instead of the 7 instrument groups it was performed with 2 modular synthesizer systems which received 7 gates to trigger musical events.
See the [website of the concert](https://capital-g.github.io/xenakis-workgroup/concert.html#aachorripsis-recalculated) for further details.

## License

GPL-2.0
