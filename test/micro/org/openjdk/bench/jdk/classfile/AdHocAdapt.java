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
package org.openjdk.bench.jdk.classfile;

import jdk.classfile.ClassTransform;
import jdk.classfile.Classfile;
import jdk.classfile.CodeTransform;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.infra.Blackhole;

/**
 * AdHocAdapt
 */
public class AdHocAdapt extends AbstractCorpusBenchmark {
    public enum X {
        LIFT(ClassTransform.transformingMethodBodies(CodeTransform.ACCEPT_ALL)),
        LIFT1(ClassTransform.transformingMethodBodies(CodeTransform.ACCEPT_ALL
                                                          .andThen(CodeTransform.ACCEPT_ALL))),
        LIFT2(ClassTransform.transformingMethodBodies(CodeTransform.ACCEPT_ALL)
                            .andThen(ClassTransform.transformingMethodBodies(CodeTransform.ACCEPT_ALL)));

        ClassTransform transform;

        X(ClassTransform transform) {
            this.transform = transform;
        }
    }

    @Param
    X transform;

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    public void transform(Blackhole bh) {
        for (byte[] bytes : classes)
            bh.consume(Classfile.parse(bytes).transform(transform.transform));
    }
}
