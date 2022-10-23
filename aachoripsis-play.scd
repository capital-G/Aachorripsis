(
q = q?();
q[\offset] = 1;
q[\gates] = [4,5,6,7];
ServerOptions.outDevices;
s.options.outDevice_("Studio 1824c");
s.options.numOutputBusChannels_(8);
s.reboot;

s.waitForBoot({
	SynthDef(\euroEnv, {|out|
		var sig = EnvGen.kr(
			envelope: Env.adsr(
				attackTime: \attackTime.kr(0.01),
				decayTime: \decayTime.kr(0.3),
				sustainLevel: \sustainLevel.kr(0.5),
				releaseTime: \releaseTime.kr(1.0),
				curve: \curve.kr(-4.0)
			),
			gate: \gate.kr(1.0),
			doneAction: Done.freeSelf,
		) * \amp.kr(1.0);
		sig = K2A.ar(sig);
		Out.ar(out, sig);
	}).add;

	SynthDef(\euroGate, {|out|
		var sig = EnvGen.kr(
			envelope: Env.adsr(
				attackTime: 0.0,
				decayTime: 0.0,
				sustainLevel: 1.0,
				releaseTime: 0.0
		), gate: \gate.kr(1.0), doneAction: Done.freeSelf);
		sig = K2A.ar(sig);
		Out.ar(out, sig);
	}).add;

	s.meter;
	s.scope(numChannels: s.options.numOutputBusChannels);
	a = AachorripsisGUI();
});
t = TempoClock.new(1.0, queueSize: 4096*16);
)

(
Tdef(\aachorripsis, {|env|
	var waitTime = 15;
	var multiplier = 15;
	a.matrix.shape[1].do({|columnNum, j|
		a.curColumn = columnNum+1;

		a.matrix.shape[0].do({|rowNum, i|
			var cell = a.matrix[i][j];
			var density = cell.density * multiplier;

			if(cell.type != 0, {
				(density).do({|d|
					var out = q[\offset]?0 + i;
					["test", q[\gates].includes(out), out].postln;
					(
						\instrument: if(q[\gates].includes(out), {\euroGate}, {\euroEnv}),
						\out: out,
						\attackTime: density.reciprocal,
						\timingOffset: (d/density)*waitTime,
						\amp: density/(multiplier),
						\legato: density.reciprocal,
						\releaseTime: density.reciprocal * multiplier * 2,
					).postln.play;
				});
			});
		});

		waitTime.wait;
	});
	"finished Aachorripsis".postln;
}).clock_(t);
)
Tdef.clear;

(
// gate test
(
instrument: \euroGate,
out: 7,
legato: 0.5
).play;
)




// test single
(
(
	\instrument: \euroEnv,
	\out: 4,
\releasetime: 4.0,
   \legato: 1.0,
\curve: -1.0,
\amp: 1.0,
).play;
)

// test all
(
Tdef(\test, {
	inf.do({|i|
		(
			\instrument: \euroGate,
			\out: [4,7],
			\legato: 0.1,
			\attackTime: 0.0,
			\releaseTime: 0.01
		).play;
		1.2.wait;
	});
}).stop;
)


Tdef.clear;