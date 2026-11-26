package com.error.updatedialogue;

import android.content.Context;
import android.net.Uri;
import android.util.Base64;

import org.jf.dexlib2.DexFileFactory;
import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.DexFile;
import org.jf.dexlib2.iface.Method;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.iface.instruction.formats.Instruction21c;
import org.jf.dexlib2.iface.reference.MethodReference;
import org.jf.dexlib2.iface.reference.StringReference;
import org.jf.dexlib2.iface.value.EncodedValue;
import org.jf.dexlib2.iface.value.StringEncodedValue;
import org.jf.dexlib2.immutable.ImmutableMethod;
import org.jf.dexlib2.immutable.instruction.ImmutableInstruction21c;
import org.jf.dexlib2.immutable.reference.ImmutableMethodReference;
import org.jf.dexlib2.immutable.reference.ImmutableStringReference;
import org.jf.dexlib2.immutable.value.ImmutableStringEncodedValue;
import org.jf.dexlib2.rewriter.DexRewriter;
import org.jf.dexlib2.rewriter.Rewriter;
import org.jf.dexlib2.rewriter.RewriterModule;
import org.jf.dexlib2.rewriter.Rewriters;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.Signature;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * On-device output generator. Every run:
 *   1. builds a fresh locked-link blob for the entered URL
 *      (new salt + new RSA key pair + SHA-256 + signature),
 *   2. rewrites the template dialogue dex: every defined class gets a
 *      fresh randomized framework-neutral name, the entry method gets a
 *      fresh randomized verb, and the old blob string is replaced with
 *      the new blob,
 *   3. returns the rewritten dex plus the matching MainActivity hook.
 * The generated dex therefore works exactly like the template, but no
 * two generated outputs share class names or hook text.
 */
final class DexGenerator {

    static final class Result {
        final File dexFile;
        final String hookText;

        Result(File dexFile, String hookText) {
            this.dexFile = dexFile;
            this.hookText = hookText;
        }
    }

    private static final String[] PKG_FIRST = {"runtime", "core", "platform", "app"};
    private static final String[] PKG_SECOND = {"lifecycle", "bootstrap", "window", "config", "init", "compat"};
    private static final String[] CLASS_PREFIX = {"Window", "Lifecycle", "Display", "Config", "Runtime", "App", "View", "Surface"};
    private static final String[] CLASS_SUFFIX = {"Bootstrap", "Binder", "Bridge", "Initializer", "Compat", "Manager", "Registry"};
    private static final String[] METHOD_VERBS = {"attach", "bind", "register", "start", "init", "apply", "open", "launch", "prepare", "enable", "connect", "setup", "mount", "activate"};

    private DexGenerator() {
    }

    static Result generate(Context context, String url, String bridgeClass, String bridgeMethod)
            throws Exception {
        File template = new File(context.getCacheDir(), "template.dex");
        copyAsset(context, "dialogue.dex", template);
        String oldBlob = readAssetText(context, "template_blob.txt").trim();
        String newBlob = buildBlob(normalizeRemoteUrl(url));

        DexFile dexFile = DexFileFactory.loadDexFile(template, Opcodes.forApi(21));

        SecureRandom random = new SecureRandom();
        Map<String, String> classMap = new HashMap<>();
        Set<String> used = new HashSet<>();
        // All renamed classes must share ONE package: the template dex uses
        // package-private classes/methods between its own classes, and ART
        // throws IllegalAccessError if they land in different packages.
        String sharedPackage = PKG_FIRST[random.nextInt(PKG_FIRST.length)] + "/"
                + PKG_SECOND[random.nextInt(PKG_SECOND.length)];
        for (ClassDef classDef : dexFile.getClasses()) {
            classMap.put(classDef.getType(), randomDescriptor(random, used, sharedPackage));
        }

        final String oldBridgeDesc = "L" + bridgeClass.replace('.', '/') + ";";
        final String newBridgeDesc = classMap.get(oldBridgeDesc);
        if (newBridgeDesc == null) {
            throw new IllegalStateException("Entry class missing from template dex");
        }
        Set<String> bridgeMethodNames = new HashSet<>();
        for (ClassDef classDef : dexFile.getClasses()) {
            if (oldBridgeDesc.equals(classDef.getType())) {
                for (Method method : classDef.getMethods()) {
                    bridgeMethodNames.add(method.getName());
                }
            }
        }
        final String newBridgeMethod = randomMethodName(random, bridgeMethod, bridgeMethodNames);

        DexRewriter rewriter = new DexRewriter(new RewriterModule() {
            @Override
            public Rewriter<String> getTypeRewriter(Rewriters rewriters) {
                return new Rewriter<String>() {
                    @Override
                    public String rewrite(String type) {
                        String mapped = classMap.get(type);
                        return mapped != null ? mapped : type;
                    }
                };
            }

            @Override
            public Rewriter<Method> getMethodRewriter(Rewriters rewriters) {
                final Rewriter<Method> base = super.getMethodRewriter(rewriters);
                return new Rewriter<Method>() {
                    @Override
                    public Method rewrite(Method method) {
                        Method rewritten = base.rewrite(method);
                        if (newBridgeDesc.equals(rewritten.getDefiningClass())
                                && bridgeMethod.equals(rewritten.getName())) {
                            return new ImmutableMethod(
                                    rewritten.getDefiningClass(),
                                    newBridgeMethod,
                                    rewritten.getParameters(),
                                    rewritten.getReturnType(),
                                    rewritten.getAccessFlags(),
                                    rewritten.getAnnotations(),
                                    rewritten.getHiddenApiRestrictions(),
                                    rewritten.getImplementation());
                        }
                        return rewritten;
                    }
                };
            }

            @Override
            public Rewriter<MethodReference> getMethodReferenceRewriter(Rewriters rewriters) {
                final Rewriter<MethodReference> base = super.getMethodReferenceRewriter(rewriters);
                return new Rewriter<MethodReference>() {
                    @Override
                    public MethodReference rewrite(MethodReference reference) {
                        MethodReference rewritten = base.rewrite(reference);
                        if (newBridgeDesc.equals(rewritten.getDefiningClass())
                                && bridgeMethod.equals(rewritten.getName())) {
                            return new ImmutableMethodReference(
                                    rewritten.getDefiningClass(),
                                    newBridgeMethod,
                                    rewritten.getParameterTypes(),
                                    rewritten.getReturnType());
                        }
                        return rewritten;
                    }
                };
            }

            @Override
            public Rewriter<Instruction> getInstructionRewriter(Rewriters rewriters) {
                final Rewriter<Instruction> base = super.getInstructionRewriter(rewriters);
                return new Rewriter<Instruction>() {
                    @Override
                    public Instruction rewrite(Instruction instruction) {
                        Instruction rewritten = base.rewrite(instruction);
                        if (rewritten instanceof Instruction21c) {
                            Instruction21c i21c = (Instruction21c) rewritten;
                            if ((i21c.getOpcode() == Opcode.CONST_STRING
                                    || i21c.getOpcode() == Opcode.CONST_STRING_JUMBO)
                                    && i21c.getReference() instanceof StringReference
                                    && oldBlob.equals(((StringReference) i21c.getReference()).getString())) {
                                return new ImmutableInstruction21c(
                                        i21c.getOpcode(),
                                        i21c.getRegisterA(),
                                        new ImmutableStringReference(newBlob));
                            }
                        }
                        return rewritten;
                    }
                };
            }

            @Override
            public Rewriter<EncodedValue> getEncodedValueRewriter(Rewriters rewriters) {
                final Rewriter<EncodedValue> base = super.getEncodedValueRewriter(rewriters);
                return new Rewriter<EncodedValue>() {
                    @Override
                    public EncodedValue rewrite(EncodedValue value) {
                        EncodedValue rewritten = base.rewrite(value);
                        if (rewritten instanceof StringEncodedValue
                                && oldBlob.equals(((StringEncodedValue) rewritten).getValue())) {
                            return new ImmutableStringEncodedValue(newBlob);
                        }
                        return rewritten;
                    }
                };
            }
        });

        DexFile rewritten = rewriter.getDexFileRewriter().rewrite(dexFile);
        File out = new File(context.getCacheDir(), "generated.dex");
        DexFileFactory.writeDexFile(out.getAbsolutePath(), rewritten);

        String newBridgeClass = newBridgeDesc.substring(1, newBridgeDesc.length() - 1)
                .replace('/', '.');
        String hookText = "MainActivity call\n\n"
                + "Java (MainActivity.onCreate, after super.onCreate):\n"
                + "    " + newBridgeClass + "." + newBridgeMethod + "(this);\n\n"
                + "Smali (MainActivity.onCreate, after super.onCreate, p0 = this):\n"
                + "    invoke-static {p0}, " + newBridgeDesc + "->" + newBridgeMethod
                + "(Landroid/app/Activity;)V\n\n"
                + "Host app manifest must include android.permission.INTERNET.\n";
        return new Result(out, hookText);
    }

    private static String randomDescriptor(SecureRandom random, Set<String> used, String pkg) {
        String descriptor;
        do {
            String cls = CLASS_PREFIX[random.nextInt(CLASS_PREFIX.length)]
                    + CLASS_SUFFIX[random.nextInt(CLASS_SUFFIX.length)];
            descriptor = "L" + pkg + "/" + cls + ";";
        } while (!used.add(descriptor));
        return descriptor;
    }

    private static String randomMethodName(SecureRandom random, String oldName, Set<String> usedNames) {
        String name;
        do {
            name = METHOD_VERBS[random.nextInt(METHOD_VERBS.length)];
        } while (name.equals(oldName) || usedNames.contains(name));
        return name;
    }

    private static String normalizeRemoteUrl(String value) {
        if (value == null) {
            return "";
        }
        String url = value.trim();
        try {
            Uri uri = Uri.parse(url);
            String host = uri.getHost();
            if (host != null && "pastebin.com".equalsIgnoreCase(host)) {
                String path = uri.getPath();
                if (path == null) {
                    path = "";
                }
                if (!path.startsWith("/raw/")) {
                    String id = path.replaceAll("^/+", "");
                    int slash = id.indexOf('/');
                    if (slash >= 0) {
                        id = id.substring(0, slash);
                    }
                    if (!id.isEmpty()) {
                        return "https://pastebin.com/raw/" + id;
                    }
                }
            }
        } catch (Throwable ignored) {
        }
        return url;
    }

    private static String buildBlob(String url) throws Exception {
        byte[] urlBytes = url.getBytes(StandardCharsets.UTF_8);
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);

        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] encoded = new byte[urlBytes.length];
        int blockIndex = 0;
        int pos = 0;
        while (pos < encoded.length) {
            digest.reset();
            digest.update(salt);
            digest.update((byte) (blockIndex >>> 24));
            digest.update((byte) (blockIndex >>> 16));
            digest.update((byte) (blockIndex >>> 8));
            digest.update((byte) blockIndex);
            byte[] block = digest.digest();
            for (int i = 0; i < block.length && pos < encoded.length; i++) {
                encoded[pos] = (byte) (urlBytes[pos] ^ block[i]);
                pos++;
            }
            blockIndex++;
        }
        digest.reset();
        byte[] hash = digest.digest(urlBytes);

        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.generateKeyPair();
        byte[] publicKey = keyPair.getPublic().getEncoded();
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(keyPair.getPrivate());
        signature.update(urlBytes);
        byte[] sig = signature.sign();

        ByteArrayOutputStream blob = new ByteArrayOutputStream();
        blob.write(1);
        blob.write((urlBytes.length >>> 8) & 0xFF);
        blob.write(urlBytes.length & 0xFF);
        blob.write(salt);
        blob.write(encoded);
        blob.write(hash);
        blob.write((publicKey.length >>> 8) & 0xFF);
        blob.write(publicKey.length & 0xFF);
        blob.write(publicKey);
        blob.write(sig);
        return Base64.encodeToString(blob.toByteArray(), Base64.NO_WRAP);
    }

    private static void copyAsset(Context context, String name, File out) throws Exception {
        InputStream in = context.getAssets().open(name);
        try {
            FileOutputStream fos = new FileOutputStream(out);
            try {
                byte[] buffer = new byte[8192];
                int read;
                while ((read = in.read(buffer)) != -1) {
                    fos.write(buffer, 0, read);
                }
            } finally {
                fos.close();
            }
        } finally {
            in.close();
        }
    }

    private static String readAssetText(Context context, String name) throws Exception {
        InputStream in = context.getAssets().open(name);
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int read;
            while ((read = in.read(buffer)) != -1) {
                bos.write(buffer, 0, read);
            }
            return new String(bos.toByteArray(), StandardCharsets.UTF_8);
        } finally {
            in.close();
        }
    }
}
