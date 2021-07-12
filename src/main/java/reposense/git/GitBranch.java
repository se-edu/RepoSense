package reposense.git;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Ref;

import reposense.git.exception.GitBranchException;
import reposense.system.LogsManager;

/**
 * Contains git branch related functionalities.
 * Git branch is responsible for list, create, or delete branches.
 */
public class GitBranch {
    private static final Logger logger = LogsManager.getLogger(GitBranch.class);
    private static final String MESSAGE_MISSING_REPOSITORY =
            "The directory %s does not exist or is not a Git repository.";

    /**
     * Returns the current working branch of the repository at {@code root}.
     */
    public static String getCurrentBranch(String root) throws GitBranchException {
        String branch;

        try {
            Git git = Git.open(new File(root));
            List<Ref> gitRefs = git.getRepository().getRefDatabase().getRefs();

            if (gitRefs.size() == 0) {
                // An empty repository does not have any refs
                return null;
            }

            return git.getRepository().getBranch();
        } catch (IOException ioe) {
            logger.log(Level.SEVERE, String.format(MESSAGE_MISSING_REPOSITORY, root), ioe);
            throw new GitBranchException(ioe);
        }
    }
}
