CREATE OR REPLACE FUNCTION TM_CZ."PARSE_NTH_VALUE" (pValue varchar2, location NUMBER, delimiter VARCHAR2)
   return varchar2
is
   v_posA number;
   v_posB number;

begin

   if location = 1 then
      v_posA := 1; -- Start at the beginning
   else
      v_posA := instr (pValue, delimiter, 1, location - 1); 
      if v_posA = 0 then
         return null; --No values left.
      end if;
      v_posA := v_posA + length(delimiter);
   end if;

   v_posB := instr (pValue, delimiter, 1, location);
   if v_posB = 0 then -- Use the end of the file
      return substr (pValue, v_posA);
   end if;
   
   return substr (pValue, v_posA, v_posB - v_posA);

end parse_nth_value;
/

