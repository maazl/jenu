package jenu.worker;

/** Events that can happen during processing. Usually they are stored as EnumSet&lt;EventType&gt;.
 * The identifiers used are directly displayed in the UI, except that underscores are replaced by spaces.
 */
public enum EventType
{
	Redirect,
	IO_error,
	HTML_parse_error,
	CSS_parse_error,
	Bad_anchor,
	HTTP_error,
	URL_error,
	Internal_error
}
