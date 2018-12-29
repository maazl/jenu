package jenu.model;

/** Events that can happen during processing. Usually they are stored as EnumSet&lt;EventType&gt;.
 * The identifiers used are directly displayed in the UI, except that underscores are replaced by spaces.
 */
public enum MessageType
{
	/** The URL results in an HTTP redirect. */
	Redirect,
	/** HTTP error code returned by server. */
	HTTP_error,
	/** I/O error during access to the URL. */
	IO_error,
	/** The Page has severe syntax errors. */
	Parse_error,
	/** The Anchor in the Link does not exist in the Traget page. */
	Bad_anchor,
	/** The URL is not well formed. */
	URL_error,
	/** Unhandled exception during processing. */
	Internal_error
}
