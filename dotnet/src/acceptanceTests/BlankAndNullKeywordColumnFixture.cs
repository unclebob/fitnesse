// Copyright (C) 2003,2004 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
using System;
using fit;

namespace fitnesse.acceptanceTests
{

	public class BlankAndNullKeywordColumnFixture : ColumnFixture
	{
		public string Field;
		public bool IsFieldNull() {return Field == null;}
		public bool IsFieldBlank() {return Field != null && Field.Length == 0;}
	}

	public class BlankAndNullKeywordRowFixture : RowFixture
	{
		public override object[] Query()
		{
			BlankAndNullKeywordColumnFixture domainObject1 = new BlankAndNullKeywordColumnFixture();
			domainObject1.Field = null;
			BlankAndNullKeywordColumnFixture domainObject2 = new BlankAndNullKeywordColumnFixture();
			domainObject2.Field = "";
			BlankAndNullKeywordColumnFixture domainObject3 = new BlankAndNullKeywordColumnFixture();
			domainObject3.Field = "joe";
			return new object[]{domainObject1, domainObject2, domainObject3};
		}

		public override Type GetTargetClass()
		{
			return typeof(BlankAndNullKeywordColumnFixture);
		}
	}
}
