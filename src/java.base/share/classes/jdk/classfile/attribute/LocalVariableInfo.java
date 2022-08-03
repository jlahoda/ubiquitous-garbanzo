/*
 * Copyright (c) 2022, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package jdk.classfile.attribute;

import java.lang.constant.ClassDesc;
import jdk.classfile.constantpool.Utf8Entry;
import jdk.classfile.impl.BoundLocalVariable;
import jdk.classfile.impl.UnboundAttribute;

/**
 * Models a single local variable in the {@link LocalVariableTableAttribute}.
 */
sealed public interface LocalVariableInfo
        permits UnboundAttribute.UnboundLocalVariableInfo, BoundLocalVariable {

    /**
     * {@return the index into the code array (inclusive) at which the scope of
     * this variable begins}
     */
    int startPc();

    /**
     * {@return the length of the region of the code array in which this
     * variable is in scope.}
     */
    int length();

    /**
     * {@return the name of the local variable}
     */
    Utf8Entry name();

    /**
     * {@return the field descriptor of the local variable}
     */
    Utf8Entry type();

    /**
     * {@return the field descriptor of the local variable}
     */
    default ClassDesc typeSymbol() {
        return ClassDesc.ofDescriptor(type().stringValue());
    }

    /**
     * {@return the index into the local variable array of the current frame
     * which holds this local variable}
     */
    int slot();

    /**
     * {@return a local variable description}
     * @param startPc the starting index into the code array (inclusive) for the
     *                scope of this variable
     * @param length the ending index into the code array (exclusive) for the scope
     *               of this variable
     * @param name the name of the variable
     * @param descriptor the field descriptor of the variable
     * @param slot the local variable slot for this variable
     */
    static LocalVariableInfo of(int startPc,
                                int length,
                                Utf8Entry name,
                                Utf8Entry descriptor,
                                int slot) {
        return new UnboundAttribute.UnboundLocalVariableInfo(startPc, length,
                                                             name, descriptor,
                                                             slot);
    }
}
