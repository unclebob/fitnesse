package fitnesse.responders;

import fitnesse.wiki.WikiPageProperty;

public class WikiImportProperty extends WikiPageProperty
{
	public static final String PROPERTY_NAME = "WikiImport";

	private WikiImportProperty()
	{}

	public WikiImportProperty(String source)
	{
		set("Source", source);
	}

	public String getSource()
	{
		return get("Source");
	}        

	public boolean isRoot()
	{
		return has("IsRoot");
	}

	public void setRoot(boolean value)
	{
		if(value)
			set("IsRoot");
		else
			remove("IsRoot");
	}

	public static WikiImportProperty createFrom(WikiPageProperty property)
	{
		if(property.has(PROPERTY_NAME))
		{
			WikiImportProperty importProperty = new WikiImportProperty();
			WikiPageProperty rawImportProperty = property.getProperty(PROPERTY_NAME);
			importProperty.set("Source", rawImportProperty.getProperty("Source"));
			if(rawImportProperty.has("IsRoot"))
				importProperty.set("IsRoot", rawImportProperty.getProperty("IsRoot"));

			return importProperty;
		}
		else
			return null;
	}

	public void addTo(WikiPageProperty rootProperty)
	{
		rootProperty.set(PROPERTY_NAME, this);
	}
}
