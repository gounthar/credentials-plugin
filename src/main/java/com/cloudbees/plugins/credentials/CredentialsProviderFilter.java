/*
 * The MIT License
 *
 * Copyright (c) 2016, CloudBees, Inc., Stephen Connolly.
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
package com.cloudbees.plugins.credentials;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.ExtensionList;
import hudson.ExtensionPoint;
import hudson.Util;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * A filter of {@link CredentialsProvider} instances used by {@link CredentialsProviderManager} to determine which
 * providers are active.
 *
 * @since 2.0
 */
public abstract class CredentialsProviderFilter extends AbstractDescribableImpl<CredentialsProviderFilter>
        implements Serializable, ExtensionPoint {
    /**
     * Ensure consistent serialization.
     */
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Returns {@code true} if and only if the supplied {@link CredentialsProvider} is permitted to be active.
     *
     * @param provider the {@link CredentialsProvider} to check.
     * @return {@code true} if and only if the supplied {@link CredentialsProvider} is permitted to be active.
     */
    public abstract boolean filter(CredentialsProvider provider);

    /**
     * {@inheritDoc}
     */
    @Override
    public CredentialsProviderFilterDescriptor getDescriptor() {
        return (CredentialsProviderFilterDescriptor) super.getDescriptor();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract int hashCode();

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract boolean equals(Object obj);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract String toString();

    /**
     * A filter that does not filter anything.
     *
     * @since 2.0
     */
    public static class None extends CredentialsProviderFilter {
        /**
         * Ensure consistent serialization.
         */
        @Serial
        private static final long serialVersionUID = 1L;

        /**
         * Our constructor.
         */
        @DataBoundConstructor
        public None() {
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean filter(CredentialsProvider provider) {
            return true;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            return o != null && getClass() == o.getClass();

        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int hashCode() {
            return None.class.hashCode();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return "None{}";
        }

        /**
         * Our descriptor.
         *
         * @since 2.0
         */
        @Extension
        public static class DescriptorImpl extends CredentialsProviderFilterDescriptor {

            /**
             * {@inheritDoc}
             */
            @NonNull
            @Override
            public String getDisplayName() {
                return Messages.CredentialsProviderFilter_None_DisplayName();
            }
        }
    }

    static Set<String> convertDescriptorClassNamesToIds(Set<String> idsOrDescriptorClassNames) {
        return idsOrDescriptorClassNames.stream().map(CredentialsProviderTypeRestriction::convertDescriptorClassNameToId).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /**
     * A filter that implements an allow-list policy, "if you are not on the list you can't come in".
     *
     * @since 2.0
     */
    public static class Includes extends CredentialsProviderFilter {
        /**
         * Ensure consistent serialization.
         */
        @Serial
        private static final long serialVersionUID = 1L;
        /**
         * The set of {@link Descriptor#getId}s that will be allowed.
         */
        @NonNull
        private Set<String> classNames;

        /**
         * Our constructor.
         *
         * @param classNames the list of allowed {@link Descriptor#getId}s
         */
        @DataBoundConstructor
        public Includes(@CheckForNull List<String> classNames) {
            this.classNames = new LinkedHashSet<>(Util.fixNull(classNames));
        }

        private Object readResolve() {
            classNames = convertDescriptorClassNamesToIds(classNames);
            return this;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean filter(CredentialsProvider provider) {
            return classNames.contains(provider.getId());
        }

        /**
         * Returns the list of allowed {@link Descriptor#getId}s.
         */
        @NonNull
        public List<String> getClassNames() {
            return new ArrayList<>(classNames);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            Includes includes = (Includes) o;

            return classNames.equals(includes.classNames);

        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int hashCode() {
            return classNames.hashCode();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return "Includes{" + "classes=" + getClassNames() +
                    '}';
        }

        /**
         * Our descriptor.
         *
         * @since 2.0
         */
        @Extension
        public static class DescriptorImpl extends CredentialsProviderFilterDescriptor {

            /**
             * {@inheritDoc}
             */
            @NonNull
            @Override
            public String getDisplayName() {
                return Messages.CredentialsProviderFilter_Includes_DisplayName();
            }

            /**
             * Gets the full list of available providers without any filtering.
             *
             * @return the full list of available providers without any filtering.
             */
            @SuppressWarnings("unused")
            @Restricted(NoExternalUse.class) // stapler EL binding
            public List<CredentialsProvider> getProviderDescriptors() {
                return ExtensionList.lookup(CredentialsProvider.class);
            }
        }
    }

    /**
     * A filter that implements a denylist policy, "if you are not on the list you can come in".
     *
     * @since 2.0
     */
    public static class Excludes extends CredentialsProviderFilter {
        /**
         * Ensure consistent serialization.
         */
        @Serial
        private static final long serialVersionUID = 1L;
        /**
         * The set of {@link Descriptor#getId}s that will not be allowed.
         */
        @NonNull
        private Set<String> classNames;

        /**
         * Our constructor.
         *
         * @param classNames the excluded list of {@link Descriptor#getId}s.
         */
        @DataBoundConstructor
        public Excludes(@CheckForNull List<String> classNames) {
            this.classNames = new LinkedHashSet<>(Util.fixNull(classNames));
        }

        private Object readResolve() {
            classNames = convertDescriptorClassNamesToIds(classNames);
            return this;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean filter(CredentialsProvider provider) {
            return !classNames.contains(provider.getId());
        }

        /**
         * Returns the list of banned {@link Descriptor#getId}s.
         */
        @NonNull
        public List<String> getClassNames() {
            return new ArrayList<>(classNames);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            Excludes excludes = (Excludes) o;

            return classNames.equals(excludes.classNames);

        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int hashCode() {
            return classNames.hashCode();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return "Excludes{" + "classes=" + getClassNames() +
                    '}';
        }

        /**
         * Our descriptor.
         *
         * @since 2.0
         */
        @Extension
        public static class DescriptorImpl extends CredentialsProviderFilterDescriptor {

            /**
             * {@inheritDoc}
             */
            @NonNull
            @Override
            public String getDisplayName() {
                return Messages.CredentialsProviderFilter_Excludes_DisplayName();
            }

            /**
             * Gets the full list of available providers without any filtering.
             *
             * @return the full list of available providers without any filtering.
             */
            @SuppressWarnings("unused")
            @Restricted(NoExternalUse.class) // stapler EL binding
            public List<CredentialsProvider> getProviderDescriptors() {
                return ExtensionList.lookup(CredentialsProvider.class);
            }

        }
    }
}
