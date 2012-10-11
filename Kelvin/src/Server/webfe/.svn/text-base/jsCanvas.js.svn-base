/* Constructor */
function jsCanvas( _eid )
{
	//.
}

jsCanvas.prototype.init = function( _eid )
{
	this.eid = _eid;
	this.canvas = document.getElementById( _eid );
	if( !this.canvas )
		alert( 'bad element canvas id when creating jsCanvas "' + this._eid + '"' );

	this.context = this.canvas.getContext('2d');
	if( !this.context )
		alert( 'unable to get context, browser compatability issue??' );
}

jsCanvas.prototype.redraw = function()
{
	//Implement the redraw in a subclass
}

jsCanvas.prototype.resize = function( _width , _height )
{
	this.canvas.width = _width;
	this.canvas.height = _height;
}

/* Avoid it if possible */
jsCanvas.prototype.clear = function()
{
	this.context.clearRect( 0 , 0 , this.canvas.width , this.canvas.height );
}


jsCanvas.prototype.testg = function()
{
	this.context.fillStyle = '#ff00ff';
	this.context.fillRect( 0 , 100 , 100 , 50 );
}
