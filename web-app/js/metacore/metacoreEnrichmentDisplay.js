// --- enrichment display ----
	
	BAR_MAX = 400;
	BAR_WIDTH = 10;
	TICKS_COUNT = 8;
	TICK_FONT_FAMILY = 'Verdana, Helvetica, sans-serif';
	TICK_FONT_SIZE = 10;
	TICK_FONT_WEIGHT = 100;
	
	function drawScale(min, max, id_prefix) {
		var R = Raphael(id_prefix + "scale", 500, 20);
		R.path("M0,0L400,0").attr({ stroke: "black" });
		var tick_step = (max - min) / TICKS_COUNT;
		var tick_width = BAR_MAX / TICKS_COUNT;
		for (var i=0; i<TICKS_COUNT; i++) {
			var tick_value = min + i*tick_step;
			tick_value = tick_value.toFixed(1);
			var tick_offset = i*tick_width;
			R.path("M" + tick_offset + ",0v5").attr({ stroke: "black" });
			R.text(tick_offset, 9, tick_value).attr({ 
				'font-family': TICK_FONT_FAMILY, 'font-size': TICK_FONT_SIZE, stroke: 'white', 'stroke-width': 0,
				'text-anchor': (i==0)?'start':'middle', 'font-weight': TICK_FONT_WEIGHT 
			});
		}
		R.path("M" + BAR_MAX + ",0v5").attr({ stroke: "black" });
		R.text(BAR_MAX, 9, max.toFixed(1)).attr({ 'font-family': TICK_FONT_FAMILY, 'font-size': TICK_FONT_SIZE, stroke: 'black', 'stroke-width': 0, 'text-anchor': 'end', 'font-weight': TICK_FONT_WEIGHT });
		
		R.text(BAR_MAX+5, 9, "-log(pValue)").attr({ 'font-family': TICK_FONT_FAMILY, 'font-size': TICK_FONT_SIZE, stroke: 'black', 'stroke-width': 0, 'text-anchor': 'start', 'font-weight': 'bold' });
	}
	
	function drawEnrichment(data, id_prefix) {
		id_prefix = typeof id_prefix !== 'undefined' ? id_prefix : '';
		
		jQuery('#' + id_prefix + 'metacoreEnrichmentResults').css('display', 'block'); // show results div
		
		// determine min/max value
		var min = 0;
		var max = 0;
		
		for (var i=0; i<data.enrichment.process.length; i++) {
			var val = data.enrichment.process[i].exp[0].value;
			max = Math.max(max, val);
			min = Math.min(min, val);
		}
		
		// draw enrichment
		for (var i=0; i<data.enrichment.process.length; i++) {
			var proc = data.enrichment.process[i];
			var cell_id = id_prefix + "cell" + proc.id;
			jQuery('#' + id_prefix + 'enrichment  > tbody:last').append(
				'<tr><td>' + (i+1) + '</td><td>'
				+ '<a href="' + data.enrichment.info_url + proc.id + '" target="_blank">' + proc.name + '</a>' 
				+ '</td><td id="' + cell_id 
				+ '"</td><td>'+ proc.val.toExponential(3) 
				+'</td><td>' + proc.exp[0].obj_cnt + '/' + proc.obj_cnt + '</td></tr>'
			);
			var R = Raphael(cell_id, 450, 10);
			var len = (proc.exp[0].value - min) / (max - min) * BAR_MAX;
			R.rect(0,0,0,10).attr({ fill: "90-#ffb4bb-#ffb400:20-#ffb400:80-#ffb4bb", stroke: 'none'}).animate({ width: len }, 1000)
			R.text(BAR_MAX+5, 5, proc.exp[0].value.toPrecision(5)).attr({ 'font-family': TICK_FONT_FAMILY, 'font-size': TICK_FONT_SIZE, stroke: 'white', 'stroke-width': 0, 'text-anchor': 'start', 'font-weight': TICK_FONT_WEIGHT });
		}
		
		drawScale(min, max, id_prefix);
	}
