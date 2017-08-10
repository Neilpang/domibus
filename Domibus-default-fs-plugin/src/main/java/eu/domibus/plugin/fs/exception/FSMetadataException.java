package eu.domibus.plugin.fs.exception;

/**
 * FSMetadataException
 *
 * @author FERNANDES Henrique, GONCALVES Bruno
 */
public class FSMetadataException extends Exception {

    /**
     * Creates a new <code>FSMetadataException/code>.
     * @param   message   the detail message. The detail message is saved for
     *          later retrieval by the {@link #getMessage()} method.
     */
    public FSMetadataException(String message) {
        super(message);
    }

    /**
     * Creates a new <code>FSMetadataException</code>.
     *
     * @param  message the detail message (which is saved for later retrieval
     *         by the {@link #getMessage()} method).
     * @param  cause the cause (which is saved for later retrieval by the
     *         {@link #getCause()} method).  (A <tt>null</tt> value is
     *         permitted, and indicates that the cause is nonexistent or
     *         unknown.)
     */
    public FSMetadataException(String message, Throwable cause) {
        super(message, cause);
    }

}