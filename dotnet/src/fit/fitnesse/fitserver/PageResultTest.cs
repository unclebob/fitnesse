
using fit;
using NUnit.Framework;

namespace fitnesse.fitserver
{
	[TestFixture]
	public class PageResultTest
	{
		[Test]
		public void TestToString()
		{
			PageResult result = new PageResult("PageTitle", new Counts(1, 2, 3, 4), "content");
			Assert.AreEqual("PageTitle\n1 right, 2 wrong, 3 ignored, 4 exceptions\ncontent", result.ToString());
		}

		[Test]
		public void TestParse()
		{
			Counts counts = new Counts(1, 2, 3, 4);
			PageResult result = new PageResult("PageTitle", counts, "content");
			PageResult parsedResult = PageResult.Parse(result.ToString());
			Assert.AreEqual("PageTitle", parsedResult.Title());
			Assert.AreEqual(counts, parsedResult.Counts());
			Assert.AreEqual("content", parsedResult.Content());
		}

	}
}
