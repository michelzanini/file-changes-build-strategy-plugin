package org.jenkinsci.plugins.buildstrategy;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.model.TaskListener;
import jenkins.branch.BranchBuildStrategy;
import jenkins.scm.api.SCMFile;
import jenkins.scm.api.SCMFileSystem;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.SCMRevision;
import jenkins.scm.api.SCMSource;
import org.apache.commons.codec.digest.DigestUtils;

import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class AbstractFileChangedMd5BuildStrategy extends BranchBuildStrategy {

  protected static final Logger LOGGER = Logger.getLogger(AbstractFileChangedMd5BuildStrategy.class.getName());

  /**
   * The file path.
   */
  @NonNull
  private final String filePath;

  /**
   * The MD5 hash for the file at the file path.
   */
  @NonNull
  private final String md5Hash;

  public AbstractFileChangedMd5BuildStrategy(@NonNull String filePath, @NonNull String md5Hash) {
    this.filePath = filePath;
    this.md5Hash = md5Hash;
  }

  /**
   * Gets the file path.
   *
   * @return the file path.
   */
  @NonNull
  public String getFilePath() {
    return filePath;
  }

  /**
   * Gets the MD5 hash.
   *
   * @return the MD5 hash.
   */
  @NonNull
  public String getMd5Hash() {
    return md5Hash;
  }

  @Override
  public boolean isAutomaticBuild(@NonNull SCMSource scmSource,
                                  @NonNull SCMHead scmHead,
                                  @NonNull SCMRevision currRevision,
                                  SCMRevision lastBuiltRevision,
                                  SCMRevision lastSeenRevision,
                                  @NonNull TaskListener taskListener) {

    try {
      return checkFileHasChanged(scmSource, scmHead, currRevision, taskListener);
    } catch (Exception e) {
      LOGGER.log(Level.SEVERE, "Exception", e);
      return true;
    }
  }

  protected SCMHead overrideSCMHead(SCMHead scmHead) {
    return scmHead;
  }

  protected SCMRevision overrideSCMRevision(SCMHead scmHead, SCMRevision scmRevision) {
    return scmRevision;
  }

  protected boolean checkFileHasChanged(@NonNull SCMSource scmSource,
                                        @NonNull SCMHead scmHead,
                                        @NonNull SCMRevision currRevision,
                                        @NonNull TaskListener taskListener)
                                        throws Exception {

    if (filePath.isEmpty() || md5Hash.isEmpty()) {
      taskListener.error("File path or commit hash are missing");
      return true;
    }

    scmHead = this.overrideSCMHead(scmHead);
    currRevision = this.overrideSCMRevision(scmHead, currRevision);

    SCMFileSystem fileSystem = SCMFileSystem.of(scmSource, scmHead, currRevision);

    if (fileSystem == null) {
      taskListener.error("Error retrieving SCMFileSystem");
      return true;
    }

    SCMFile file = fileSystem.child(filePath);

    if (file == null) {
      taskListener.error("File has not been found at path: '%s'", filePath);
      return true;
    }

    String currentMd5 = DigestUtils.md5Hex(file.contentAsBytes());
    boolean matchesMd5 = md5Hash.equals(currentMd5);

    if (!matchesMd5) {
      taskListener.getLogger().println(String.format("File at path '%s' has been changed. " +
        "MD5 hash did not match. To match, the MD5 should have been %s, but the actual MD5 was %s. " +
        "Build will be skipped.", filePath, md5Hash, currentMd5));
    }

    return matchesMd5;
  }

}
