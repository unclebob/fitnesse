// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.wiki;

import java.util.*;

public class PageVersionPruner
{
	public static int daysTillVersionsExpire = 14;

	public static void pruneVersions(FileSystemPage page, Collection versions) throws Exception
	{
		List versionsList = makeSortedVersionList(versions);
		if(versions.size() > 0)
		{
			VersionInfo lastVersion = (VersionInfo) versionsList.get(versionsList.size() - 1);
			GregorianCalendar expirationDate = makeVersionExpirationDate(lastVersion);
			for(Iterator iterator = versionsList.iterator(); iterator.hasNext();)
			{
				VersionInfo version = (VersionInfo) iterator.next();
				removeVersionIfExpired(page, version, expirationDate);
			}
		}
	}

	private static List makeSortedVersionList(Collection versions) throws Exception
	{
		List versionsList = new ArrayList(versions);
		Collections.sort(versionsList);
		return versionsList;
	}

	private static GregorianCalendar makeVersionExpirationDate(VersionInfo lastVersion) throws Exception
	{
		Date dateOfLastVersion = lastVersion.getCreationTime();
		GregorianCalendar expirationDate = new GregorianCalendar();
		expirationDate.setTime(dateOfLastVersion);
		expirationDate.add(Calendar.DAY_OF_MONTH, -(daysTillVersionsExpire));
		return expirationDate;
	}

	private static void removeVersionIfExpired(FileSystemPage page, VersionInfo version, GregorianCalendar expirationDate) throws Exception
	{
		Calendar thisDate = new GregorianCalendar();
		thisDate.setTime(version.getCreationTime());
		if(thisDate.before(expirationDate) || thisDate.equals(expirationDate))
			page.removeVersion(version.getName());
	}
}
