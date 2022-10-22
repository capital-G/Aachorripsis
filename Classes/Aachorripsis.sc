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
	var <lambda;
	var <rows;
	var <columns;
	var <matrix;
	var <curColumn;

	// private
	var scrollView;
	var curColumnText;
	var timeButtons;

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
		lambda = 0.6;
		rows = 7;
		columns = 28;
		curColumn = 1;

		window = Window.new(
			name: "Aachorripsis",
			bounds: Rect(0, 0, 1200, 400),
		);
		this.buildWindow();
	}

	prBuildScrollView {
		var scrollLayout = VLayout();
		var view = View();
		var timeLegend = HLayout();

		matrix = Aachorripsis(
			columns: columns,
			rows: rows,
			lambda: lambda,
		).matrix;

		timeButtons = [];
		timeLegend.add(StaticText().string_("t"));
		matrix.shape[1].do({|i|
			var button = Button().states_([
				[i+1, Color.white, Color.black],
				[i+1, Color.black, Color.yellow],
			]);
			timeButtons = timeButtons.add(button);
			timeLegend.add(button);
		});
		scrollLayout.add(timeLegend);

		// rows = tracks
		matrix.do({|rows, i|
			var rowView = HLayout();
			rowView.add(StaticText().string_(i+1));

			// columns = time segments
			rows.do({|column, j|
				var event = matrix[i][j];
				rowView.add(
					Button().states_([[event.density, Color.black, AachorripsisGUI.colors[event.type] ? Color.blue]])
				);
			});
			scrollLayout.add(rowView);
		});

		view.layout = scrollLayout;
		scrollView.canvas = view;
	}


	buildWindow {
		var mainHLayout = HLayout();
		var view = View();
		var leftVLayout = VLayout();
		var rightVLayout = VLayout();
		var buttonLegendLayout = HLayout();

		scrollView = ScrollView();

		this.prBuildScrollView;

		AachorripsisGUI.colors.keys.asList.sort.do({|i|
			buttonLegendLayout.add(Button().states_([[i, Color.black, AachorripsisGUI.colors[i]]]));
		});

		leftVLayout.add(scrollView);
		leftVLayout.add(buttonLegendLayout);

		mainHLayout.add(leftVLayout, stretch: 10);

		rightVLayout.add(Button().states_([["Generate", Color.white, Color.blue]]).action_({
			this.prBuildScrollView;
		}));

		rightVLayout.add(StaticText().string_("Lambda"));
		rightVLayout.add(NumberBox().value_(lambda).step_(0.05).scroll_step_(0.05).clipLo_(0.05).clipHi_(0.95).action_({|numb|
			lambda=numb.value;
		}));

		rightVLayout.add(StaticText().string_("Rows"));
		rightVLayout.add(NumberBox().value_(rows).step_(1).clipLo_(1).action_({|numb|
			rows=numb.value.asInteger;
		}));

		rightVLayout.add(StaticText().string_("Columns"));
		rightVLayout.add(NumberBox().value_(columns).step_(1).clipLo_(1).action_({|numb|
			columns=numb.value.asInteger;
		}));

		rightVLayout.add(Button().states_([
			["Play", Color.black, Color.green],
			["Stop", Color.black, Color.red]
		]).action_({|butt|
			if(butt.value==1, {
				"Start Aachorripsis Tdef".postln;
				Tdef(\aachorripsis).play;
			}, {
				"Stop Aachorripsis Tdef".postln;
				Tdef(\aachorripsis).stop;
			});
		}));
		rightVLayout.add(StaticText().string_("CurrentLocation"));
		curColumnText = StaticText().string_(curColumn);
		rightVLayout.add(curColumnText);

		mainHLayout.add(rightVLayout, stretch: 1);


		window.layout = mainHLayout;
		window.front;
	}

	curColumn_ {|newColumn|
		// make this async so we can update from a tdef clock
		{
			var activeButton;
			scrollView.visibleOrigin_(Point(x: (newColumn/columns)*scrollView.innerBounds.width - (scrollView.bounds.width/2), y: 0));
			curColumnText.string_(newColumn);
			timeButtons.do({|button|
				button.value = 0;
			});
			// only update if we are in range
			activeButton = timeButtons[newColumn-1];
			if(activeButton.notNil, {
				activeButton.value = 1;
			});
		}.defer;
		curColumn = newColumn;

	}
}
