package com.joyent.manta.vfs;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.operations.AbstractFileOperationProvider;
import org.apache.commons.vfs2.operations.FileOperation;

import java.util.Collection;

/**
 * {@link FileOperation} provider that allows VFS to support file system
 * specific operations that aren't globally shared.
 *
 * @author <a href="https://github.com/dekobon">Elijah Zupancic</a>
 * @since 1.0.0
 */
public class MantaFileOperationsProvider extends AbstractFileOperationProvider {
    /**
     * Creates new instance.
     */
    public MantaFileOperationsProvider() {
    }

    @Override
    protected void doCollectOperations(final Collection<Class<? extends FileOperation>> availableOperations,
                                       final Collection<Class<? extends FileOperation>> resultList,
                                       final FileObject file) throws FileSystemException {
        // no operations defined yet
    }

    @Override
    protected FileOperation instantiateOperation(final FileObject file,
                                                 final Class<? extends FileOperation> operationClass)
            throws FileSystemException {
        // define operations here
        final String msg = String.format("Operation [%s] is not available for file: %s",
                operationClass.getName(), file.getName().getPath());
        throw new FileSystemException(msg);
    }
}
