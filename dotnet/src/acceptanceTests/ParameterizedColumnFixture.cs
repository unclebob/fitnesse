using fit;

namespace fitnesse.acceptanceTests
{
	public class ParameterizedColumnFixture : ColumnFixture
	{
		public string StringField;
		public int IntField;
		public bool BoolField;
		public Person PersonField;

		public override void Execute()
		{
			StringField = (string) new TypeAdapter(typeof(string)).Parse(Args[0]);
			IntField = (int) new TypeAdapter(typeof(int)).Parse(Args[1]);
			BoolField = (bool) new TypeAdapter(typeof(bool)).Parse(Args[2]);
			PersonField = (Person) new TypeAdapter(typeof(Person)).Parse(Args[3]);
		}
	}
}