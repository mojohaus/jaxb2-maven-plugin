package org.codehaus.mojo.jaxb2.shared.filters.pattern;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.codehaus.mojo.jaxb2.shared.filters.AbstractFilter;

import java.io.File;
import java.io.FileFilter;
import java.util.regex.Pattern;

/**
 * <p>Accepts all directories. Non-directory files are regex-matched against the supplied regex pattern. Uses standard
 * java.util.regex.Pattern and java.util.regex.Matcher</p>
 * <p>
 * Usage:<br/>
 * (exclude everything matching the string "MyExcludedClass"):<br/>
 * <pre>
 *   ...
 *     <schemaSourceExcludeFilters>
 *       <filter implementation="org.codehaus.mojo.jaxb2.shared.filters.pattern.SimpleRegexFileFilter">
 *         <regex>^.*MyExcludedClass.*$</regex>
 *       </filter>
 *     </schemaSourceExcludeFilters>
 *   ...
 * </pre>
 * or exclude everything <i>but</i> files matching "MyExcludedClass"...<br/>
 * <pre>
 *   ...
 *     <schemaSourceExcludeFilters>
 *       <filter implementation="org.codehaus.mojo.jaxb2.shared.filters.pattern.SimpleRegexFileFilter">
 *         <regex>^((?!MyExcludedClass).)*$</regex>
 *       </filter>
 *     </schemaSourceExcludeFilters>
 *   ...
 * </pre>
 *
 * </p>
 *
 * @author <a href="mailto:markus.umefjord@gmail.com">Markus Umefjord</a>
 * @since 2.5.1
 */
public class SimpleRegexFileFilter extends AbstractFilter<File> implements FileFilter {

    private Pattern pattern;

    /**
     * Sets the regex string to match files against. Must be called to activate the filter. If not called, the filter will accept all files.
     *
     * @param regex The regex pattern to match
     */
    public void setRegex(String regex) {
        this.pattern = Pattern.compile(regex);
    }

    @Override
    protected boolean onCandidate(File pathname) {
        if (pathname.isDirectory()) {
            return false; //Never exclude the directories. Files are filtered below:
        }
        return pattern != null && pattern.matcher(pathname.getAbsolutePath()).matches();
    }

}
