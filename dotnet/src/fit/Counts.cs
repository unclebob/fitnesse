// Modified or written by Object Mentor, Inc. for inclusion with FitNesse.
// Copyright (c) 2002 Cunningham & Cunningham, Inc.
// Released under the terms of the GNU General Public License version 2 or later.
namespace fit
{
	public class Counts
	{
		private int right = 0;
		private int wrong = 0;
		private int ignores = 0;
		private int exceptions = 0;

		public Counts()
		{}

		public Counts(int right, int wrong, int ignores, int exceptions)
		{
			this.right = right;
			this.wrong = wrong;
			this.ignores = ignores;
			this.exceptions = exceptions;
		}

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

		public override bool Equals(object o)
		{
			if(o is Counts)
			{
				Counts that = o as Counts;
				if(this.right == that.right
					&& this.wrong == that.wrong
					&& this.ignores == that.ignores
					&& this.exceptions == that.exceptions)
					return true;
				else
					return false;
			}
			else
				return false;
		}

		public override int GetHashCode()
		{
			return right * 1000 + wrong * 100 + ignores * 10 + exceptions;
		}

		public void TallyPageCounts(Counts counts)
		{
			if(counts.wrong > 0)
				wrong += 1;
			else if(counts.exceptions > 0)
				exceptions += 1;
			else if(counts.ignores > 0 && counts.right == 0)
				ignores += 1;
			else
				right += 1;
		}
	}

}