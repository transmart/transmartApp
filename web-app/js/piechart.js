  /*************************************************************************
 * tranSMART - translational medicine data mart
 * 
 * Copyright 2008-2012 Janssen Research & Development, LLC.
 * 
 * This product includes software developed at Janssen Research & Development, LLC.
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License 
 * as published by the Free Software  * Foundation, either version 3 of the License, or (at your option) any later version, along with the following terms:
 * 1.	You may convey a work based on this program in accordance with section 5, provided that you retain the above notices.
 * 2.	You may convey verbatim copies of this program code as you receive it, in any medium, provided that you retain the above notices.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS    * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 *
 ******************************************************************/

function drawPieChart(divid, catid, ddid, data, charttype, parentcolor, ddstack)
{
	var w = 550;
	var h = 250;
	var r = 60;
	var ir = 0;
	var textOffset = 25;
	var tweenDuration = 150;
	
	//OBJECTS TO BE POPULATED WITH DATA LATER
	var lines, valueLabels, nameLabels;
	var pieData = [];    
	var fPieData = [];
	//D3 helper function to populate pie slice parameters from array data
	var donut = d3.layout.pie().value(function(d){
		 // return d.studies; //change to val later
		return d.value;
	}).startAngle(-Math.PI/4).endAngle(2*Math.PI-Math.PI/4);

	//add the data
	pieData = donut(data);
	
	//filter
	var total = 0; 
	  fPieData = pieData.filter(filterData);
	  function filterData(element, index, array) {
	    element.name = data[index].name;
	    //element.value = data[index].studies;
	    element.value=data[index].value;
	    element.id= data[index].id;
	   // element.startAngle=element.startAngle-Math.PI/2;
	   // element.endAngle=element.endAngle-Math.PI/2;
	    total += element.value;
	    return (element.value > 0);
	  }
	//D3 helper function to create colors from an ordinal scale
	var color;
	 if(!parentcolor)
		{
		color = d3.scale.category20();
		}
	 else if(data.length==1) //single drill down
		{
		 	color=function(i){return parentcolor;};
		}
	 else //range drill down
		{
			color=function(i){return d3.rgb(parentcolor).darker(1/(i+2)).toString();};
		}
	//D3 helper function to draw arcs, populates parameter "d" in path object
	var arc = d3.svg.arc()
	  .startAngle(function(d){ return d.startAngle; })
	  .endAngle(function(d){ return d.endAngle; })
	  .innerRadius(ir)
	  .outerRadius(r);
	
	var svgid="#"+divid+"_svg"
	var msvg=d3.select(svgid);
	if(!msvg.empty())
		{	
		 msvg.remove();
		}
	var vis = d3.select('#'+divid).append("svg:svg")
	  	.attr("width", w)//w
	    .attr("height", h)//h*/
	    .attr("id", divid+'_svg');


	//GROUP FOR ARCS/PATHS
	var arc_group = vis.append("svg:g")
	  .attr("class", "arc")
	  .attr("transform", "translate(" + (w/2) + "," + (h/2) + ")");

	//GROUP FOR LABELS
	var label_group = vis.append("svg:g")
	  .attr("class", "label_group")
	  .attr("transform", "translate(" + (w/2) + "," + (h/2) + ")");

	//GROUP FOR CENTER TEXT  
	var crumb_group = vis.append("svg:g")
	  .attr("class", "center_group")
	  .attr("transform", "translate(" + (w/2) + "," + ((h/2)-r -40)+ ")"); //was h/2

	//GROUP FOR CENTER TEXT  
	var total_group = vis.append("svg:g")
	  .attr("class", "center_group")
	  .attr("transform", "translate(" + (w/2) + "," + ((h/2)+r+45) + ")"); //was h/2
	
	
	if(fPieData.length==0)
		{
		total_group.attr("transform", "translate(" + (w/2) + "," + (h/2) + ")"); //was h/2
		}
	
	//PLACEHOLDER GRAY CIRCLE
	var paths = arc_group.append("svg:circle")
	    .attr("fill", "#EFEFEF")
	    .attr("r", r);
	//WHITE CIRCLE BEHIND LABELS
	/*var whiteCircle = center_group.append("svg:circle")
	  .attr("fill", "white")
	  .attr("r", ir);*/

	// "TOTAL" LABEL
	/*var totalLabel = total_group.append("svg:text")
	  .attr("class", "label")
	  .attr("dy", -15)
	  .attr("text-anchor", "middle") // text-align: right
	  .text("TOTAL");

	//TOTAL TRAFFIC VALUE
	var totalValue = total_group.append("svg:text")
	  .attr("class", "total")
	  .attr("dy", 7)
	  .attr("text-anchor", "middle") // text-align: right
	  .text("Waiting...");*/

	//Total LABEL
	var totalValue = total_group.append("svg:text")
	  .attr("class", "total")
	  /*.attr("dy", 21)*/
	  .attr("text-anchor", "middle") // text-align: right
	  .text("Waiting...");
	  //.text("studies");

	//REMOVE PLACEHOLDER CIRCLE
    arc_group.selectAll("circle").remove();

    totalValue.text(function(){
      var kb = total;
      //return kb.toFixed(0)+" "+ charttype.charAt(0).toUpperCase() + charttype.slice(1);
      return charttype.charAt(0).toUpperCase() + charttype.slice(1);
      //return bchart.label.abbreviated(totalOctets*8);
    });
	
	
	//DRAW ARC PATHS
    paths = arc_group.selectAll("path").data(fPieData);
    

   
    paths.enter().append("svg:path")
      .attr("stroke", "white")
      .attr("stroke-width", 0.5)
      .attr("fill", function(d, i) { return color(i); })
      .attr("cursor", 'pointer')
      //.transition()
      	//.duration(tweenDuration)
      	//	.attrTween("d", pieTween)
      		.append("svg:title")
    .text(function(d) { return d.name });;
     
    paths
      .transition()
       .duration(tweenDuration)
       .attrTween("d", pieTween).each("end", function(){
          this._listenToEvents = true;
        });
    
     //paths.exit()
     // .transition()
       //.duration(tweenDuration)
       // .attrTween("d", removePieTween)
      //.remove();


    
    
    //DRAW TICK MARK LINES FOR LABELS
    lines = label_group.selectAll("line").data(fPieData);
    lines.enter().append("svg:line")
      .attr("x1", 0)
      .attr("x2", 0)
      .attr("y1", -r-3)
      .attr("y2", -r-8)
      .attr("stroke", "gray")
      .attr("transform", function(d) {
        return "rotate(" + (d.startAngle+d.endAngle)/2 * (180/Math.PI) + ")";
      });
    lines.transition()
      .duration(tweenDuration)
      .attr("transform", function(d) {
        return "rotate(" + (d.startAngle+d.endAngle)/2 * (180/Math.PI) + ")";
      });
    lines.exit().remove();

    //DRAW LABELS WITH PERCENTAGE VALUES
    valueLabels = label_group.selectAll("text.value").data(fPieData)
      .attr("dy", function(d){
        if ((d.startAngle+d.endAngle)/2 > Math.PI/2 && (d.startAngle+d.endAngle)/2 < Math.PI*1.5 ) {
          return 0;
        } else {
          return 0;
        }
      })
      .attr("text-anchor", function(d){
        if ( (d.startAngle+d.endAngle)/2 < Math.PI ){
          return "beginning";
        } else {
          return "end";
        }
      })
      .text(function(d){
        //var percentage = (d.value/total)*100;
        //return percentage.toFixed(1) + "%";
    	  return d.value;
      });

    valueLabels.enter().append("svg:text")
      .attr("class", "value")
      .attr("transform", function(d) {
        return "translate(" + Math.cos(((d.startAngle+d.endAngle - Math.PI)/2)) * (r+textOffset) + "," + Math.sin((d.startAngle+d.endAngle - Math.PI)/2) * (r+textOffset) + ")";
      })
      .attr("dy", function(d){
        if ((d.startAngle+d.endAngle)/2 > Math.PI/2 && (d.startAngle+d.endAngle)/2 < Math.PI*1.5 ) {
          return 0;
        } else {
          return 0;
        }
      })
      .attr("text-anchor", function(d){
        if ( (d.startAngle+d.endAngle)/2 < Math.PI ){
          return "beginning";
        } else {
          return "end";
        }
      }).text(function(d){
       // var percentage = (d.value/total)*100;
        //return percentage.toFixed(1) + "%";
    	  return d.value;
      });

    valueLabels.transition().duration(tweenDuration).attrTween("transform", textTween);

    valueLabels.exit().remove();


    //DRAW LABELS WITH ENTITY NAMES
    nameLabels = label_group.selectAll("text.units").data(fPieData)
     /* .attr("dy", function(d){
        //if ((d.startAngle+d.endAngle)/2 > Math.PI/2 && (d.startAngle+d.endAngle)/2 < Math.PI*1.5 ) {
    	if((d.startAngle+d.endAngle)/2 > ((3*Math.PI/4)-(Math.PI/4)) && (d.startAngle+d.endAngle)/2 < (Math.PI*5/4-Math.PI/4 )) {
    	  return 10; //17
        } else {
          return 0; //5
        }
      })*/
     // .attr("dx", -50)
      .attr("text-anchor", function(d){
        if ((d.startAngle+d.endAngle)/2 < Math.PI ) {
          return "beginning";
        } else {
          return "end";
        }
      }).text(function(d){
        return d.name;
      });

      nameLabels.enter()
      .append("svg:text")
      .attr("class", "units")
      .attr("transform", function(d, i) {
        return "translate(" + Math.cos(((d.startAngle+d.endAngle - Math.PI)/2)) * (r+textOffset) + "," + Math.sin((d.startAngle+d.endAngle - Math.PI)/2) * (r+textOffset) + ")";
      })
      /*.attr("dy", function(d, i){
        //if ((d.startAngle+d.endAngle)/2 > Math.PI/2 && (d.startAngle+d.endAngle)/2 < Math.PI*1.5 ) {
        if((d.startAngle+d.endAngle)/2 > (3*Math.PI/4) && (d.startAngle+d.endAngle)/2 < (Math.PI*5/4 )) {
    	 return (d.startAngle+d.endAngle)/10//17
        
        } else {
          return 0; //5
        	
        }
      })*/
     /* .attr("text-anchor", function(d){
        if ((d.startAngle+d.endAngle)/2 < Math.PI ) {
          return "beginning";
        } else {
          return "end";
        }
      })*/
        .text(function(d){
        return d.name;
      })
       .attr("dx", function(d){
        if ((d.startAngle+d.endAngle)/2 < Math.PI ) {
          return 20;
        } else {
          return -20-this.getComputedTextLength();
        }
      });

    nameLabels.transition().duration(tweenDuration).attrTween("transform", textTween);

    nameLabels.exit().remove();

    paths.on("click", function(d){ 
        if(this._listenToEvents){
          // Reset inmediatelly
          d3.select(this).attr("transform", "translate(0,0)")
          // Change level on click if no transition has started                
          paths.each(function(){
             this._listenToEvents = false;
          });
          d3.event.stopPropagation();
          parentcolor=d3.select(this).attr("fill");
          if(fPieData.length>1)
        	  {
        	  ddstack.push({ddid:d.id, ddname: d.name, color:parentcolor});
        	  }
          getPieChartData(divid, catid, d.id, false, charttype, parentcolor, ddstack); //false for drillback
          
        }
      })
    .on("mouseover", function(d){ 
         // Mouseover effect if no transition has started                
        if(this._listenToEvents){
          // Calculate angle bisector
          var ang = d.startAngle + (d.endAngle - d.startAngle)/2; 
          // Transformate to SVG space
          ang = (ang - (Math.PI / 2) ) * -1;

          // Calculate a 10% radius displacement
          var x = Math.cos(ang) * r * 0.1;
          var y = Math.sin(ang) * r * -0.1;

          d3.select(this).transition()
            .duration(250).attr("transform", "translate("+x+","+y+")")

        }
      })
    .on("mouseout", function(d){
      // Mouseout effect if no transition has started                
      if(this._listenToEvents){
        d3.select(this).transition()
          .duration(150).attr("transform", "translate(0,0)"); 
      }
    });

    //go back to parent if click somwhere else
    vis.on("click", function(d){ 
        if(false){
          //this._listenToEvents = false;
          /*
          // Reset inmediatelly
          d3.select(this).attr("transform", "translate(0,0)")
          // Change level on click if no transition has started                
          paths.each(function(){
             this._listenToEvents = false;
          });*/
          parentcolor="";
          if(ddstack.length>1)
        	  {
        	  ddstack.pop();
        	  }
          getPieChartData(divid, catid, ddid, true, charttype, parentcolor, ddstack);
        }
      });
    
  //BREAD CRUMBS LABEL
    var crumbLabel = crumb_group.append("svg:text")
	
	  /*.attr("dy", 21)*/
	  .attr("text-anchor", "middle") // text-align: right
	  //.text("studies");
	  
	var crumbsenter = crumbLabel.selectAll("tspan").data(ddstack).enter()
	
	crumbsenter.append("tspan")
		  .attr("class", "crumbs")
	    .text(function(d, i){
    	var t=d.ddname;
    	/*if(i < ddstack.length-1)
    	{
	    t= t+"→";
    	}*/
    	return t;
    }).on("click", function(d, i){ 
        if(true){
          //alert("d:"+d+" i:"+i);
          if(ddstack.length>1)
       	  {
          for(x=0;x<(ddstack.length-1)-i; x++)
          	{
        	  ddstack.pop();
          	}
       	  }
         getPieChartData(divid, catid, d.ddid, false, charttype, d.color, ddstack);
        }
       }).append("tspan")
       .attr("class", "arrow")
       .text(function(d, i){
    	var t="";
    	if(i < ddstack.length-1)
    	{
	    t= t+"→";
    	}
    	return t;});     
       
    

	

//Interpolate the arcs in data space.
function pieTween(d, i) {
  var s0;
  var e0;
  /*if(oldPieData[i]){
    s0 = oldPieData[i].startAngle;
    e0 = oldPieData[i].endAngle;
  } else if (!(oldPieData[i]) && oldPieData[i-1]) {
    s0 = oldPieData[i-1].endAngle;
    e0 = oldPieData[i-1].endAngle;
  } else if(!(oldPieData[i-1]) && oldPieData.length > 0){
    s0 = oldPieData[oldPieData.length-1].endAngle;
    e0 = oldPieData[oldPieData.length-1].endAngle;
  } else {*/
    s0 = 0;
    e0 = 0;
  //}
  var i = d3.interpolate({startAngle: s0, endAngle: e0}, {startAngle: d.startAngle, endAngle: d.endAngle});
  return function(t) {
    var b = i(t);
    return arc(b);
  };
}

function textTween(d, i) {
	  var a;
	  /*if(oldPieData[i]){
	    a = (oldPieData[i].startAngle + oldPieData[i].endAngle - Math.PI)/2;
	  } else if (!(oldPieData[i]) && oldPieData[i-1]) {
	    a = (oldPieData[i-1].startAngle + oldPieData[i-1].endAngle - Math.PI)/2;
	  } else if(!(oldPieData[i-1]) && oldPieData.length > 0) {
	    a = (oldPieData[oldPieData.length-1].startAngle + oldPieData[oldPieData.length-1].endAngle - Math.PI)/2;
	  } else {*/
	    a = 0;
	  //}
	  var b = (d.startAngle + d.endAngle - Math.PI)/2;

	  var fn = d3.interpolateNumber(a, b);
	  return function(t) {
	    var val = fn(t);
	    return "translate(" + Math.cos(val) * (r+textOffset) + "," + Math.sin(val) * (r+textOffset) + ")";
	  };
}
}


