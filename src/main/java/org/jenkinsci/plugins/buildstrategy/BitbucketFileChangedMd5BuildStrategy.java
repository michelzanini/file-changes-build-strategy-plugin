package org.jenkinsci.plugins.buildstrategy;

import com.cloudbees.jenkins.plugins.bitbucket.BitbucketSCMSource;
import com.cloudbees.jenkins.plugins.bitbucket.BranchSCMHead;
import com.cloudbees.jenkins.plugins.bitbucket.PullRequestSCMHead;
import com.cloudbees.jenkins.plugins.bitbucket.PullRequestSCMRevision;
import com.cloudbees.jenkins.plugins.bitbucket.api.BitbucketRepositoryType;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import jenkins.branch.BranchBuildStrategyDescriptor;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.SCMRevision;
import jenkins.scm.api.SCMSourceDescriptor;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;

public class BitbucketFileChangedMd5BuildStrategy extends AbstractFileChangedMd5BuildStrategy {

  @DataBoundConstructor
  public BitbucketFileChangedMd5BuildStrategy(@NonNull String filePath, @NonNull String md5Hash) {
    super(filePath, md5Hash);
  }

  @Override
  protected SCMHead overrideSCMHead(SCMHead scmHead) {
    if (scmHead instanceof PullRequestSCMHead) {
      String branchName = ((PullRequestSCMHead) scmHead).getBranchName();
      return new BranchSCMHead(branchName, BitbucketRepositoryType.GIT);
    }
    return scmHead;
  }

  @Override
  protected SCMRevision overrideSCMRevision(SCMHead scmHead, SCMRevision scmRevision) {
    if (scmRevision instanceof PullRequestSCMRevision) {
      return ((PullRequestSCMRevision<?>) scmRevision).getPull();
    }
    return scmRevision;
  }

  @Symbol("bitbucketFileChangedMd5BuildStrategy")
  @Extension
  public static class DescriptorImpl extends BranchBuildStrategyDescriptor {

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public String getDisplayName() {
      return Messages.BitbucketFileChangedMd5BuildStrategy_displayName();
    }

    @Override
    public boolean isApplicable(@Nonnull SCMSourceDescriptor sourceDescriptor) {
      return BitbucketSCMSource.DescriptorImpl.class.isAssignableFrom(sourceDescriptor.getClass());
    }

  }

}
