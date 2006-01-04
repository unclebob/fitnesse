// Modified or written by Object Mentor, Inc. for inclusion with FitNesse.
// Copyright (c) 2002 Cunningham & Cunningham, Inc.
// Released under the terms of the GNU General Public License version 2 or later.
// Copyright (c) 2002, 2003 Cunningham & Cunningham, Inc.
// Released under the terms of the GNU General Public License version 2 or later.

using System;
using System.IO;
using System.Text;

namespace fit
{
	public class Parse
	{
		private string leader;
		private string tag;
		private string body;
		private string end;
		private string trailer;

		private Parse more;
		private Parse parts;

		public string Leader
		{
			get { return leader; }
		}

        public string Tag {
            get { return tag; }
        }

        // added
        public string End {
            get { return end; }
        }

        public string Body
		{
			get { return body; }
		}

		public void SetBody(string val)
		{
			body = val;
		}

		public string Trailer
		{
            get { return trailer; }
            //added
            set { trailer = value; }
        }

		public Parse More
		{
			get { return more; }
			set { more = value; }
		}

		public Parse Parts
		{
			get { return parts; }
		}

        //added
        public Parse(string theTag, string theEnd, string theLeader, string theBody, Parse theParts) {
            tag = theTag;
            end = theEnd;
            leader = theLeader;
            body = theBody;
            parts = theParts;
        }

		public Parse(string tag, string body, Parse parts, Parse more)
		{
			this.leader = "\n";
			this.tag = "<" + tag + ">";
			this.body = body;
			this.end = "</" + tag + ">";
			this.trailer = "";
			this.parts = parts;
			this.more = more;
		}

		public static string[] Tags = {"table", "tr", "td"};

		public Parse(string text) : this(text, Tags, 0, 0)
		{}

		public Parse(string text, string[] tags) : this(text, tags, 0, 0)
		{}

		private static string Substring(string text, int startIndexInclusive, int endIndexExclusive)
		{
			return text.Substring(startIndexInclusive, endIndexExclusive - startIndexInclusive);
		}

		private static int ProtectedIndexOf(string text, string searchValue, int offset, string tag)
		{
			int result = text.IndexOf(searchValue, offset);
			if (result < 0)
				throw new ApplicationException("Can't find tag: " + tag);
			else
				return result;
		}

		public Parse(string text, string[] tags, int level, int offset)
		{
			string lc = text.ToLower();
			string target = tags[level].ToLower();

			int startTag = ProtectedIndexOf(lc, "<" + target, 0, target);
			int endTag = ProtectedIndexOf(lc, ">", startTag, target) + 1;
			int startEnd = ProtectedIndexOf(lc, "</" + target, endTag, target);
			int endEnd = ProtectedIndexOf(lc, ">", startEnd, target) + 1;
			int startMore = lc.IndexOf("<" + target, endEnd);

			leader = Substring(text, 0, startTag);
			tag = Substring(text, startTag, endTag);
			body = Substring(text, endTag, startEnd);
			end = Substring(text, startEnd, endEnd);
			trailer = text.Substring(endEnd);

			if (level + 1 < tags.Length)
			{
				parts = new Parse(body, tags, level + 1, offset + endTag);
				body = null;
			}

			if (startMore >= 0)
			{
				more = new Parse(trailer, tags, level, offset + endEnd);
				trailer = null;
			}
		}

		public virtual int Size
		{
			get { return more == null ? 1 : more.Size + 1; }
		}

		public virtual Parse Last
		{
			get { return more == null ? this : more.Last; }
		}

		public virtual Parse Leaf
		{
			get { return parts == null ? this : parts.Leaf; }
		}

		public virtual Parse At(int i)
		{
			return i == 0 || more == null ? this : more.At(i - 1);
		}

		public virtual Parse At(int i, int j)
		{
			return At(i).parts.At(j);
		}

		public virtual Parse At(int i, int j, int k)
		{
			return At(i, j).parts.At(k);
		}

		public virtual string Text
		{
			get
			{
				string result = UnEscape(UnFormat(body));
				foreach (char c in result)
				{
					if (c != ' ')
						return result;
				}
				return "";
			}
		}

		public static string UnFormat(string s)
		{
			return StripMarkup(s);
		}

		private static string StripMarkup(string s)
		{
			int i = 0, j;
			while ((i = s.IndexOf('<', i)) >= 0)
			{
				if ((j = s.IndexOf('>', i + 1)) > 0)
					s = Substring(s, 0, i) + s.Substring(j + 1);
				else
					break;
			}
			return s;
		}

		public static string UnEscape(string s)
		{
			int i = -1, j;
			while ((i = s.IndexOf('&', i + 1)) >= 0)
			{
				if ((j = s.IndexOf(';', i + 1)) > 0)
				{
					string from = Substring(s, i + 1, j).ToLower();
					string to = null;
					if ((to = Replacement(from)) != null)
						s = Substring(s, 0, i) + to + s.Substring(j + 1);
				}
			}
			return s;
		}

		public static string Replacement(string from)
		{
			if (from == "lt")
				return "<";
			else if (from == "gt")
				return ">";
			else if (from == "amp")
				return "&";
			else if (from == "nbsp")
				return " ";
			else
				return null;
		}

		public virtual void AddToTag(string text)
		{
			int last = tag.Length - 1;
			tag = Substring(tag, 0, last) + text + ">";
		}

		public virtual void AddToBody(string text)
		{
			body = body + text;
		}

		public virtual void Print(TextWriter output)
		{
			output.Write(ToString());
		}

		public override string ToString()
		{
			StringBuilder builder = new StringBuilder();
			return BuildString(builder).ToString();
		}

		private StringBuilder BuildString(StringBuilder builder)
		{
			builder.Append(leader);
			builder.Append(tag);
			if (parts != null)
				builder.Append(parts.BuildString(new StringBuilder()));
			else
				builder.Append(body);
			builder.Append(end);
			if (more != null)
			{
				return builder.Append(more.BuildString(new StringBuilder()));
			}
			else
			{
				builder.Append(trailer);
				return builder;
			}
		}

		public static int FootnoteFiles = 0;

		public virtual string Footnote
		{
			get
			{
				if (FootnoteFiles >= 25)
					return "[-]";
				else
				{
					try
					{
						int thisFootnote = ++FootnoteFiles;
						string html = "footnotes/" + thisFootnote + ".html";
						FileInfo file = new FileInfo("Reports/" + html);

						// Create the Reports directory if not exists
						string directory = file.DirectoryName;
						if (!Directory.Exists(directory))
							Directory.CreateDirectory(directory);
						else if (file.Exists)
							file.Delete();

						TextWriter output = file.CreateText();
						Print(output);
						output.Close();
						return string.Format("<a href={0}>[{1}]</a>", file.FullName, thisFootnote);
					}
					catch (IOException)
					{
						return "[!]";
					}
				}
			}
		}
	}
}