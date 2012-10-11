function canvasMap( _eid )
{
	jsCanvas.prototype.init.call( this, _eid );
	this.bgImage = "";
	this.heatmap = new Object();
	this.heatmap.values = []; //{"1:2":22}
	this.heatmap.width = 0;
	this.heatmap.height = 0;
	this.heatmap.minVal = 0.0; // C
	this.heatmap.maxVal = 30.0; // C
	this.heatmap.pointRadiusSq = 999*999; // squared
	this.heatmap.alpha = 1.0;
}

canvasMap.prototype = new jsCanvas();

canvasMap.prototype.setHeatmapMinVal = function( _min )
{
	this.heatmap.minVal = _min;
}

canvasMap.prototype.setHeatmapMaxVal = function( _max )
{
	this.heatmap.maxVal = _max;
}

canvasMap.prototype.setHeatmapAlpha = function( _alpha )
{
	this.heatmap.alpha = _alpha;
}

/*
 * Sets the radius of influence around an unknown point.
 */
canvasMap.prototype.setHeatmapPointRadius = function( _radius )
{
	this.heatmap.pointRadiusSq = _radius*_radius; // store squared
}

/*
 * @return array of values.
*/
canvasMap.prototype.calcHeatmap = function( _map, _width, _height )
{
	this.heatmap.width = _width;
	this.heatmap.height = _height;
	// overwrite old this.heatmap.values

	for(var y=0; y<_height; ++y) {
		for(var x=0; x<_width; ++x) {
			var coordKey = x + ":" + y;
			var resultIndex = (y*_width) + x;

			if(coordKey in _map) {
				this.heatmap.values[resultIndex] = _map[coordKey];
				continue;
			}

			/* Calculate smoothed value */

			var totDstSq = 0.0;
			var valDstSqMap = {};

			for(var key in _map) {
				var coordStrs = key.split(":");
				var xDelta = parseInt(coordStrs[0]) - x;
				var yDelta = parseInt(coordStrs[1]) - y;
				var dstSq = xDelta*xDelta + yDelta*yDelta;

				if(dstSq > this.heatmap.pointRadiusSq) {
					continue;
				}

				totDstSq += 1/dstSq;

				valDstSqMap[key] = [_map[key], 1/dstSq];
			}

			this.heatmap.values[resultIndex] = 0.0; // initialise

			for(var key in valDstSqMap) {
				this.heatmap.values[resultIndex] += (valDstSqMap[key][1]/totDstSq)*valDstSqMap[key][0];
			}
		}
	}
}

canvasMap.prototype.redraw = function() {
	var rectWidth = this.canvas.width / this.heatmap.width;
	var rectHeight = this.canvas.height / this.heatmap.height;

	for(var y=0; y<this.heatmap.height; ++y) {
		for(var x=0; x<this.heatmap.width; ++x) {
			var heatmapValue = this.heatmap.values[(y*this.heatmap.width) + x];
			var midVal = (this.heatmap.maxVal + this.heatmap.minVal)/2;
			var valRange = this.heatmap.maxVal - midVal;

			if(heatmapValue > this.heatmap.maxVal) {
				heatmapValue = this.heatmap.maxVal;
			} else if(heatmapValue < this.heatmap.minVal) {
				heatmapValue = this.heatmap.minVal;
			}

			var cR = Math.floor(((heatmapValue - midVal)*0xff)/valRange);
			cR = (cR > 0 ? cR : 0);
			
			var cB = Math.floor(((midVal - heatmapValue)*0xff)/valRange);
			cB = (cB > 0 ? cB : 0);

			var cG = 150;
			if(heatmapValue < midVal) {
				cG = Math.floor(((heatmapValue*0.60)*cG)/midVal);
			} else {
				cG = Math.floor(((midVal*0.60)*cG)/heatmapValue);
			}
			cG -= Math.floor((cR+cB) * 0.45);
			cG = (cG > 0 ? cG : 0);

			this.context.fillStyle = "rgba("+cR+","+cG+","+cB+","+this.heatmap.alpha+")";
			this.context.fillRect(x*rectWidth, y*rectHeight, rectWidth, rectHeight);
		}
	}
}

canvasMap.prototype.setBackground = function( _imgurl )
{
	this.bgImage = _imgurl;
}

