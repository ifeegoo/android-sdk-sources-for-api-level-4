/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.dx.dex.file;

import com.android.dx.rop.annotation.Annotation;
import com.android.dx.rop.annotation.NameValuePair;
import com.android.dx.rop.cst.Constant;
import com.android.dx.rop.cst.CstAnnotation;
import com.android.dx.rop.cst.CstArray;
import com.android.dx.rop.cst.CstInteger;
import com.android.dx.rop.cst.CstKnownNull;
import com.android.dx.rop.cst.CstMethodRef;
import com.android.dx.rop.cst.CstString;
import com.android.dx.rop.cst.CstType;
import com.android.dx.rop.cst.CstUtf8;
import com.android.dx.rop.type.Type;
import com.android.dx.rop.type.TypeList;

import java.util.ArrayList;

import static com.android.dx.rop.annotation.AnnotationVisibility.*;

/**
 * Utility class for dealing with annotations.
 */
public final class AnnotationUtils {
    /** non-null; type for <code>AnnotationDefault</code> annotations */
    private static final CstType ANNOTATION_DEFAULT_TYPE = 
        CstType.intern(Type.intern("Ldalvik/annotation/AnnotationDefault;"));

    /** non-null; type for <code>EnclosingClass</code> annotations */
    private static final CstType ENCLOSING_CLASS_TYPE = 
        CstType.intern(Type.intern("Ldalvik/annotation/EnclosingClass;"));

    /** non-null; type for <code>EnclosingMethod</code> annotations */
    private static final CstType ENCLOSING_METHOD_TYPE = 
        CstType.intern(Type.intern("Ldalvik/annotation/EnclosingMethod;"));

    /** non-null; type for <code>InnerClass</code> annotations */
    private static final CstType INNER_CLASS_TYPE = 
        CstType.intern(Type.intern("Ldalvik/annotation/InnerClass;"));

    /** non-null; type for <code>MemberClasses</code> annotations */
    private static final CstType MEMBER_CLASSES_TYPE = 
        CstType.intern(Type.intern("Ldalvik/annotation/MemberClasses;"));

    /** non-null; type for <code>Signature</code> annotations */
    private static final CstType SIGNATURE_TYPE = 
        CstType.intern(Type.intern("Ldalvik/annotation/Signature;"));

    /** non-null; type for <code>Throws</code> annotations */
    private static final CstType THROWS_TYPE = 
        CstType.intern(Type.intern("Ldalvik/annotation/Throws;"));

    /** non-null; the UTF-8 constant <code>"accessFlags"</code> */
    private static final CstUtf8 ACCESS_FLAGS_UTF = new CstUtf8("accessFlags");

    /** non-null; the UTF-8 constant <code>"name"</code> */
    private static final CstUtf8 NAME_UTF = new CstUtf8("name");

    /** non-null; the UTF-8 constant <code>"value"</code> */
    private static final CstUtf8 VALUE_UTF = new CstUtf8("value");

    /**
     * This class is uninstantiable.
     */
    private AnnotationUtils() {
        // This space intentionally left blank.
    }

    /**
     * Constructs a standard <code>AnnotationDefault</code> annotation.
     * 
     * @param defaults non-null; the defaults, itself as an annotation
     * @return non-null; the constructed annotation
     */
    public static Annotation makeAnnotationDefault(Annotation defaults) {
        Annotation result = new Annotation(ANNOTATION_DEFAULT_TYPE, SYSTEM);

        result.put(new NameValuePair(VALUE_UTF, new CstAnnotation(defaults)));
        result.setImmutable();
        return result;
    }

    /**
     * Constructs a standard <code>EnclosingClass</code> annotation.
     * 
     * @param clazz non-null; the enclosing class
     * @return non-null; the annotation
     */
    public static Annotation makeEnclosingClass(CstType clazz) {
        Annotation result = new Annotation(ENCLOSING_CLASS_TYPE, SYSTEM);

        result.put(new NameValuePair(VALUE_UTF, clazz));
        result.setImmutable();
        return result;
    }

    /**
     * Constructs a standard <code>EnclosingMethod</code> annotation.
     * 
     * @param method non-null; the enclosing method
     * @return non-null; the annotation
     */
    public static Annotation makeEnclosingMethod(CstMethodRef method) {
        Annotation result = new Annotation(ENCLOSING_METHOD_TYPE, SYSTEM);

        result.put(new NameValuePair(VALUE_UTF, method));
        result.setImmutable();
        return result;
    }

    /**
     * Constructs a standard <code>InnerClass</code> annotation.
     * 
     * @param name null-ok; the original name of the class, or
     * <code>null</code> to represent an anonymous class
     * @param accessFlags the original access flags
     * @return non-null; the annotation
     */
    public static Annotation makeInnerClass(CstUtf8 name, int accessFlags) {
        Annotation result = new Annotation(INNER_CLASS_TYPE, SYSTEM);
        Constant nameCst =
            (name != null) ? new CstString(name) : CstKnownNull.THE_ONE;

        result.put(new NameValuePair(NAME_UTF, nameCst));
        result.put(new NameValuePair(ACCESS_FLAGS_UTF,
                        CstInteger.make(accessFlags)));
        result.setImmutable();
        return result;
    }

    /**
     * Constructs a standard <code>MemberClasses</code> annotation.
     * 
     * @param types non-null; the list of (the types of) the member classes
     * @return non-null; the annotation
     */
    public static Annotation makeMemberClasses(TypeList types) {
        CstArray array = makeCstArray(types);
        Annotation result = new Annotation(MEMBER_CLASSES_TYPE, SYSTEM);
        result.put(new NameValuePair(VALUE_UTF, array));
        result.setImmutable();
        return result;
    }

    /**
     * Constructs a standard <code>Signature</code> annotation.
     * 
     * @param signature non-null; the signature string
     * @return non-null; the annotation
     */
    public static Annotation makeSignature(CstUtf8 signature) {
        Annotation result = new Annotation(SIGNATURE_TYPE, SYSTEM);

        /*
         * Split the string into pieces that are likely to be common
         * across many signatures and the rest of the file.
         */
         
        String raw = signature.getString();
        int rawLength = raw.length();
        ArrayList<String> pieces = new ArrayList<String>(20);

        for (int at = 0; at < rawLength; /*at*/) {
            char c = raw.charAt(at);
            int endAt = at + 1;
            if (c == 'L') {
                // Scan to ';' or '<'. Consume ';' but not '<'.
                while (endAt < rawLength) {
                    c = raw.charAt(endAt);
                    if (c == ';') {
                        endAt++;
                        break;
                    } else if (c == '<') {
                        break;
                    }
                    endAt++;
                }
            } else {
                // Scan to 'L' without consuming it.
                while (endAt < rawLength) {
                    c = raw.charAt(endAt);
                    if (c == 'L') {
                        break;
                    }
                    endAt++;
                }
            }

            pieces.add(raw.substring(at, endAt));
            at = endAt;
        }

        int size = pieces.size();
        CstArray.List list = new CstArray.List(size);

        for (int i = 0; i < size; i++) {
            list.set(i, new CstString(pieces.get(i)));
        }

        list.setImmutable();
        
        result.put(new NameValuePair(VALUE_UTF, new CstArray(list)));
        result.setImmutable();
        return result;
    }

    /**
     * Constructs a standard <code>Throws</code> annotation.
     * 
     * @param types non-null; the list of thrown types
     * @return non-null; the annotation
     */
    public static Annotation makeThrows(TypeList types) {
        CstArray array = makeCstArray(types);
        Annotation result = new Annotation(THROWS_TYPE, SYSTEM);
        result.put(new NameValuePair(VALUE_UTF, array));
        result.setImmutable();
        return result;
    }

    /**
     * Converts a {@link TypeList} to a {@link CstArray}.
     * 
     * @param types non-null; the type list
     * @return non-null; the corresponding array constant
     */
    private static CstArray makeCstArray(TypeList types) {
        int size = types.size();
        CstArray.List list = new CstArray.List(size);

        for (int i = 0; i < size; i++) {
            list.set(i, CstType.intern(types.getType(i)));
        }

        list.setImmutable();
        return new CstArray(list);
    }
}
