using System;
using System.Collections.Generic;

#region Copyright
/*
<xsl:value-of select="$copyright"/>
*/
@endregion

namespace <xsl:value-of select="$namespace"/>
{
<![CDATA[
	/// <summary>
	/// Changes the content-type of a stream.
	/// This interface can be implemented by a custom Stream class passed as a 'body' parameter to the API.
	/// </summary>
	public abstract class HasContentType
	{
		/// <summary>
		/// What content-type should be used instead of the default 'application/octet-stream'.
		/// </summary>
		public abstract string GetContentType();
	}
]]>
}