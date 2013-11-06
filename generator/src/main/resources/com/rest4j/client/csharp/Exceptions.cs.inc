using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

#region Copyright
/*
 * <xsl:value-of select="rest4j:javadocEscape0($copyright)"/>
 */
#endregion

namespace <xsl:value-of select="$namespace"/>
<![CDATA[{
    /// <summary>
    /// Thrown by the Request.Execute() when an erroneous status code returned.
    /// </summary>
    public class StatusException : Exception
    {
        public StatusException(int code, String description)
            : base(String.Format("Unexpected status code: {0} {1}", code, description))
        {
            StatusCode = code;
            StatusDescription = description;
        }

        public int StatusCode
        {
            get;
            set;
        }
        public String StatusDescription
        {
            get;
            set;
        }
    }

    /// <summary>
    /// Thrown when a response was expected but not returned.
    /// </summary>
    public class AbsentResponseException: Exception
    {
        public AbsentResponseException() : base("Response is absent")
        { }
    }
}
]]>