(
q = q?();
q[\offset] = 4;
q[\gates] = [7];
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
			), gate: \gate.kr(1.0),
		);
		sig = K2A.ar(sig);
		Out.ar(out, sig);
	}).add;

	s.meter;
	s.scope(numChannels: s.options.numOutputBusChannels);
	a = AachorripsisGUI();
});
)

(
Tdef(\aachorripsis, {|env|
	var waitTime = 5;
	a.matrix.shape[1].do({|columnNum, j|
		a.curColumn = columnNum+1;

		a.matrix.shape[0].do({|rowNum, i|
			var cell = a.matrix[i][j];
			var density = cell.density * 5;

			if(cell.type != 0, {
				(density).do({|d|
					var out = q[\offset]?0 + i;
					["test", q[\gates].includes(out), out].postln;
					(
						\instrument: if(q[\gates].includes(out), {\euroGate}, {\euroEnv}),
						\out: out,
						\attackTime: density.reciprocal,
						\timingOffset: (d/density)*waitTime,
						\amp: density/(5*8),
						\legato: density.reciprocal,
						\releaseTime: density.reciprocal * 4,
					).postln.play;
				});
			});
		});

		waitTime.wait;
	});
	"finished Aachorripsis".postln;
});
)
Tdef.clear;

(
(
instrument: \euroGate,
out: 6,
legato: 20.5
).play;
)




// test single
(
(
	\instrument: \euroEnv,
	\out: 1,
).play;
)

// test all
(
Tdef(\test, {
	(1..7).do({|i|
		(
			\instrument: \euroEnv,
			\out: i,
		).play;
		0.5.wait;
	});
}).play;
)


