// Copyright (C) 2003,2004 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
namespace fit
{
	public class Counts
	{
		private int right = 0;
		private int wrong = 0;
		private int ignores = 0;
		private int exceptions = 0;

		public override string ToString()
		{
			return
			right + " right, " +
			wrong + " wrong, " +
			ignores + " ignored, " +
			exceptions + " exceptions";
		}

		public virtual void Tally(Counts source)
		{
			right += source.right;
			wrong += source.wrong;
			ignores += source.ignores;
			exceptions += source.exceptions;
		}

		public int Right
		{
			get { return right; }
			set { right = value; }
		}

		public int Wrong
		{
			get { return wrong; }
			set { wrong = value; }
		}

		public int Exceptions
		{
			get { return exceptions; }
			set { exceptions = value; }
		}

		public int Ignores
		{
			get { return ignores; }
			set { ignores = value; }
		}
	}

}