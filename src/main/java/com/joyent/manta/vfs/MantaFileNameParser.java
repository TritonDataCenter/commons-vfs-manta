package com.joyent.manta.vfs;

import com.joyent.manta.client.MantaClient;
import com.joyent.manta.config.ConfigContext;
import com.joyent.manta.org.apache.commons.lang3.ObjectUtils;
import com.joyent.manta.org.apache.commons.lang3.StringUtils;
import com.joyent.manta.org.apache.commons.lang3.Validate;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.provider.AbstractFileNameParser;
import org.apache.commons.vfs2.provider.VfsComponentContext;

import java.net.URI;

import static com.joyent.manta.client.MantaClient.SEPARATOR;

/**
 * @author <a href="https://github.com/dekobon">Elijah Zupancic</a>
 */
public class MantaFileNameParser extends AbstractFileNameParser {
    private static final String DOUBLE_SEPARATOR = SEPARATOR + SEPARATOR;
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
