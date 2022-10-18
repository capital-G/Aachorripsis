Aachorripsis {
	var <columns; // number of time segments
	var <rows; // number of instruments/tracks
	var <lambda; // average that an event will happen
	var <maxEventOrder; // there are 0-events, 1-events, 2-events, ...
	var <matrix;
	var <p;

	*new {|columns=28, rows=7, lambda=0.6, maxEventOrder=5|
		^super.newCopyArgs(columns, rows, lambda, maxEventOrder).init;
	}

	init {
		p = ();
		// calculate probability for each event class (0-event, 1-event)
		(0..maxEventOrder).do({|i|
			var prob = Aachorripsis.poisson(lambda, i);
			var numCells = (prob * rows * columns).round.asInteger;
			// calculate how many n-events can happen in parallel
			// according to poisson
			var simEvents = ();
			(0..rows).do({|j|
				simEvents[j] = (Aachorripsis.poisson(lambda: numCells/columns, k: j) * columns).round.asInteger;
			});
			p[i] = (
				type: i,
				prob: prob,
				numCells: numCells,
				simEvents: simEvents;
			);
		});
		// @todo enforce sum=1 for stability in calculations ?
		// p = p.normalizeSum;
		["p", p].postln;

		// create empty 0 matrix with track/rows x time/columns
		matrix = rows.collect({columns.collect({0})});

		// start from most sim events as these can clug our matrix
		// we also can skip the 0 events
		(rows..1).do({|i|
			var q = p;
			// remove empty events
			q[0] = nil;
			q.pairsDo({|k, v|
				(v[\simEvents][i] ? 0).do({
					this.prInsertEvent(
						eventType: v[\type],
						simEvents: i,
					);
				});
			});
		});


	}

	*poisson{|lambda, k|
		^((lambda**k)/(k.factorial))*(((-1)*lambda).exp);
	}

	prInsertEvent { |eventType, simEvents|
		var columnSpace;
		var fittingIndices = [];
		var index;
		var nonActiveTracks = [];

		["insert", eventType, simEvents].postln;
		// count all 0s for each column
		columnSpace = columns.collect({|i|
			var columnValues = matrix.collect({|row|
				row[i]
			});
			// if column already contains the event type it is also = 0
			// as otherwise a 1-sim-single event could become a 2-sim-single event
			if(columnValues.includes(eventType), {
				0;
			}, {
				columnValues.select({|x| x==0}).size;
			});
		});
		["columnSpace", columnSpace].postln;

		columnSpace.do({|space, i|
			if(space>=simEvents, {
				fittingIndices = fittingIndices.add(i);
			});
		});
		["fittingIndices", fittingIndices].postln;

		if(fittingIndices.isEmpty, {
			"Could not insert % simultaneous % events into matrix - no space left".format(
				simEvents,
				eventType,
			).warn;
			^this;
		});

		// select one random column which still has place
		index = fittingIndices.choose;
		["index", index].postln;

		// look which rows/tracks are empty
		matrix.do({|row, j|
			if(row[index]==0, {
				nonActiveTracks = nonActiveTracks.add(j);
			});
		});
		["nonActiveTracks", nonActiveTracks].postln;

		// place the events into the matrix
		nonActiveTracks.scramble[(0..(simEvents-1))].do({|j|
			matrix[j][index] = eventType;
		});
	}
}


AachorripsisGUI {
	classvar <>colors;
	var window;

	*initClass {
		colors = (
			0: Color.white,
			1: Color.grey,
			2: Color.green,
			3: Color.cyan,
			4: Color.red,
			5: Color.magenta,
		);
	}

	*new {
		^super.newCopyArgs().init;
	}

	init {
		window = Window.new(
			name: "Aachorripsis",
			bounds: Rect(0, 0, 800, 400),
		);
	}

	buildWindow { |m|
		var scroll = ScrollView(
			parent: window,
			bounds: Rect(0, 0, 1400, 700),
		).hasBorder_(true);

		var comp = VLayoutView(
			parent: scroll,
			bounds: Rect(0, 0, 1400, 700),
		);


		m.do({|rows, i|
			var rowView = HLayoutView(parent: comp, bounds: Rect(0, 0, 500, 50));

			rows.do({|column, j|
				var v = m[i][j];
				Button(rowView).states_([["%".format(v), Color.black, AachorripsisGUI.colors[v] ? Color.blue]]);
			});
		});

		window.front;
	}
}
