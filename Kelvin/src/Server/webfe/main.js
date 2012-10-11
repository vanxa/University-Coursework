/* Globals */

var MODE = 'graph';
var draggedNode;

var heatmap;

var showAlertErrorMsgs = false;
var currentlySelectedPlaceMap = 0;
var unassignedBoards = {};
var boardsOnMap = {};
var placerListGroup = 0;

var _graphdatatype = "temp";

var heatmapPageSelectedGroup = 0;
var graphPageSelectedGroup = 0;


var colPos = 0;
var okColors = [
	"#ff0033",
	"#336600",
	"#6600FF",
	"#333366",
	"#FF3300",
	"#339999",
	"#993300",
	"#00CC00",
	"#3366CC",
	"#660000",
	"#CC6600",
	"#FF6600" ];

/* Globals end */

/* Image preload */
var at3jpg;

function errorMsg( msg )
{
	if( typeof(console) != 'undefined' && w && h )
		console.log( msg );
	else
		if( showAlertErrorMsgs )
			alert( msg );
}

function changeRangeSlider( _val )
{
	maxDistTime = Math.floor(_val);
	var tContainer = document.getElementById('tContainer');
	
	//Clear tContainer
	while( tContainer.firstChild )
		tContainer.removeChild( tContainer.firstChild )
		
	var innerTxt = document.createTextNode( 'Showing up to previous ' + secsToStr( _val ) );
	
	tContainer.appendChild( innerTxt );
}

function secsToStr( _secs )
{
	var pSecs = _secs;
	var pMins = _secs/60;
	var pHours = _secs/(60*60);
	var pDays = _secs/(60*60*24);
	var pMonths = _secs/(60*60*24*30);
	var pYears = _secs/(60*60*24*365);
	
	var strTime;
	
	if( pYears > 0.8 )
		strTime = Math.round(pYears) + " year" + ( Math.round(pYears) > 1 ? 's' : '' );
	else if ( pMonths > 0.8 )
		strTime = Math.round(pMonths) + " month" + ( Math.round(pMonths) > 1 ? 's' : '' );
	else if ( pDays > 0.8 )
		strTime = Math.round(pDays) + " day" + ( Math.round(pDays) > 1 ? 's' : '' );
	else if ( pHours > 0.8 )
		strTime = Math.round(pHours) + " hour" + ( Math.round(pHours) > 1 ? 's' : '' );
	else if ( pMins > 0.8 )
		strTime = Math.round(pMins) + " minute" + ( Math.round(pMins) > 1 ? 's' : '' );
	else
		strTime = Math.round(pSecs) + " second" + ( Math.round(pSecs) > 1 ? 's' : '' );
		
	return strTime;
}

function makeTime( num )
{
	// Slider values
	var farNum = 1;
	var nearNum = 200;

	//0.5 Year in seconds
	var far = 60*60*24*180;
	
	//1 minute in seconds
	var near = 60;
	
	var numDelta = Math.abs(nearNum - farNum);
	
	var numInv = nearNum - num;
	
	var timeDelta = Math.abs(far - near);
	
	tVal = timeDelta * Math.pow((numInv/numDelta), 4);

	//var offset = Math.pow( numInv , 1.5 )/nearNum;
	
	//var tVal = timeDelta*(offset/numDelta);
	
	changeRangeSlider( tVal );
	
}

function addRangeSelector( el )
{
	rootEl = document.getElementById( el );
	if( !rootEl )
		errorMsg( 'Bad element id given for range selector' );
	
	var selector = document.createElement('div');
	selector.setAttribute('id','rangeSelector');
	rootEl.appendChild( selector );
	
	//Draggable handle
	var rangeSel = document.createElement('input');
	rangeSel.setAttribute('type','range');
	rangeSel.setAttribute('min','1');
	rangeSel.setAttribute('max','200');
	rangeSel.value = 185;
	
	rangeSel.style.width = '400px';
	rangeSel.addEventListener('change', function() { makeTime( this.value ) } );
	rangeSel.addEventListener('mouseup' , refresh );
	
	
	selector.appendChild( rangeSel );
	
	selector.appendChild( document.createElement( 'br' ) );
	
	var tContainer = document.createElement('span');
	tContainer.setAttribute('id','tContainer');
	
	selector.appendChild( tContainer );
}

function dragEnterListener(ev)
{
	ev.preventDefault();
	ev.dataTransfer.dropEffect = 'Move';
	return true;
	//return ev.dataTransfer.types.contains('URL');
}

function dragOverListener(ev)
{
	ev.preventDefault();
}

function dropListener(ev)
{
	ev.preventDefault();
	
	x = (ev.pageX - this.offsetLeft - 30);
	y = (ev.pageY - this.offsetTop - 30);
	
	addBoardToGroup( draggedNode.id , currentlySelectedPlaceMap , x , y , "refreshPlacerMap()" )

	draggedNode.style.color = "#aaaaaa";
	draggedNode = null;
 
}

function drawPlacerMap()
{
	for( node in boardsOnMap )
	{
		var im = new Image();
		im.src = "sensor.png";
		
		im._x = boardsOnMap[node][0];
		im._y = boardsOnMap[node][1];
		
		im.onload = function()
		{
			document.getElementById('placerDz').getContext('2d').drawImage( this , this._x , this._y )
		};
		
	}
}

function refreshPlacerMap()
{
	var cvas = document.getElementById('placerDz');
	var contxt = cvas.getContext('2d');
	//Clear
	contxt.clearRect( 0 , 0 , cvas.width , cvas.height );
	
	if( currentlySelectedPlaceMap > 0 )
	{
		getPlacerMapNodes( currentlySelectedPlaceMap , "drawPlacerMap()" );
		//Draw at3 map
		var im = new Image();
		im.src = "maps/at3.jpg";
		
		im.onload = function()
		{
			document.getElementById('placerDz').getContext('2d').drawImage( this , 0 , 0 , 750 , 300 );
		}
	}
}

function dragStartListener( evt )
{
	draggedNode = this;
	//alert( draggedNode.getAttribute('id') );
}

function selectLeftGroupSelect( _val )
{
	placerListGroup = _val;
	listBoards(_val,'nodesToPlace');
}

function graphDropSelectGroup( _val )
{
	graphPageSelectedGroup = _val;
	listBoardsCheckable(_val,'graphNodeSelect');
}

function heatmapSelectMap( _val )
{
	heatmapPageSelectedGroup = _val;
	getBoardPositions( _val );
}

function setGroups( _groups , _chooser )
{
	var selector = document.getElementById(_chooser);

	if( !selector )
	{
		selector = document.createElement('select');
		selector.setAttribute('id','groupSelector');
		document.getElementById('dropdownArea').appendChild( selector );
	} else {
		while( selector.firstChild )
			selector.removeChild( selector.firstChild );
	}
		if( _chooser == "heatmapGroupSelector" )
			selector.addEventListener( 'change' , function() { heatmapSelectMap( this.value ) } , true );
	
		if( selector.getAttribute('id') == 'gMapSelect' )
			selector.addEventListener( 'change' , function() { graphDropSelectGroup( this.value ) ; clearGraphVars() } , true );
	
		if( _chooser == "leftGroupSelect" )
			selector.addEventListener( 'change' , function() { selectLeftGroupSelect( this.value ) } , true );
	
		if( _chooser == "rightGroupSelect" )
			selector.addEventListener( 'change' , function() { selectPlacerMapGroup( this.value ) } , true );
	
	for( var i = 0 ; i < _groups.length ; i++ )
	{
		var opt = document.createElement('option');
		var tn = document.createTextNode( _groups[i][1] );
		 
		opt.value = _groups[i][0];
		 
		opt.appendChild( tn );
	 
		selector.appendChild( opt );
	}
}

function selectPlacerMapGroup( _gid )
{
	currentlySelectedPlaceMap = _gid;
	refreshPlacerMap();
}

function selectGroup( _gid )
{
	if( _gid.match( '..:..:..:..:..:..:..:..' ) )
	{
		getTestData();
		selectedBoard = _gid;
	}
}

function initialize() 
{
	{
		graph = new canvasGraph('graph');
		restoreSize();
		//graph.resize( 780 , 400 );
		graph.xLabel( "Time" );
		graph.xAxisIsTime = true;
		graph.yLabel( "Temperature (°C)" );
		graph.redraw();

		document.getElementById('dbg').addEventListener( 'click' , function() { eval( prompt('eval:') ) } , true );

		document.getElementById('gph').addEventListener( 'click' , function() {; graph.addGraph( randomLine() ) } , true );

		//document.getElementById('notifier').addEventListener( 'click' , toggleNotifierDrop );

		document.getElementById('not').addEventListener( 'click' , function() { notify('New sensors detected!' , 'Four new sensors have been detected!' , function() { this.parentNode.removeChild(this);removeNotification() } ) } , true );

		document.getElementById('addn').addEventListener( 'click' , checkNotify , true );

		document.getElementById('rsz').addEventListener( 'click' , resizeMe , true );

		document.getElementById('cgph').addEventListener( 'click' , function() { graph.clearLines();graph.redraw() } , true );
		
		document.getElementById('cgt').addEventListener( 'click' , listBoards , true );
		
		document.getElementById('addGroup').addEventListener( 'click' , function() { var txt = prompt('Enter the new group name','name'); if( txt ) createGroup( txt ); setTimeout( function() { setMode( "groups" ) } , 1000 ) } , true );
		
		document.addEventListener( 'keypress' , keypressHandler , true );
		
		document.getElementById('home').addEventListener('click', function() { setMode('graph') }, true);
		
		document.getElementById('groups').addEventListener('click', function() { setMode('groups') }, true);
		
		document.getElementById('heatmap').addEventListener('click', function() { setMode('heatmap') }, true);
		
		
		createGraphTypeChooser( "graphTypeContainer" )
		
		setMode( "graph" );
		
		heatmap = new canvasMap("heatmapGraph");
		
		/* Image preload */
		at3jpg = new Image();
		at3jpg = "maps/at3.jpg";
		
		dz = document.getElementById('placerDz');
		dz.addEventListener('dragenter', dragEnterListener ,true);
		dz.addEventListener('dragover', dragOverListener ,true);
		dz.addEventListener('drop', dropListener,true);
		
		addRangeSelector( 'rangeSelectorContainer' );
		
		changeRangeSlider( 360 );
		
		//listBoards();
		
		refresh();
		setInterval( refresh , 3000 );
	}

}

function refresh() 
{
	var d = new Date();
	starttime =  Math.floor(d.getTime()/1000) - maxDistTime;

	if(MODE=='graph')
	{
		checkUnassignedBoards();
		getTestData();
		//listGroups( 'gMapSelect' );
		listBoardsCheckable(graphPageSelectedGroup,'graphNodeSelect');
	}else if ( MODE == 'heatmap' )
	{
		getBoardPositions( heatmapPageSelectedGroup );
	}
		
}

function toggleLine( evt )
{
	graphNodeStates[this.name] = this.checked;
	if( this.checked )
	{
		//Simply add the graph
		graph.addGraph( trackedLines[this.name] )
	} else {
		graph.clearLines();
		graph.redraw();
		//Otherwise remove all graphs and add those that are shown
		for( shown in graphNodeStates )
		{
			if( graphNodeStates[shown] == true )
			{
				graph.addGraph( trackedLines[shown] );
			}
		}
	}
}

function createGraphTypeChooser( _parent )
{
	var types = ["temperature","humidity","pressure","light","latency","hops"];
	var parent = document.getElementById( _parent );
	
	var selector = document.createElement('select');
	selector.setAttribute('id','graphTypeSelect');
	selector.addEventListener( 'change' , setGraphType );
	
	for( i in types )
	{
		var nde = document.createElement('option');
		nde.setAttribute('value',types[i]);
		nde.appendChild( document.createTextNode(types[i]) );

		selector.appendChild( nde );
	}
	
	parent.appendChild( document.createTextNode('Sensor type: ') );
	parent.appendChild( selector );
}

function showWelcome()
{
	xt = document.getElementById('welcome');
	var c=document.getElementById('welcomeText');
	c.style.display="Block";
	xt.style.opacity = 1;
	xt.style.display = "Block";
}

function hideWelcome()
{
	xt = document.getElementById('welcome');
	var c=document.getElementById('welcomeText');
	c.style.display="None";
	xt.style.opacity = 0;
	setTimeout( function() { document.getElementById('welcome').style.display='none' } , 3000 );
}

function clearGraphVars()
{
	rawNodes = [];
	graphNodeStates = {};
	graphLines = {};
	trackedLineColors = {};
	trackedLines = {};
	
	graph.clearLines();
	graph.redraw();
}

function setGraphType()
{
	clearGraphVars();
	if( this.value == "temperature" )
	{
		graph.yLabel( "Temperature (°C)" );
		_graphdatatype = "temp";
	} else if ( this.value == "humidity" )
	{
		graph.yLabel( "Humidity (%RH)" );
		_graphdatatype = "humidity";
	} else if ( this.value == "pressure" )
	{
		graph.yLabel( "Pressure (kPa)" );
		_graphdatatype = "pressure";
	} else if ( this.value == "light" )
	{
		graph.yLabel( "Light level" );
		_graphdatatype = "light";
	} else if ( this.value = "latency" )
	{
		graph.yLabel( "Latency (ms)" );
		_graphdatatype = "latency";
	} else if ( this.value = "hops" )
	{
		graph.yLabel( "Hop Count" );
		_graphdatatype = "hops";
	}
}

function setMode( _mode )
{
	if( _mode == "graph" )
	{
		//Show graph
		
		MODE = "graph";
		listGroups( 'gMapSelect' );
		document.getElementById('heatmapArea').style.display = "None";
		document.getElementById('grapharea').style.display = "Block";
		document.getElementById('nodePlacer').style.display = "None";
		listBoardsCheckable(0,'graphNodeSelect');
		
	} else if ( _mode == "groups" )
	{
		/* Clear all notifications */
		clearAllNotifications();
		
		MODE = "groups";
		
		document.getElementById('heatmapArea').style.display = "None";
		document.getElementById('grapharea').style.display = "None";
		document.getElementById('nodePlacer').style.display = "Block";
		listGroups('leftGroupSelect');
		listGroups('rightGroupSelect');
		currentlySelectedPlaceMap = 0;
		listBoards(currentlySelectedPlaceMap,'nodesToPlace');
	} else if ( _mode == "heatmap" )
	{	
		listGroups( 'heatmapGroupSelector' );
		document.getElementById('heatmapArea').style.display = "Block";
		document.getElementById('grapharea').style.display = "None";
		document.getElementById('nodePlacer').style.display = "None";
		
		MODE = "heatmap";
	}

}