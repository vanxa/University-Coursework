/* Graph constructor, takes an id of a canvas element */
function canvasGraph( _eid )
{
	this.data = new Object();
	this.grid = new Object();
    
	this.data.scaling = { "x" : 1 , "y" : 1 }
	this.data.bounds = { "x0" : 0 , "x1" : 0 , "y0" : 0 , "y1" : 0 };
	this.data.range = { "w" : 0 , "h" : 0 };
  
	this.origins = { "tl" : 0, "tr" : 1, "bl" : 2, "br" : 3 };
	jsCanvas.prototype.init.call( this , _eid );
	this.origin = this.origins.bl;
	this.context.font = "bold sans";
	this.lines = new Array();
	
	this.xLabelEdge = 1;
	this.yLabelEdge = 0;
	this.labelOutset = 5;
	this.xLabelText = "x-axis";
	this.yLabelText = "y-axis";
	this.gridPadding = [ 0 , 10 , 60 , 100 ];
	this.hGridStep = 120;
	this.vGridStep = 80;
	this.gridWidthVal = 1;
	this.gridBorder = true;
	this.gridStrokeStyle = "#cccccc";
	this.labelFillStyle = "#666666";
	
	this.xAxisIsTime = false;
	
	/* Now draw the graph */
	this.redraw();

}

/* Inheritance */
canvasGraph.prototype = new jsCanvas();

/* @TODO: set methods for xLabelEdge,yLabelEdge,origin,autogrid,labelOutset */

/* Set the label for the x-axis */
canvasGraph.prototype.xLabel = function( _label )
{
	this.xLabelText = _label;
}

/* Set the label for the y-axis */
canvasGraph.prototype.yLabel = function( _label )
{
	this.yLabelText = _label;
}

/* Set the inset for the graph from the edge of the canvas */
canvasGraph.prototype.gridInset = function( _top , _right, _bottom , _left )
{
	this.gridPadding = [ _top , _right , _bottom , _left ];
}

/* Set the style used for the text labels */
canvasGraph.prototype.labelStyle = function( _style )
{
	this.labelFillStyle = _style;
}

/* Set the style used for gridlines */
canvasGraph.prototype.gridStyle = function( _style )
{
	this.gridStrokeStyle = _style;
}

/* Set the grid line width */
canvasGraph.prototype.gridWidth = function( _width )
{
	this.gridWidthVal = _width;
}

/* Set the number of x and y grid lines, for the current canvas size */
canvasGraph.prototype.gridLines = function( _x , _y )
{
	this.hGridsX = _x;
	this.hGridsY = _y;

	this.hGridStep = this.canvas.width / _x;
	this.vGridStep = this.canvas.height / _y;
}

/* Set the canvas grid spacing in pixels, canvas size independent */
canvasGraph.prototype.gridSpacing = function( _x , _y )
{
	this.hGridStep = _x;
	this.vGridStep = _y;
}	

canvasGraph.prototype.drawLabels = function()
{
	//@TODO: labels will be broken for anything besides bottom left origin
	
	this.context.fillStyle = this.labelFillStyle;

	/* x-axis labels */

	xlabelsxoffset = 0;
	xlabelxoffset = 0;

	/* x-axis at the bottom */
	if( this.xLabelEdge == 1 )
	{
		xlabelsxoffset = this.canvas.height - this.gridPadding[0] - this.gridPadding[2] + this.labelOutset;
		xlabelxoffset = this.canvas.height;
	}
	
	this.context.textBaseline = "top";
	this.context.textAlign = "center";
	
	for( var x = this.grid.bounds.x0 ; x <= this.grid.bounds.x1 ; x+= this.hGridStep )
	{
	  if( this.xAxisIsTime )
	  {
	    //@TODO Make this display clever, i.e. don't always show yyyy/mm/dd if range is <1d
	    var time = this.data.bounds.x0 + (x-this.grid.bounds.x0)/this.data.scaling.x;
	    var d = new Date( time * 1000 );
	    
	    var hr = ( d.getHours() < 10 ? "0" : "" ) + d.getHours();
	    var min = ( d.getMinutes() < 10 ? "0" : "" ) + d.getMinutes();
	    var sec = ( d.getSeconds() < 10 ? "0" : "" ) + d.getSeconds();
	        
	    var dString = d.getFullYear() + '/' + d.getMonth() + '/' + d.getDate() + '\n' + hr + ':' +min + ':' + sec;
	    
	    this.context.fillText(
	      dString,
	      x,
	      xlabelsxoffset
	    );
	  } else {
	    this.context.fillText(
	      Math.round(  this.data.bounds.x0*100 + (x-this.grid.bounds.x0)*100/this.data.scaling.x )/100,
	      x,
	      xlabelsxoffset
	    );
	  }
	}

	/* y-axis labels */

	//This ensures that text is shown centered vertically and horizontally
	this.context.textBaseline = "middle";
	this.context.textAlign = "right";
	
	for( var y = this.grid.bounds.y1 ; y > this.grid.bounds.y0 ; y-= this.vGridStep )
	{
	  this.context.fillText(
	    Math.round( this.data.bounds.y0*100 + (this.grid.range.h - y)*100/this.data.scaling.y )/100,
	    this.gridPadding[3] - this.labelOutset,
	    y,
	    this.gridPadding[3]
	  );
	}

	/******* AXIS TITLES ******/
	/* Save context before rotating and changing font size */
	this.context.save()

	this.context.textAlign = "center";
	this.context.textBaseline = "bottom";
	this.context.font = '40px bold sans-serif';
	/* x-axis title */
	this.context.fillText( this.xLabelText , (this.canvas.width + this.gridPadding[3])/2 , xlabelxoffset );

	this.context.rotate( Math.PI/2 );
	/* y-axis title */
	this.context.textAlign = "right";
	this.context.textAlign = "center";
	this.context.fillText( this.yLabelText , (this.canvas.height + (this.gridPadding[0] - this.gridPadding[2]))/2 , 0 );	
	this.context.restore()
}

canvasGraph.prototype.drawGrid = function()
{
	this.context.strokeStyle = this.gridStrokeStyle;
	this.context.lineWidth = this.gridWidthVal;

	/*@TODO: This needs to take account of the origin for line placements,
	 * anything beside bl (bottom left) origin may not have axis bars correct
	 * (changes should be reflected in labels also) */

	/* Complete border */
	this.context.beginPath();
	this.context.moveTo( this.gridPadding[3] , this.gridPadding[0] );
	this.context.lineTo( this.canvas.width - this.gridPadding[1] , this.gridPadding[0] );
	this.context.lineTo( this.canvas.width - this.gridPadding[1] , this.canvas.height - this.gridPadding[0] - this.gridPadding[2] );
	this.context.lineTo( this.gridPadding[3] , this.canvas.height - this.gridPadding[0] - this.gridPadding[2] );
	this.context.closePath();
	this.context.stroke();
	
	/* Vertical lines */
	
	for(x=this.gridPadding[3];x<this.canvas.width;x+=this.hGridStep)
	{
		this.context.beginPath();
		this.context.moveTo( x , this.gridPadding[0]);
		this.context.lineTo( x , this.canvas.height - this.gridPadding[2] - this.gridPadding[0] );
		this.context.closePath();
		this.context.stroke();
		this.context.fill();
	}

	/* Horizontal lines */
	
	for(y=this.canvas.height-this.gridPadding[0]-this.gridPadding[2];y>this.gridPadding[0];y-=this.vGridStep)
	{
		this.context.beginPath();
		this.context.moveTo( this.gridPadding[3] , y );
		this.context.lineTo( this.canvas.width - this.gridPadding[1] , y );
		this.context.closePath();
		this.context.stroke();
		this.context.fill();
	}
}

/* This function should be called whenever the data range could have changed, e.g. on adding or changing a line
 * 
 * returns true if the ranges changed happened, otherwise false
 * 
 * */
canvasGraph.prototype.calcDataRanges = function()
{
    var oldbounds = this.data.bounds;
    
    if( this.lines.length == 0 )
      return;
    
	//var linebounds = this.lines[0].exbounds();
    
	var bounds_init = false;
	
    for( i = 0 ; i < this.lines.length ; i++ )
    {
      var linebounds = this.lines[i].exbounds();
	  
	  
	  /* If bounds have not yet been initialised then check if this line could be used as the source */
	  if( ! bounds_init && this.lines[i].numPoints() > 0 )
	  {
		this.data.bounds = { "x0" : linebounds.x0 , "y0" : linebounds.y0 , "x1" : linebounds.x1 , "y1" : linebounds.y1 };
		bounds_init = true;
		continue;
	  }
	  
	  /* Ignore lines that do not have any points */
	  
	  if( this.lines[i].numPoints() == 0 )
		continue;
      
      if( linebounds.x0 < this.data.bounds.x0 )
		this.data.bounds.x0 = linebounds.x0
      if( linebounds.x1 > this.data.bounds.x1 )
		this.data.bounds.x1 = linebounds.x1
      if( linebounds.y0 < this.data.bounds.y0 )
		this.data.bounds.y0 = linebounds.y0
      if( linebounds.y1 > this.data.bounds.y1 )
		this.data.bounds.y1 = linebounds.y1
    }
    
    this.data.range.w = this.data.bounds.x1 - this.data.bounds.x0;
    this.data.range.h = this.data.bounds.y1 - this.data.bounds.y0;  
	
	if( this.data.range.h == 0 )
	{
		this.data.range.h = 0.2;
		this.data.bounds.y0 -= 0.1;
		this.data.bounds.y1 += 0.1;
	}
	
	if( this.data.range.w == 0 )
	{
		this.data.range.w = 2;
		this.data.bounds.x0 -= 1;
		this.data.bounds.x1 += 1;
	}
    
    /* Test to see if the old bounds match the new bounds */
    if( this.data.bounds.x0 != oldbounds.x0 )
      return true;
    if( this.data.bounds.x1 != oldbounds.x1 )
      return true;
    if( this.data.bounds.y0 != oldbounds.y0 )
      return true;
    if( this.data.bounds.y1 != oldbounds.y1 )
      return true;
    
    /* Otherwise bounds did not change */
    return false;
    
}

//Hack to show time on x-axis
/*canvasGraph.prototype.xAxisIsTime = function()
{
  this.xAxisIsTime = true;
}*/

canvasGraph.prototype.clearLines = function()
{
	this.lines = new Array();
}

/* Pass a canvasGraphLine object */
canvasGraph.prototype.addGraph = function( _line )
{
	this.lines.push( _line );
	/*If calcDataRanges detects a change in bounds then the whole graph must be redrawn */
	if( this.calcDataRanges() )
	  this.redraw();
	else
	  this.graphIt( _line );
}

canvasGraph.prototype.graphAllLines = function()
{
	for(var x in this.lines)
	{
		this.graphIt( this.lines[x] );
	}
}

canvasGraph.prototype.graphIt = function( _line )
{
	this.context.save()
	/* Set clip rect */
	this.context.rect( this.gridPadding[3] , this.gridPadding[0] , this.canvas.width - this.gridPadding[3] - this.gridPadding[1] , this.canvas.height - this.gridPadding[0] - this.gridPadding[2]);
	this.context.clip();
	/* Draw path */
	this.context.beginPath();
	var pts = _line.expoints();
	
	for( x = 0 ; x < pts.length ; x++ )
	{
		//alert( 'y: ' + (pts[x][1] - this.data.bounds.y0)*this.data.scaling.y + this.gridPadding[0] );
		this.context.moveTo( (pts[x][0] - this.data.bounds.x0) * this.data.scaling.x + this.gridPadding[3] , this.grid.range.h - (pts[x][1] - this.data.bounds.y0)*this.data.scaling.y + this.gridPadding[0] );
		if( pts[x+1] )
			this.context.lineTo( (pts[x+1][0] - this.data.bounds.x0)*this.data.scaling.x + this.gridPadding[3] ,this.grid.range.h - (pts[x+1][1] - this.data.bounds.y0)*this.data.scaling.y + this.gridPadding[0]  );
	}
	this.context.closePath();
	this.context.lineWidth = 3;
	this.context.shadowBlur = 5;
	this.context.shadowColor = _line.shadowColor;
	this.context.strokeStyle = _line.lineColor;
	this.context.stroke();
	this.context.restore()
}

canvasGraph.prototype.calcGridArea = function()
{
  this.grid.bounds = {
      "x0" : ( this.gridPadding[3] ),
      "y0" : ( this.gridPadding[0] ),
      "x1" : ( this.canvas.width - this.gridPadding[1] ),
      "y1" : ( this.canvas.height - this.gridPadding[2] )
  };
  
  this.grid.range = {
      "w" : ( this.canvas.width - this.gridPadding[1] - this.gridPadding[3]) , 
      "h" : ( this.canvas.height - this.gridPadding[0] - this.gridPadding[2] )
  };
  
  this.data.scaling.x = this.grid.range.w / this.data.range.w;
  this.data.scaling.y = this.grid.range.h / this.data.range.h;
}


canvasGraph.prototype.redraw = function()
{
	this.calcGridArea();
	this.clear();
	this.drawGrid();
	this.drawLabels();
	this.graphAllLines();
}

/* canvasGraphLine */

function canvasGraphLine( _points )
{
	this.lineWidth = 3;
	this.lineBlur = 5;
	this.shadowColor = randColor();
  
	this.lineColor = randColor();
	this.shadowColor = randColor();
	if( typeof( _points ) == 'object' )
	{
	  this.points = _points;
	  this.calcRanges();
	}
	else
	  this.points = new Array();
}

canvasGraphLine.prototype.numPoints = function()
{
	return this.points.length;
}

canvasGraphLine.prototype.calcRanges = function()
{
    this.bounds = {
      "x0" : this.points[0][0],
      "y0" : this.points[0][1],
      "x1" : this.points[0][0],
      "y1" : this.points[0][1]
    };
    
    /* As the initial bounds have been set, then start at point 1 */
    for( i = 1; i < this.points.length ; i++ )
    {
      x = this.points[i];
      
      if( x[0] < this.bounds.x0 )
	this.bounds.x0 = x[0];
      if( x[0] > this.bounds.x1 )
	this.bounds.x1 = x[0];
      if( x[1] < this.bounds.y0 )
	this.bounds.y0 = x[1];
      if( x[1] > this.bounds.y1 )
	this.bounds.y1 = x[1];
    }
}

canvasGraphLine.prototype.exbounds = function()
{
  return this.bounds;
}

canvasGraphLine.prototype.expoints = function()
{
  return this.points;
}

canvasGraphLine.prototype.setPoints = function( _points )
{
  this.points = _points;
  this.calcRanges();
}

canvasGraphLine.prototype.width = function()
{
  return this.bounds.x1 - this.bounds.x0;
}

canvasGraphLine.prototype.height = function()
{
  return this.bounds.y1 - this.bounds.y0;
}

canvasGraphLine.prototype.lineWidth = function( _width )
{
  this.lineWidth = _width;
}

canvasGraphLine.prototype.lineBlur = function( _width )
{
  this.lineBlur = _width;
}

canvasGraphLine.prototype.setPoints = function( _points )
{
  this.points = _points;
  this.calcRanges();
}

canvasGraphLine.prototype.setColor = function( _col )
{
  this.lineColor = _col;
}

function colorList()
{
	/* Use hard coded colors from a list*/
	var col = okColors[colPos];
	colPos = (colPos + 1) % okColors.length;
	
	return col;
}
	

function randColor()
{
	red = Math.floor(Math.random()*120)+120;
	green = Math.floor(Math.random()*120)+120;
	blue = Math.floor(Math.random()*120)+120;

	return '#' + (red + (green<<8) + (blue << 16)).toString(16);
}
