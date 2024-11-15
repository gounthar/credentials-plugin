/*
 * The MIT License
 *
 * Copyright (c) 2011-2014, CloudBees, Inc., Stephen Connolly.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.cloudbees.plugins.credentials.domains;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Util;
import java.io.Serial;

/**
 * A requirement for a specific URI path, for example <code>/</code> or <code>/foo/bar/manchu</code>
 *
 * @since 1.12
 */
public class PathRequirement extends DomainRequirement {
    /**
     * Ensure consistent serialization.
     */
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * The path.
     */
    @NonNull
    private final String path;

    /**
     * Constructs a requirement for the specified scheme.
     *
     * @param path the path.
     */
    public PathRequirement(@CheckForNull String path) {
        path = Util.fixNull(path);
        this.path = path.startsWith("/") ? path : "/" + path;
    }

    /**
     * Returns the path.
     *
     * @return the path.
     */
    @NonNull
    public String getPath() {
        return path;
    }
}
