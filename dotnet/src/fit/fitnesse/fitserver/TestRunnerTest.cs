using System;
using System.IO;
using fit;
using NUnit.Framework;

namespace fitnesse.fitserver
{
	[TestFixture]
	public class TestRunnerTest
	{
		private TestRunner runner;

		[SetUp]
		public void SetUp()
		{
			runner = new TestRunner();
		}

		[TearDown]
		public void TearDown()
		{
			if(runner.cacheWriter != null)
				runner.cacheWriter.Close();
			if(File.Exists(runner.cacheFilename))
				File.Delete(runner.cacheFilename);
		}

		[Test]
		public void TestMakeHttpRequest()
		{
			runner.pageName = "SomePageName";
			string request = runner.MakeHttpRequest();
			Assert.AreEqual("GET /SomePageName?responder=fitClient HTTP/1.1\r\n\r\n", request);

			runner.usingDownloadedPaths = true;
			request = runner.MakeHttpRequest();
			Assert.AreEqual("GET /SomePageName?responder=fitClient&includePaths=yes HTTP/1.1\r\n\r\n", request);
		}

		[Test]
		public void TestParseArgs()
		{
			bool result = runner.ParseArgs(new string[] {});
			Assert.IsFalse(result);

			result = runner.ParseArgs(new string[] {"localhost", "8081", "SomeTestPage"});
			Assert.IsTrue(result);
			Assert.AreEqual("localhost", runner.host);
			Assert.AreEqual(8081, runner.port);
			Assert.AreEqual("SomeTestPage", runner.pageName);
		}

		[Test]
		public void TestParseArgsWithOptions()
		{
			bool result = runner.ParseArgs(new string[] {"-v", "-debug", "-results", "stdout", "localhost", "8081", "SomeTestPage"});
			Assert.IsTrue(runner.verbose);
			Assert.IsTrue(runner.debug);
			Assert.IsTrue(result);
			Assert.AreEqual("localhost", runner.host);
			Assert.AreEqual(8081, runner.port);
			Assert.AreEqual("SomeTestPage", runner.pageName);
			Assert.AreEqual("stdout", runner.cacheFilename);
		}

		[Test]
		public void TestEstablishCacheStream_Temp()
		{
			runner.CreateCacheStream("");
			Assert.IsTrue(runner.cacheFilename.StartsWith("FitNesse"));
			Assert.IsTrue(runner.cacheWriter is StreamWriter);
			Assert.IsTrue(runner.deleteCacheOnExit);
		}

		[Test]
		public void TestEstablishCacheStream_Stdout()
		{
			runner.CreateCacheStream("stdout");
			Assert.AreSame(Console.Out, runner.cacheWriter);
			Assert.IsFalse(runner.deleteCacheOnExit);
		}

		[Test]
		public void TestEstablishCacheStream_File()
		{
			runner.CreateCacheStream("MyFile.results");
			Assert.AreEqual("MyFile.results", runner.cacheFilename);
			Assert.IsTrue(runner.cacheWriter is StreamWriter);
			Assert.IsFalse(runner.deleteCacheOnExit);
		}

		[Test]
		public void TestCacheResults()
		{
			PageResult results = new PageResult("TestPage", new Counts(1, 2, 3, 4), "content");
			StringWriter writer = new StringWriter();
			runner.cacheWriter = writer;
			runner.CacheResults(results);
			writer.Close();
			string text = writer.ToString();
			Assert.AreEqual(results.ToString() + "\n", text);
		}

		[Test]
		public void TestCacheFinalCount()
		{
			StringWriter writer = new StringWriter();
			runner.cacheWriter = writer;
			runner.CacheFinalCount(new Counts(1, 2, 3, 4));	
			writer.Close();
			string text = writer.ToString();
			Assert.AreEqual("00000000000000000001000000000200000000030000000004", text);
		}

		[Test]
		public void TestVerbose()
		{
			StringWriter output = new StringWriter();
			runner.verbose = false;
			runner.output = output;

			runner.HandleFinalCount(new Counts(1, 2, 3, 4));
			Assert.AreEqual("", output.ToString());

			
			runner.verbose = true;
			runner.HandleFinalCount(new Counts(1, 2, 3, 4));
			string expected = "\r\n" +
					"Test Pages: 0 right, 0 wrong, 0 ignored, 0 exceptions\r\n" +
					"Assertions: 1 right, 2 wrong, 3 ignored, 4 exceptions\r\n";
			Assert.AreEqual(expected, output.ToString());
		}
	}
}
