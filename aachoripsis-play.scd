(
q = q?();
q[\offset] = 3;
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
					(
						\instrument: \euroEnv,
						\out: q[\offset]?0 + i,
						\attackTime: a.matrix[i][j].density,
						\timingOffset: (d/density)*waitTime,
						\amp: density/(5*8),
						\legato: 0.01,
						\releaseTime: 0.2,
					).postln.play;
				});
			});
		});

		waitTime.wait;
	});
	"finished Aachorripsis".postln;
});
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


