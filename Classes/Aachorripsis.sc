AachorripsisCell {
	// dataclass
	var <type;
	var <density;

	*new {|type=0, density=0|
		^super.newCopyArgs(type, density);
	}

	printOn { | stream |
		stream << type << " (" << density << ")";
	}
}

Aachorripsis {
	var <columns; // number of time segments
	var <rows; // number of instruments/tracks
	var <lambda; // average that an event will happen
	var singleEventsPerSec;
	// xenakis uses 1-events... but the used density for a cell is
	// not directly equal to this but instead it is normal distributed
	// this is not documented in formalized music but if you compare the
	// numbers in the score it checks out
	var <distributeDensity;
	// there are 0-events, 1-events, 2-events - you may want to limit this?
	var <maxEventOrder;
	var <matrix;
	var <p;

	*new {|columns=28, rows=7, lambda=0.6, singleEventsPerSec=2, distributeDensity=true, maxEventOrder=5|
		^super.newCopyArgs(columns, rows, lambda, singleEventsPerSec, distributeDensity, maxEventOrder).init;
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

		// create empty matrix with track/rows x time/columns
		// we use a custom dataclass here as the matrix consists of two values
		// the event type and the density
		matrix = rows.collect({columns.collect({AachorripsisCell()})});

		// start from most sim events as these can clug our matrix
		// we also can skip the 0 events
		(rows..1).do({|i|
			var q = p;

			// remove empty events
			q[0] = nil;

			q.pairsDo({|eventType, props|
				(props[\simEvents][i] ? 0).do({
					this.prInsertEvent(
						eventType: props[\type],
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
			var columnEventTypes = matrix.collect({|row|
				row[i].type;
			});

			// if column already contains the event type it is also = 0
			// as otherwise a 1-sim-single event could become a 2-sim-single event
			if(columnEventTypes.includes(eventType), {
				0;
			}, {
				columnEventTypes.select({|x| x==0}).size;
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
			if(row[index].type==0, {
				nonActiveTracks = nonActiveTracks.add(j);
			});
		});
		["nonActiveTracks", nonActiveTracks].postln;

		// place the events into the matrix
		nonActiveTracks.scramble[(0..(simEvents-1))].do({|j|
			matrix[j][index] = AachorripsisCell(
				type: eventType,
				density: if(distributeDensity, {
					(eventType*singleEventsPerSec).gauss(1.0).max(0.25).round(0.5);
				}, {
					eventType * singleEventsPerSec;
				}),
			);
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
			bounds: Rect(0, 0, 1200, 400),
		);
	}


	buildWindow { |m|
		var mainHLayout = HLayout();

		var scroll = ScrollView();
		var view = View();
		var leftVLayout = VLayout();
		var rightVLayout = VLayout();
		var buttonLegendLayout = HLayout();

		// stuff that gets into scroll view
		var scrollLayout = VLayout();

		// time legend
		var timeLegend = HLayout();
		timeLegend.add(StaticText().string_("t"));
		m.shape[1].do({|i|
			timeLegend.add(Button().states_([[i, Color.white, Color.black]]));
		});
		scrollLayout.add(timeLegend);

		// rows = tracks
		m.do({|rows, i|
			var rowView = HLayout();
			rowView.add(StaticText().string_(i));

			// columns = time segments
			rows.do({|column, j|
				var event = m[i][j];
				rowView.add(
					Button().states_([[event.density, Color.black, AachorripsisGUI.colors[event.type] ? Color.blue]])
				);
			});
			scrollLayout.add(rowView);
		});

		view.layout = scrollLayout;
		scroll.canvas = view;

		AachorripsisGUI.colors.keys.asList.sort.do({|i|
			buttonLegendLayout.add(Button().states_([[i, Color.black, AachorripsisGUI.colors[i]]]));
		});

		leftVLayout.add(scroll);
		leftVLayout.add(buttonLegendLayout);
		// vlayout.add(Button().states_([["Bab"]]));

		mainHLayout.add(leftVLayout, stretch: 10);
		// scroll.front;

		rightVLayout.add(Button().states_([["Generate", Color.white, Color.blue]]));

		rightVLayout.add(StaticText().string_("Lambda"));
		rightVLayout.add(NumberBox().value_(0.6).step_(0.05).scroll_step_(0.05).clipLo_(0.05).clipHi_(0.95));
		rightVLayout.add(StaticText().string_("Rows"));
		rightVLayout.add(NumberBox().value_(7).step_(1).clipLo_(1));
		rightVLayout.add(StaticText().string_("Columns"));
		rightVLayout.add(NumberBox().value_(27).step_(1).clipLo_(1));

		rightVLayout.add(Button().states_([
			["Play", Color.black, Color.green],
			["Stop", Color.black, Color.red]
		]));
		rightVLayout.add(StaticText().string_("CurrentLocation"));
		rightVLayout.add(StaticText().string_(1));

		mainHLayout.add(rightVLayout, stretch: 2);


		window.layout = mainHLayout;
		window.front;

	}
}
