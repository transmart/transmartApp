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
  

package i2b2


class StringLineReader {
	public static String LINE_BREAK = "\n";
	
	String str;
	
	int pos = 0;
	int len = 0;
	
	public StringLineReader() {}
		
	public StringLineReader(String str) {
		this.str = str;
		this.len = str.length();
	}
	
	public String readLine() {
		if (str == null || pos > len) return null;
		int idx = str.indexOf(LINE_BREAK, pos);
		if (idx == 0 || idx == pos) {	// The first line is empty, or the next line is empty
			pos ++;
			return "";
		}
		else if (idx > 0) {
			String result = str.substring(pos, idx).trim();
			pos = idx + 1;
			return result;
		}
		else if (idx < 0 && pos <= len){	// Reach the end
			String result = str.substring(pos);
			pos = len + 1;
			return result;
		}
		else {
			pos = len + 1;
			return null;
		}
	}
	
	public static void main(String[] args) {
		String test1 = '''1\n2\n3''';
		String test2 = '''\n\n1\n2\n3''';
		String test3 = '''\n\n1\n2\n\n3''';
		String test4 = '''\n\n1\n2\n3\n\n\n''';
		
		print "Start testing 1:"
		test(test1);
		print "Finished testing 1:"
		
		print "Start testing 2:"
		test(test2);
		print "Finished testing 2:"
		
		print "Start testing 3:"
		test(test3);
		print "Finished testing 3:"
		
		print "Start testing 4:"
		test(test4);
		print "Finished testing 4:"
		
	}
	
	public static void test(String str) {
		StringLineReader reader = new StringLineReader(str);
		String line = null;
		while ((line = reader.readLine()) != null) {
			print line + "\n";
		}
	}
}
