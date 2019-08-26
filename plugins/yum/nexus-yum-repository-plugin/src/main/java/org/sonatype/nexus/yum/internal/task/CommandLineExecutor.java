/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-present Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.yum.internal.task;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.gossip.Level;
import org.sonatype.gossip.support.LoggingOutputStream;
import org.sonatype.nexus.configuration.application.ApplicationDirectories;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toSet;

/**
 * @since yum 3.0
 */
@Named
@Singleton
public class CommandLineExecutor
{

  private static final Logger LOG = LoggerFactory.getLogger(CommandLineExecutor.class);

  private final Set<String> allowedExecutables = new HashSet<>();

  private final ApplicationDirectories applicationDirectories;

  @Inject
  public CommandLineExecutor(final ApplicationDirectories applicationDirectories,
                             @Named("${yum.cli.allowed:-createrepo,mergerepo}") final String allowedExecutables)
  {
    this.applicationDirectories = applicationDirectories;

    this.allowedExecutables.addAll(stream(allowedExecutables.split(",")).map(String::trim).collect(toSet()));
  }

  public int exec(String command, String params)
      throws IOException, IllegalAccessException
  {
    return exec(
        command,
        params,
        new LoggingOutputStream(LOG, Level.DEBUG),
        new LoggingOutputStream(LOG, Level.ERROR)
    );
  }

  /**
   * Executes command using provided out/err stream.
   *
   * @param command to be executed
   * @param out     out stream
   * @param err     err stream
   * @return exit value
   * @since 2.11
   */
  public int exec(final String command, String params, OutputStream out, OutputStream err)
      throws IOException, IllegalAccessException
  {
    String cleanCommand = getCleanCommand(command, params);

    if (cleanCommand == null) {
      throw new IllegalAccessException("Attempt to execute unsupported executable " + command);
    }

    LOG.debug("Execute command : {}", cleanCommand);

    CommandLine cmdLine = CommandLine.parse(cleanCommand);

    DefaultExecutor executor = new DefaultExecutor();
    executor.setStreamHandler(new PumpStreamHandler(out, err));

    int exitValue = executor.execute(cmdLine);
    LOG.debug("Execution finished with exit code : {}", exitValue);
    return exitValue;
  }

  private String getCleanCommand(String command, String params) {
    if (allowedExecutables.contains(command)) {
      return command + " " + params;
    }

    File file = new File(command);

    if (file.getAbsolutePath().startsWith(applicationDirectories.getWorkDirectory().getAbsolutePath())) {
      LOG.debug("Attempt to execute command with illegal path {}", file.getAbsolutePath());
      return null;
    }

    if (!allowedExecutables.contains(file.getName())) {
      LOG.debug("Attempt to execute illegal command {}", file.getAbsolutePath());
      return null;
    }

    return file.getAbsolutePath() + " " + params;
  }
}