<g:form>

     <script type="text/javascript">
          function toggleValueConstraint()
          {
               if (jQuery("#chkEnableModifierValues").is(":checked")) 
               {
                    jQuery("#modifierValueList").prop("disabled", false)
               }
               else
               {
                    jQuery("#modifierValueList").prop("disabled", true)     
               }
          }

            function valueMethodChanged(value)
            {
            if(value=="numeric")
                {
                      jQuery("#divNumericMethod").show();
                }
            if(value=="novalue")
                 {
                 jQuery("#divNumericMethod").hide();
                 }
            }

            function valueOperatorChanged(value)
            {
                 if(value=="BETWEEN")
                      jQuery("#divHighValueModifier").show();
                 else
                      jQuery("#divHighValueModifier").hide(); 
            }
               
     </script>

     <br />
     
     <h2>You may constrain simply by the presence of the modifier or further constrain by a specific value for that modifier.</h2><br />
     
     <div id="divModifierEnum" style="text-align:left; padding: 20px;display:none;">
          <input id='chkEnableModifierValues' type='checkbox' onclick='toggleValueConstraint();' /> Click here to constrain by value <br /><br />
     
          <select id='modifierValueList' multiple disabled>
               
          </select>
     </div>

     <div id="divModifierFloat" style="text-align:left; padding: 20px;display:none;">
          
          <input type="radio"  id="valueMethod_novalue" name="valueMethod" value="novalue" onclick="valueMethodChanged(this.value)">&nbsp;No Value <br />
          
          <input type="radio" id="valueMethod_numeric" name="valueMethod" checked="checked" value="numeric" onclick="valueMethodChanged(this.value)">&nbsp;By Value<br /><br/>
     
          <div id="divNumericMethod">
               <select id="valueOperator" onclick="valueOperatorChanged(this.value);">
                    <option value="LT">LESS THAN(<)</option>
                    <option value="LE">LESS THAN OR EQUAL TO(<=)</option>
                    <option value="EQ">EQUAL TO(=)</option>
                    <option value="BETWEEN">BETWEEN</option>
                    <option value="GT">GREATER THAN(>)</option>
                    <option value="GE">GREATER THAN OR EQUAL TO(>=)</option>
               </select><br/><br/>
               
               <input type="text" size="5" name="lowValueModifier" id="lowValueModifier" value="" />
               <div id="divHighValueModifier" style="display:none">AND<input type="text" size="5" name="highValueModifier" id="highValueModifier" value="" /></div>
               
          </div>
          
     </div>

     <div id="divModifierString" style="text-align:left; padding: 20px;display:none;">
          <input id='chkEnableModifierValues' type='checkbox' onclick='toggleValueConstraint();' /> Click here to constrain by value <br /><br />
     
          <select id='modifierValueList' multiple disabled>
               
          </select>
     </div>
     
     <br />
     
     <input type='button' class="flatbutton" value='Apply Modifier' id="btnModifierValuesDone">
     
</g:form>
