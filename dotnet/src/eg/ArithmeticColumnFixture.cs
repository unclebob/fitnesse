// Modified or written by Object Mentor, Inc. for inclusion with FitNesse.
// Copyright (c) 2002 Cunningham & Cunningham, Inc.
// Released under the terms of the GNU General Public License version 2 or later.
// Copyright (c) 2002 Cunningham & Cunningham, Inc.
// Released under the terms of the GNU General Public License version 2 or later.

using System;
using fit;

namespace eg
{
	public class ArithmeticColumnFixture : ColumnFixture 
	{

		public int x;
		public int y;

		public int plus() 
		{
			return x + y;
		}

		public int minus() 
		{
			return x - y;
		}

		public int times () 
		{
			return x * y;
		}

		public int divide () 
		{
			return x / y;
		}

		public float floating () 
		{
			return (float)x / (float)y;
		}
        
        public ScientificDouble  sin () {
            return new ScientificDouble(Math.Sin(toRadians(x)));
        }

        public ScientificDouble  cos () {
            return new ScientificDouble(Math.Cos(toRadians(x)));
        }

        private double toRadians(double degrees) {
            return (degrees * Math.PI) / 180d;
        }
	}
}