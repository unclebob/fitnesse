// Modified or written by Object Mentor, Inc. for inclusion with FitNesse.
// Copyright (c) 2002 Cunningham & Cunningham, Inc.
// Released under the terms of the GNU General Public License version 2 or later.
// Copyright (c) 2002, 2003 Cunningham & Cunningham, Inc.
// Released under the terms of the GNU General Public License version 2 or later.

// Warning: not (yet) a general number usable in all calculations.

using System;

namespace fit
{
	public class ScientificDouble
	{
		protected double value;
		protected double precision;

		public ScientificDouble(double value)
		{
			this.value = value;
			this.precision = 0;
		}

		public static ScientificDouble ValueOf(string s)
		{
			ScientificDouble result = new ScientificDouble(double.Parse(s));
			result.precision = MeasurePrecision(s);
			return result;
		}

		public static double MeasurePrecision(string s)
		{
			double value = double.Parse(s);
			double bound = double.Parse(Tweak(s.Trim()));
			return Math.Abs(bound-value);
		}

		public static string Tweak(string s)
		{
			int pos;
			if ((pos = s.ToLower().IndexOf("e"))>=0)
				return Tweak(s.Substring(0,pos)) + s.Substring(pos);
				
			if (s.IndexOf(".")>=0) 
				return s + "5";
				
			return s+".5";
		}

		public static ScientificDouble Parse(string s)
		{
			return ValueOf(s);
		}

		public override bool Equals(object obj)
		{
			return CompareTo(obj) == 0;
		}

		public virtual int CompareTo(object obj)
		{
			double other;
			try
			{
				if (obj is ScientificDouble) 
					other = ((ScientificDouble)obj).DoubleValue();
				else
					other = (double)obj;
			}
			catch (InvalidCastException e) 
			{
				throw new ApplicationException("Can't compare ScientificDouble with a " + obj.GetType(), e);
			}

			double diff = value-other;
			
			if (diff < -precision)
				return -1;
			if (diff > precision)
				return 1;
			if (double.IsNaN(value) && double.IsNaN(other)) 
				return 0;
			if (double.IsNaN(value))
				return 1;
			if (double.IsNaN(other)) 
				return -1;
			return 0;
		}

		public override string ToString()
		{
			return value.ToString();
		}

		public override int GetHashCode()
		{
			return (int)value;
		}

		public virtual double DoubleValue()
		{
			return value;
		}

		public virtual float FloatValue()
		{
			return (float)value;
		}

		public virtual long LongValue()
		{
			return (long)value;
		}

		public virtual int IntValue() 
		{
			return (int)value;
		}
	}
}