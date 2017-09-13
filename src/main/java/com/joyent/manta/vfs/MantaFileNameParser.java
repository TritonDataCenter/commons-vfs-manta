package com.joyent.manta.vfs;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.provider.AbstractFileNameParser;
import org.apache.commons.vfs2.provider.VfsComponentContext;

import java.net.URI;

import static com.joyent.manta.client.MantaClient.SEPARATOR;

/**
 * Manta specific implementation of {@link org.apache.commons.vfs2.provider.FileNameParser}.
 * This implementation normalizes all URIs / filenames processed.
 *
 * @author <a href="https://github.com/dekobon">Elijah Zupancic</a>
 * @since 1.0.0
 */
public class MantaFileNameParser extends AbstractFileNameParser {
    /**
     * Constant representing a path separator that has been repeated (e.g. //)
     */
    private static final String DOUBLE_SEPARATOR = SEPARATOR + SEPARATOR;

    /**
     * Creates a new instance.
     */
    public MantaFileNameParser() {
    }

    @Override
    public FileName parseUri(final VfsComponentContext context,
                             final FileName base,
                             final String uri) throws FileSystemException {
        final URI parsedUri = URI.create(uri);
        final String parsedPath = ObjectUtils.firstNonNull(parsedUri.getPath(), SEPARATOR);
        final String stripped;

        if (parsedPath.startsWith(DOUBLE_SEPARATOR)) {
            stripped = SEPARATOR + StringUtils.stripStart(parsedPath, SEPARATOR);
        } else {
            stripped = parsedPath;
        }

        final String normalized = FilenameUtils.normalizeNoEndSeparator(stripped, true);
        final String path;

        if (normalized == null) {
            path = SEPARATOR;
        } else {
            path = normalized;
        }

        return new MantaFileName(parsedUri.getScheme(), path, FileType.FILE_OR_FOLDER);
    }
}
