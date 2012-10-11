/* XML remote fetch and handling */

var rawNodes = [];
var graphNodeStates = {};
var graphLines = {};
var trackedLineColors = {};
var trackedLines = {};
var heatmapPositions = {};

function unassignedBoards()
{
  return listBoards( 0 );
}

function listGroups( _chooser )
{
  xmlhttp = new XMLHttpRequest();
  xmlhttp._chooser = _chooser;
  xmlhttp.onreadystatechange = function()
  {
    if( this.readyState == 4 && this.status == 200 )
    {
      var newGroups = [];
      var groups = this.responseXML.getElementsByTagName('group');
      for( k = 0 ; k < groups.length ; k++ )
      {
		newGroups.push( [ groups[k].getAttribute('id') , groups[k].getAttribute('name') ] );
        
      }
	  setGroups( newGroups , this._chooser );
    }
  }
  xmlhttp.open("GET","/data?type=group&action=list",true);
  xmlhttp.send(null);
}

/*
function listBoardsOld( _gid , _chooser )
{
  xmlhttp = new XMLHttpRequest();
  xmlhttp._gid = _gid;
  xmlhttp._chooser = _chooser;
  xmlhttp.onreadystatechange = function()
  {
    if( this.readyState == 4 && this.status == 200 )
    {
        var grs = this.responseXML.getElementsByTagName( 'group' )
		if( !grs )
			return;
		
		var boards = grs.childNodes.getElementsByTagName('board');
		if( !boards )
			return;
		
		var chooser = document.getElementById( _chooser );
		
		var aboards = [];
		
		//while( chooser.firstChild )
			//chooser.removeChild( chooser.firstChild );
		
		for( var i = 0 ; i < boards.length ; i++ )
		{
			aboards.push( [ boards[i].getAttribute('id') , boards[i].getAttribute('id') ] );
		}
		
		setGroups( aboards , this._chooser );
	}
  }
  xmlhttp.open("GET","/data?type=chips&action=display&gid=" + _gid ,true);
  
  xmlhttp.send(null);
}*/

function trackNewLine( _bid )
{
	trackedLines[_bid] = new canvasGraphLine();
	var col = colorList();
	trackedLines[_bid].setColor( col );
	trackedLineColors[_bid] = col;
}

function checkUnassignedBoards( _gid )
{
  xmlhttp = new XMLHttpRequest();
  xmlhttp._gid = _gid;
  xmlhttp.onreadystatechange = function()
  {
    if( this.readyState == 4 && this.status == 200 )
    {
        var boards = this.responseXML.getElementsByTagName( 'group' )[0].getElementsByTagName('board');
		var groupid = this.responseXML.getElementsByTagName( 'group' )[0].getAttribute('id');
		
		for( var i = 0 ; i < boards.length ; i++ )
		{
			var bid = boards[i].getAttribute('id');
			if( !( bid in unassignedBoards) && groupid == 0 )
			{
				notify( 'New node detected' , 'A new node has been detected with address ' + bid , function() { removeNotification( this ); setMode( 'groups' ) } );
				unassignedBoards[bid] = true;
			}
		}
	}
  }
  xmlhttp.open("GET","/data?type=chips&gid=0" ,true);
  
  xmlhttp.send(null);
}


function listBoardsCheckable( _gid , _chooser )
{
  xmlhttp = new XMLHttpRequest();
  xmlhttp._gid = _gid;
  xmlhttp._chooser = _chooser;
  xmlhttp.onreadystatechange = function()
  {
    if( this.readyState == 4 && this.status == 200 )
    {
        var boards = this.responseXML.getElementsByTagName( 'group' )[0].getElementsByTagName('board');
		var groupid = this.responseXML.getElementsByTagName( 'group' )[0].getAttribute('id');
		var chooser = document.getElementById( _chooser );
		
		
		while( chooser.firstChild )
			chooser.removeChild( chooser.firstChild );
		
		for( var i = 0 ; i < boards.length ; i++ )
		{
			var bid = boards[i].getAttribute('id');
			var state = boards[i].getAttribute('state');
			
			if( !( bid in unassignedBoards) && groupid == 0 )
			{
				notify( 'New node detected' , 'A new node has been detected with address ' + bid , function() { removeNotification( this ); setMode( 'groups' ) } );
				unassignedBoards[bid] = true;
			}
		
			var el = document.createElement('li');
			
			var cbx = document.createElement('input');
			cbx.setAttribute('type','checkbox');
			cbx.setAttribute('name', bid );
			
			if( !(bid in graphNodeStates) )
				graphNodeStates[bid] = false;
			
			if( !(bid in trackedLines) )
				trackNewLine( bid );
				
			//trackedLineColors[bid] = trackedLines[bid].
				
			if( graphNodeStates[bid] == true )
				cbx.checked = true;
				
			cbx.addEventListener( 'click' , toggleLine );
			
			var elt = document.createTextNode(boards[i].getAttribute('id'));

			el.appendChild( cbx );
			el.appendChild( elt );
			chooser.appendChild( el );
			el.style.backgroundColor = trackedLineColors[bid];
		}
	}
  }
  xmlhttp.open("GET","/data?type=chips&gid=" + _gid ,true);
  
  xmlhttp.send(null);
}

function getPlacerMapNodes( _gid , _callback )
{
	xmlhttp = new XMLHttpRequest();
	xmlhttp._gid = _gid;
	xmlhttp._callback = _callback;
	xmlhttp.onreadystatechange = function()
	{
		if( this.readyState == 4 && this.status == 200 )
		{
			boardsOnMap = {};
			var boards = this.responseXML.getElementsByTagName( 'group' )[0].getElementsByTagName('board');

			for( var i = 0 ; i < boards.length ; i++ )
			{
				var bid = boards[i].getAttribute('id');
				var x = boards[i].getAttribute('x') == 'undefined' ? 0 : parseInt(boards[i].getAttribute('x'));
				var y = boards[i].getAttribute('y') == 'undefined' ? 0 : parseInt(boards[i].getAttribute('y'));
				
				boardsOnMap[bid] = [x,y];
			}
			
			eval( this._callback );
		}
	}
  xmlhttp.open("GET","/data?type=chips&gid=" + _gid ,true);
  
  xmlhttp.send(null);
}

function listBoards( _gid , _chooser )
{
  xmlhttp = new XMLHttpRequest();
  xmlhttp._gid = _gid;
  xmlhttp._chooser = _chooser;
  xmlhttp.onreadystatechange = function()
  {
    if( this.readyState == 4 && this.status == 200 )
    {
        var boards = this.responseXML.getElementsByTagName( 'group' )[0].getElementsByTagName('board');
		var chooser = document.getElementById( _chooser );
		
		
		while( chooser.firstChild )
			chooser.removeChild( chooser.firstChild );
		
		for( var i = 0 ; i < boards.length ; i++ )
		{
			var el = document.createElement('li');
			var elt = document.createTextNode(boards[i].getAttribute('id'));
			el.appendChild( elt );
			el.setAttribute('bid',boards[i].getAttribute('id'));
			el.setAttribute('draggable' , true );
			el.addEventListener( 'dragstart' , dragStartListener );
			el.setAttribute('id' , boards[i].getAttribute('id'));
			chooser.appendChild( el );
		}
	}
  }
  xmlhttp.open("GET","/data?type=chips&gid=" + _gid ,true);
  
  xmlhttp.send(null);
}


function addBoardToGroup( _bid , _gid , _x , _y , _oncomplete )
{
  xmlhttp = new XMLHttpRequest();
  xmlhttp.oncomplete = _oncomplete
  xmlhttp.onreadystatechange = function( )
  {
    if( this.readyState == 4 && this.status == 200 )
      eval( this.oncomplete );
  }
  
  xmlhttp.open("GET","/data?type=group&action=assign&chip=" + _bid + "&group=" + _gid + "&x=" + _x + "&y=" + _y , true )
  xmlhttp.send(null);
}

/*
function removeBoardFromGroup( _bid , _oncomplete )
{
  xmlhttp = new XMLHttpRequest();
  xmlhttp.oncomplete = _oncomplete;
  xmlhttp.onreadystatechange = function( )
  {
    if( this.readyState == 4 && this.status == 200 )
      eval( this.oncomplete );
  }
  
  xmlhttp.open("GET","/data?type=remove&chip=" + _bid );
  xmlhttp.send(null);
}*/

function getBoardPositions( _gid )
{
	xmlhttp = new XMLHttpRequest();
	xmlhttp._gid = _gid;

	xmlhttp.onreadystatechange = function()
	{
		
		if( this.readyState == 4 && this.status == 200 )
		{
			heatmapPositions = {};
			var boards = this.responseXML.getElementsByTagName('board');
			for( var i = 0 ; i < boards.length ; i++ )
			{
				var x = Math.floor( boards[i].getAttribute('x')/5 );
				var y = Math.floor( boards[i].getAttribute('y')/5 );
				heatmapPositions[ boards[i].getAttribute('id') ] = x+":"+y;
			}
			
			getHeatmapData( this._gid );
		}		
	}
	xmlhttp.open("GET","/data?type=chips&gid="+_gid , true );
	xmlhttp.send( null );
}

function getHeatmapData( _gid )
{
	xmlhttp = new XMLHttpRequest();
	xmlhttp._gid = _gid;

	xmlhttp.onreadystatechange = function()
	{
		var boardsForHeatmap = {};
		if( this.readyState == 4 && this.status == 200 )
		{
			var boards = this.responseXML.getElementsByTagName('board');
			for( var i = 0 ; i < boards.length ; i++ )
			{	
				var timeElem = boards[i].getElementsByTagName('time')[0];
				if( ! timeElem )
					continue;
				
				var coord = heatmapPositions[ boards[i].getAttribute('id') ];
				var num = parseFloat(timeElem.firstChild.nodeValue);
				boardsForHeatmap[ coord ] = num;
			}
		
			var im = new Image();
			im.src="maps/at3.jpg";
			
		
			heatmap.context.clearRect( 0 , 0 , heatmap.canvas.width , heatmap.canvas.height );
			heatmap.context.drawImage( im , 0 , 0 , 700 , 350 );
			heatmap.setHeatmapMinVal(20.0);
			heatmap.setHeatmapMaxVal(30.0);
			heatmap.setHeatmapAlpha(0.8);
			heatmap.setHeatmapPointRadius(9999);
			heatmap.calcHeatmap(boardsForHeatmap, 700/5, 350/5);
			heatmap.redraw();
		}		
	}
	
	var d = new Date();
	xmlhttp.open("GET","/data?type=data&action=display&sensor=temp&start=" + (Math.floor(d.getTime()/1000)-60) + "&end="+Math.floor(d.getTime()/1000)+"&step=1&group=" + _gid , true );
	xmlhttp.send( null );
}

function getTestData( )
{
	//graph.clearLines();
	xmlhttp = new XMLHttpRequest();
	
	var lines = 0;
	
	xmlhttp.onreadystatechange = function()
	{
		if( this.readyState == 4 && this.status == 200 )
		{
			var boards = this.responseXML.getElementsByTagName('board');
			for( var i = 0 ; i < boards.length ; i++ )
			{
				var boardId = boards[i].getAttribute('id');
				var times = boards[i].getElementsByTagName('time');
				var line = [];
				
				for( var jk = 0 ; jk < times.length ; jk++ )
				{
					line.push( [ parseInt(times[jk].getAttribute('value')) , parseFloat(times[jk].firstChild.nodeValue) ] );
				}
				
				if( line.length > 0 )
				{
					if( !( boardId in trackedLines) )
					{
						trackNewLine( boardId );
					
					} else 
					{
						trackedLines[boardId].setPoints( line );
					}
				}
			}
			
		graph.calcDataRanges();
		graph.redraw();
		}		
	}
	
	var d = new Date();
	xmlhttp.open("GET","/data?type=data&action=display&sensor=" + _graphdatatype + "&start=" + starttime + "&end="+Math.floor(d.getTime()/1000)+"&step=40" , true );
	xmlhttp.send( null );
}

function createGroup( _groupName , _oncomplete )
{
	xmlhttp = new XMLHttpRequest();
	xmlhttp.oncomplete = _oncomplete;
	xmlhttp.onreadystatechange = function()
	{
		if( this.readyState == 4 && this.status == 200 )
			eval( this.oncomplete )
	}
	
	xmlhttp.open("GET","/data?type=group&action=create&name=" + _groupName , true );
	xmlhttp.send( null );
}


  