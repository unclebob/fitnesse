package fitnesse.updates;

import fitnesse.util.*;
import fitnesse.wiki.*;
import org.w3c.dom.*;

public class SymLinkPropertyFormatUpdate extends PageTraversingUpdate
{
	public SymLinkPropertyFormatUpdate(Updater updater)
	{
		super(updater);
	}

	public void processPage(WikiPage currentPage) throws Exception
	{
		try
		{
			FileSystemPage page = (FileSystemPage) currentPage;
			String propertiesContent = FileUtil.getFileContent(page.getFileSystemPath() + FileSystemPage.propertiesFilename);
			if(propertiesContent.contains("<symbolicLink>"))
				fixPropertiesFile(page, propertiesContent);
		}
		catch(Exception e)
		{
			// continue to next page
		}
	}

	private void fixPropertiesFile(FileSystemPage page, String propertiesContent) throws Exception
	{
		PageData data = page.getData();
		WikiPageProperty symLinkProperty = getSymbolicLinkProperty(data);
		data.getProperties().remove("symbolicLink");

		Document document = XmlUtil.newDocument(propertiesContent);
		NodeList oldLinkElements = document.getElementsByTagName("symbolicLink");
		for(int i = 0; i < oldLinkElements.getLength(); i++)
		{
			Element oldSymLink = (Element) oldLinkElements.item(i);
			String name = XmlUtil.getLocalTextValue(oldSymLink, "name");
			String path = XmlUtil.getLocalTextValue(oldSymLink, "path");
			symLinkProperty.set(name, path);
		}

		page.commit(data);
	}

	private WikiPageProperty getSymbolicLinkProperty(PageData data) throws Exception
	{
		WikiPageProperties properties = data.getProperties();
		WikiPageProperty symLinkProperty = properties.getProperty(SymbolicPage.PROPERTY_NAME);
		if(symLinkProperty == null)
			symLinkProperty = properties.set(SymbolicPage.PROPERTY_NAME);
		return symLinkProperty;
	}

	public String getName()
	{
		return "SymLinkPropertyFormatUpdate";
	}

	public String getMessage()
	{
		return "Updating the format of SybolicLink properties";
	}
}
