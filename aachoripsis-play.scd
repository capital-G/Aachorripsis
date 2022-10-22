(
q = q?();
q[\offset] = 1;
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
	10.do({|i|
		(
			\instrument: \euroEnv,
			\out: 5.rand + 1;
		).play;

		a.curColumn = i+1;
		1.5.wait;
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


