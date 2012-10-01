var visualize = function(){
	var w = 480,
    h = 290;

	var svg = d3.select("body").append("svg:svg")
    	.attr("width", w)
    	.attr("height", h);
	
	var defs = svg.append("svg:defs");

	defs.append("svg:clipPath")
    .attr("id", "circle1")
    .append("svg:circle")
    .attr("cx", 200)
    .attr("cy", 110)
    .attr("r", 90);

	defs.append("svg:clipPath")
    .attr("id", "circle2")
    .append("svg:circle")
    .attr("cx", 300)
    .attr("cy", 110)
    .attr("r", 90);

	defs.append("svg:clipPath")
    .attr("id", "circle3")
    .append("svg:circle")
    .attr("cx", 250)
    .attr("cy", 180)
    .attr("r", 90);

	svg.append("svg:rect")
    .attr("clip-path", "url(#circle1)")
    .attr("width", w)
    .attr("height", h)
    .attr("id", "1")
    .style("fill", "#ff0000")
    .on("click", mouseclick);

	svg.append("svg:rect")
	.attr("clip-path", "url(#circle2)")
    .attr("width", w)
    .attr("height", h)
    .attr("id", "2")
    .style("fill", "#00ff00")
    .on("click", mouseclick);

	svg.append("svg:rect")
    .attr("clip-path", "url(#circle3)")
    .attr("width", w)
    .attr("height", h)
    .attr("id", "3")
    .style("fill", "#0000ff")
    .on("click", mouseclick);

	svg.append("svg:g")
    .attr("clip-path", "url(#circle1)")
    .append("svg:rect")
    .attr("clip-path", "url(#circle2)")
    .attr("width", w)
    .attr("height", h)
    .attr("id", "12")
    .style("fill", "#ffff00")
    .on("click", mouseclick);

	svg.append("svg:g")
    .attr("clip-path", "url(#circle2)")
    .append("svg:rect")
    .attr("clip-path", "url(#circle3)")
    .attr("width", w)
    .attr("height", h)
    .attr("id", "23")
    .style("fill", "#00ffff")
    .on("click", mouseclick);

	svg.append("svg:g")
    .attr("clip-path", "url(#circle3)")
    .append("svg:rect")
    .attr("clip-path", "url(#circle1)")
    .attr("width", w)
    .attr("height", h)
    .attr("id", "13")
    .style("fill", "#ff00ff")
    .on("click", mouseclick);

	svg.append("svg:g")
    .attr("clip-path", "url(#circle3)")
    .append("svg:g")
    .attr("clip-path", "url(#circle2)")
    .append("svg:rect")
    .attr("clip-path", "url(#circle1)")
    .attr("width", w)
    .attr("height", h)
	.attr("id", "123")
    .style("fill", "#ffffff")
    .on("click", mouseclick);
	
	//add counts for each area
	addCount(svg, 150, 80, 2);
	addCount(svg, 245, 80, 1);
	addCount(svg, 340, 80, 1);
	addCount(svg, 190, 160, 1);
	addCount(svg, 245, 160, 1);
	addCount(svg, 290, 160, 2);
	addCount(svg, 240, 240, 1);
	
	//add lable for each set
	addCount(svg, 50, 80, "List1");
	addCount(svg, 400, 80, "List3");
	addCount(svg, 225, 290, "List2");
}

function mouseclick(d, i){
	switch(this.id){
	case "1":
		jQuery("#results").val(jQuery('#results').val()+"TTI1 INPPP1 ");
		break;
	case "12":
		jQuery("#results").val(jQuery('#results').val()+"H2AFJ ");
		break;
	case "2":
		jQuery("#results").val(jQuery('#results').val()+"MET ");
		break;
	case "13":
		jQuery("#results").val(jQuery('#results').val()+"TMX1 ");
		break;
	case "123":
		jQuery("#results").val(jQuery('#results').val()+"ILF3 ");
		break;
	case "23":
		jQuery("#results").val(jQuery('#results').val()+"DCT CRYGN ");
		break;
	case "3":
		jQuery("#results").val(jQuery('#results').val()+"SCN11A ");
		break;
	}
}

function addCount(svg, x, y, count){
	svg.append("svg:text")
	.attr("x",x)
	.attr("y",y)
	.text(count);
}

function resetResults(){
	jQuery("#results").val("");
}
function resetOutputList(){
	jQuery("#outputList").val("");
}
