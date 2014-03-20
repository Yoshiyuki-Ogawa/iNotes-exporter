/**
 *
 */
package fr.cedrik.inotes.fs;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.cedrik.inotes.BaseINotesMessage;
import fr.cedrik.inotes.Folder;
import fr.cedrik.inotes.FoldersList;
import fr.cedrik.inotes.util.Charsets;

/**
 * Usage:
 * <ul><li>{@link #openFolder(String)}</li>
 * <li>{@link #openFile(String, BaseINotesMessage)}</li>
 * <li>{@link #write(BaseINotesMessage, Iterator)}</li>
 * <li>{@link #closeFile(BaseINotesMessage)} (in {@code finally} block!)</li>
 * <li>{@link #closeFolder()} (in {@code finally} block!)</li></ul>
 *
 * @author C&eacute;drik LIME
 */
public abstract class BaseZipWriter {
	protected final Logger logger = LoggerFactory.getLogger(this.getClass());

	protected ZipOutputStream outStream = null;
	protected Writer outWriter = null;
	protected ZipEntry currentEntry = null;
	protected String baseName = "";

	public BaseZipWriter(ZipOutputStream out, String baseName) {
		this.outStream = out;
		baseName = StringUtils.defaultString(baseName);
		if ( ! baseName.isEmpty() && ! baseName.endsWith("/")) {
			baseName += '/';
		}
		this.baseName = baseName;
	}

	public abstract void openFolder(String folderChainStr) throws IOException;

	public abstract void openFile(String folderChainStr, BaseINotesMessage message) throws IOException;

	protected void newZipEntry(String folderChainStr, BaseINotesMessage message) throws IOException {
		currentEntry = new ZipEntry(computeZipMailFileName(folderChainStr, message));
		if (message != null && message.getDate() != null) {
			currentEntry.setTime(message.getDate().getTime());
		}
		outStream.putNextEntry(currentEntry);
		outWriter = new BufferedWriter(new OutputStreamWriter(outStream, Charsets.US_ASCII), 32*1024);
	}

//	protected abstract ZipEntry getMBoxFile(BaseINotesMessage message);
	/**
	 * You can use {@link BaseZipWriter#computeZipFolderName(Folder, FoldersList, String)} for base folder handling
	 */
	protected abstract String computeZipMailFileName(String folderChainStr, BaseINotesMessage message);

	public final void write(BaseINotesMessage message, Iterator<String> mime) throws IOException {
		logger.debug("Writing message {}", message);
		writeMIME(outWriter, message, mime);
	}

	public void flush() throws IOException {
		if (outWriter != null) {
			outWriter.flush();
		}
	}

	public abstract void closeFile(BaseINotesMessage message) throws IOException;

	public abstract void closeFolder() throws IOException;

	protected void closeZipEntry() throws IOException {
		if (outWriter != null) {
			outWriter.flush(); // very important in order to not corrupt the ZIP stream!
		}
//		IOUtils.closeQuietly(outWriter);// Do not close stream, it is the caller's responsability
		outStream.closeEntry();
		currentEntry = null;
	}

	protected abstract void writeMIME(Writer mbox, BaseINotesMessage message, Iterator<String> mime) throws IOException;//FIXME replace BaseINotesMessage with "extraHeaders"?

	/**
	 * @return RFC-compliant new-line char
	 */
	protected String newLine() {
		return "\n";
	}
	/**
	 * @return RFC-compliant new-line char
	 */
	protected Writer newLine(Writer out) throws IOException {
		return out.append(newLine());
	}
}
