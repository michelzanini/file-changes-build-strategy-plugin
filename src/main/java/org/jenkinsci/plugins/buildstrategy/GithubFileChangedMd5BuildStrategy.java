package org.jenkinsci.plugins.buildstrategy;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import jenkins.branch.BranchBuildStrategyDescriptor;
import jenkins.plugins.git.AbstractGitSCMSource;
import jenkins.plugins.git.GitBranchSCMHead;
import jenkins.scm.api.SCMHead;
import jenkins.scm.api.SCMRevision;
import jenkins.scm.api.SCMSourceDescriptor;
import org.jenkinsci.Symbol;
import org.jenkinsci.plugins.github_branch_source.GitHubSCMSource;
import org.jenkinsci.plugins.github_branch_source.PullRequestSCMHead;
import org.jenkinsci.plugins.github_branch_source.PullRequestSCMRevision;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;

public class GithubFileChangedMd5BuildStrategy extends AbstractFileChangedMd5BuildStrategy {

  @DataBoundConstructor
  public GithubFileChangedMd5BuildStrategy(@NonNull String filePath, @NonNull String md5Hash) {
    super(filePath, md5Hash);
  }

  @Override
  protected SCMHead overrideSCMHead(SCMHead scmHead) {
    if (scmHead instanceof PullRequestSCMHead) {
      String branchName = ((PullRequestSCMHead) scmHead).getSourceBranch();
      return new GitBranchSCMHead(branchName);
    }
    return scmHead;
  }

  @Override
  protected SCMRevision overrideSCMRevision(SCMHead scmHead, SCMRevision scmRevision) {
    if (scmRevision instanceof PullRequestSCMRevision) {
      String pullHash = ((PullRequestSCMRevision) scmRevision).getPullHash();
      return new AbstractGitSCMSource.SCMRevisionImpl(scmHead, pullHash);
    }
    return scmRevision;
  }

  @Symbol("githubFileChangedMd5BuildStrategy")
  @Extension
  public static class DescriptorImpl extends BranchBuildStrategyDescriptor {

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public String getDisplayName() {
      return Messages.GithubFileChangedMd5BuildStrategy_displayName();
    }

    @Override
    public boolean isApplicable(@Nonnull SCMSourceDescriptor sourceDescriptor) {
      return GitHubSCMSource.DescriptorImpl.class.isAssignableFrom(sourceDescriptor.getClass());
    }

  }

}
