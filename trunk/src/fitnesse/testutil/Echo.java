// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.testutil;

import java.io.*;

public class Echo
{
	public static void main(String[] args) throws Exception
	{
		String s;
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
		while((s = bufferedReader.readLine()) != null)
			System.out.println(s);
		System.exit(0);
	}
}
