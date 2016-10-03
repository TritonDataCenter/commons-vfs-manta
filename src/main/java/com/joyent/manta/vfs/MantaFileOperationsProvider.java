package com.joyent.manta.vfs;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.operations.AbstractFileOperationProvider;
import org.apache.commons.vfs2.operations.FileOperation;

import java.util.Collection;

/**
 * @author <a href="https://github.com/dekobon">Elijah Zupancic</a>
 */
public class MantaFileOperationsProvider extends AbstractFileOperationProvider {
    public MantaFileOperationsProvider() {
    }

    @Override
    protected void doCollectOperations(final Collection<Class<? extends FileOperation>> availableOperations,
                                       final Collection<Class<? extends FileOperation>> resultList,
                                       final FileObject file) throws FileSystemException {

    }

    @Override
    protected FileOperation instantiateOperation(final FileObject file, Class<? extends FileOperation> operationClass)
            throws FileSystemException {
        return null;
    }
}
