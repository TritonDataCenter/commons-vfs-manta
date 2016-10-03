import com.joyent.manta.config.ConfigContext;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;

public class MantaStorLister {
    public static void main(final String[] argv) throws Exception {
        FileSystemManager fileSystemManager = VFS.getManager();
        FileObject object = fileSystemManager.resolveFile(String.format("manta:///%s/stor",
                ConfigContext.deriveHomeDirectoryFromUser(System.getenv("MANTA_USER"))));

        for (FileObject child : object.getChildren()) {
            System.out.println(child.getName().getBaseName());
        }

        fileSystemManager.closeFileSystem(object.getFileSystem());
    }
}