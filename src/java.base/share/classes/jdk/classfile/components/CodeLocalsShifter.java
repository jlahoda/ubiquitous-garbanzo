/*
 * Copyright (c) 2022, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
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
 *
 */
package jdk.classfile.components;

import java.lang.constant.MethodTypeDesc;
import java.util.Arrays;

import java.lang.reflect.AccessFlag;
import jdk.classfile.AccessFlags;
import jdk.classfile.CodeBuilder;
import jdk.classfile.CodeElement;
import jdk.classfile.CodeTransform;
import jdk.classfile.Signature;
import jdk.classfile.instruction.IncrementInstruction;
import jdk.classfile.instruction.LoadInstruction;
import jdk.classfile.instruction.LocalVariable;
import jdk.classfile.instruction.StoreInstruction;
import jdk.classfile.TypeKind;
import jdk.classfile.instruction.LocalVariableType;

/**
 * CodeLocalsShifter is a {@link jdk.classfile.CodeTransform} shifting locals to
 * newly allocated positions to avoid conflicts during code injection.
 * Locals pointing to the receiver or to method arguments slots are never shifted.
 * All locals pointing beyond the method arguments are re-indexed in order of appearance.
 * <p>
 * Sample use in code injection transformation:
 * <p>
 * {@snippet lang=java :
 *     methodBuilder.transformCode(codeModel, (codeBuilder, codeElement) -> {
 *         ...
 *         codeBuilder.transform(injectedCodeModel,
 *                               CodeLocalsShifter.of(methodModel.flags(), methodModel.methodTypeSymbol())
 *                               .andThen(otherInjectedCodeTransforms));
 *         ...
 *     }));
 * }
 */
public sealed interface CodeLocalsShifter extends CodeTransform {

    /**
     * Creates a new instance of CodeLocalsShifter
     * with fixed local slots calculated from provided method information
     * @param methodFlags flags of the method to construct CodeLocalsShifter for
     * @param methodDescriptor descriptor of the method to construct CodeLocalsShifter for
     * @return new instance of CodeLocalsShifter
     */
    static CodeLocalsShifter of(AccessFlags methodFlags, MethodTypeDesc methodDescriptor) {
        int fixed = methodFlags.has(AccessFlag.STATIC) ? 0 : 1;
        for (var param : methodDescriptor.parameterList())
            fixed += TypeKind.fromDescriptor(param.descriptorString()).slotSize();
        return new CodeLocalsShifterImpl(fixed);
    }

    final static class CodeLocalsShifterImpl implements CodeLocalsShifter {

        private int[] locals = new int[0];
        private final int fixed;

        private CodeLocalsShifterImpl(int fixed) {
            this.fixed = fixed;
        }

        @Override
        public void accept(CodeBuilder cob, CodeElement coe) {
            switch (coe) {
                case LoadInstruction li ->
                    cob.loadInstruction(
                            li.typeKind(),
                            shift(cob, li.slot(), li.typeKind()));
                case StoreInstruction si ->
                    cob.storeInstruction(
                            si.typeKind(),
                            shift(cob, si.slot(), si.typeKind()));
                case IncrementInstruction ii ->
                    cob.incrementInstruction(
                            shift(cob, ii.slot(), TypeKind.IntType),
                            ii.constant());
                case LocalVariable lv ->
                    cob.localVariable(
                            shift(cob, lv.slot(), TypeKind.fromDescriptor(lv.type().stringValue())),
                            lv.name(),
                            lv.type(),
                            lv.startScope(),
                            lv.endScope());
                case LocalVariableType lvt ->
                    cob.localVariableType(
                            shift(cob, lvt.slot(),
                                    (lvt.signatureSymbol() instanceof Signature.BaseTypeSig bsig)
                                            ? TypeKind.fromDescriptor(bsig.signatureString())
                                            : TypeKind.ReferenceType),
                            lvt.name(),
                            lvt.signature(),
                            lvt.startScope(),
                            lvt.endScope());
                default -> cob.with(coe);
            }
        }

        private int shift(CodeBuilder cob, int slot, TypeKind tk) {
            if (tk == TypeKind.VoidType)  throw new IllegalArgumentException("Illegal local void type");
            if (slot >= fixed) {
                int key = 2*slot - fixed + tk.slotSize() - 1;
                if (key >= locals.length) locals = Arrays.copyOf(locals, key + 20);
                slot = locals[key] - 1;
                if (slot < 0) {
                    slot = cob.allocateLocal(tk);
                    locals[key] = slot + 1;
                    if (tk.slotSize() == 2) locals[key - 1] = slot + 1;
                }
            }
            return slot;
        }
    }
}
