package de.aaschmid.taskwarrior.client;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.KeyStore.PasswordProtection;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.KeyStore.ProtectionParameter;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPrivateCrtKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Objects.requireNonNull;

class KeyStoreBuilder {

    private static final String TYPE_CERTIFICATE = "X.509";
    private static final String ALGORITHM_PRIVATE_KEY = "RSA";

    private static final Pattern PATTERN_PKCS1_PEM = Pattern.compile("-----BEGIN RSA PRIVATE KEY-----(.*)-----END RSA PRIVATE KEY-----");
    private static final Pattern PATTERN_PKCS8_PEM = Pattern.compile("-----BEGIN PRIVATE KEY-----(.*)-----END PRIVATE KEY-----");

    private ProtectionParameter keyStoreProtection;
    private File caCertFile;
    private File privateKeyCertFile;
    private File privateKeyFile;

    KeyStoreBuilder withKeyStoreProtection(ProtectionParameter keyStoreProtection) {
        this.keyStoreProtection = requireNonNull(keyStoreProtection, "'keyStoreProtection' must not be null.");
        return this;
    }

    KeyStoreBuilder withPasswordProtection(String password) {
        return withKeyStoreProtection(new PasswordProtection(requireNonNull(password, "'password' must not be null.").toCharArray()));
    }

    KeyStoreBuilder withCaCertFile(File caCertFile) {
        requireNonNull(caCertFile, "'caCertFile' must not be null.");
        if (!caCertFile.exists()) {
            throw new IllegalArgumentException(String.format("CA certificate '%s' does not exist.", caCertFile));
        }
        this.caCertFile = caCertFile;
        return this;
    }

    KeyStoreBuilder withPrivateKeyCertFile(File privateKeyCertFile) {
        requireNonNull(privateKeyCertFile, "'privateKeyCertFile' must not be null.");
        if (!privateKeyCertFile.exists()) {
            throw new IllegalArgumentException(String.format("Private key certificate '%s' does not exist.", privateKeyCertFile));
        }
        this.privateKeyCertFile = privateKeyCertFile;
        return this;
    }

    KeyStoreBuilder withPrivateKeyFile(File privateKeyFile) {
        requireNonNull(privateKeyFile, "'privateKeyFile' must not be null.");
        if (!privateKeyFile.exists()) {
            throw new IllegalArgumentException(String.format("Private key '%s' does not exist.", privateKeyFile));
        }
        this.privateKeyFile = privateKeyFile;
        return this;
    }

    KeyStore build() {
        KeyStore result;
        try {
            result = KeyStore.Builder.newInstance(KeyStore.getDefaultType(), null, keyStoreProtection).getKeyStore();
        } catch (KeyStoreException e) {
            throw new TaskwarriorKeyStoreException(e, "Could not build keystore: %s", e.getMessage());
        }

        AtomicInteger idx = new AtomicInteger(0);
        createCertificatesFor(caCertFile).forEach(c -> {
            try {
                result.setCertificateEntry("ca_" + idx.getAndIncrement(), c);
            } catch (KeyStoreException e) {
                throw new TaskwarriorKeyStoreException(e, "Could not add CA certificate '%s' to keystore.", caCertFile);
            }
        });

        Certificate[] privateKeyCertsChain = createCertificatesFor(privateKeyCertFile).toArray(new Certificate[0]);
        PrivateKey privateKey = createPrivateKeyFor(privateKeyFile);
        try {
            result.setEntry("key", new PrivateKeyEntry(privateKey, privateKeyCertsChain), keyStoreProtection);
        } catch (KeyStoreException e) {
            throw new TaskwarriorKeyStoreException(e, "Could not create private cert '%s' and key '%s' to keystore.", privateKeyCertFile,
                    privateKeyFile);
        }

        return result;
    }

    private List<Certificate> createCertificatesFor(File certFile) {
        List<Certificate> result = new ArrayList<>();
        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(certFile))) {
            CertificateFactory cf = CertificateFactory.getInstance(TYPE_CERTIFICATE);
            while (bis.available() > 0) {
                result.add(cf.generateCertificate(bis));
            }
        } catch (IOException e) {
            throw new TaskwarriorKeyStoreException(e, "Could not read certificates of '%s' via input stream.", certFile);
        } catch (CertificateException e) {
            throw new TaskwarriorKeyStoreException(e, "Could not generate certificates for '%s'.", certFile);
        }
        return result;
    }

    private PrivateKey createPrivateKeyFor(File privateKeyFile) {
        try {
            byte[] bytes = Files.readAllBytes(privateKeyFile.toPath());
            if (privateKeyFile.getName().endsWith("pem")) {
                String content = new String(bytes, StandardCharsets.UTF_8).replaceAll("\\n", "");

                Matcher pkcs1Matcher = PATTERN_PKCS1_PEM.matcher(content);
                if (pkcs1Matcher.find()) {
                    return createPrivateKeyFromPemPkcs1(pkcs1Matcher.group(1));
                }

                Matcher pkcs8Matcher = PATTERN_PKCS8_PEM.matcher(content);
                if (pkcs8Matcher.find()) {
                    return createPrivateKeyFromPemPkcs8(pkcs8Matcher.group(1));
                }

                throw new TaskwarriorKeyStoreException("Could not detect key algorithm for '%s'.", privateKeyFile);
            }
            return createPrivateKeyFromPkcs8Der(bytes);
        } catch (IOException e) {
            throw new TaskwarriorKeyStoreException(e, "Could not read private key of '%s' via input stream.", privateKeyFile);
        }
    }

    @SuppressWarnings("sunapi")
    private PrivateKey createPrivateKeyFromPemPkcs1(String privateKeyContent) throws IOException {
        try {
            byte[] bytes = Base64.getDecoder().decode(privateKeyContent);

            sun.security.util.DerInputStream derReader = new sun.security.util.DerInputStream(bytes);
            sun.security.util.DerValue[] seq = derReader.getSequence(0);
            // skip version seq[0];
            BigInteger modulus = seq[1].getBigInteger();
            BigInteger publicExp = seq[2].getBigInteger();
            BigInteger privateExp = seq[3].getBigInteger();
            BigInteger prime1 = seq[4].getBigInteger();
            BigInteger prime2 = seq[5].getBigInteger();
            BigInteger exp1 = seq[6].getBigInteger();
            BigInteger exp2 = seq[7].getBigInteger();
            BigInteger crtCoef = seq[8].getBigInteger();

            RSAPrivateCrtKeySpec keySpec = new RSAPrivateCrtKeySpec(modulus, publicExp, privateExp, prime1, prime2, exp1, exp2, crtCoef);
            return createPrivateKey(privateKeyFile, keySpec);
        } catch (Error | Exception e) {
            throw new TaskwarriorKeyStoreException("Could not use required but proprietary 'sun.security.util' package on this platform.", e);
        }
    }

    private PrivateKey createPrivateKeyFromPemPkcs8(String privateKeyContent) {
        byte[] bytes = Base64.getDecoder().decode(privateKeyContent);
        return createPrivateKey(privateKeyFile, new PKCS8EncodedKeySpec(bytes));
    }

    private PrivateKey createPrivateKeyFromPkcs8Der(byte[] privateKeyBytes) {
        return createPrivateKey(privateKeyFile, new PKCS8EncodedKeySpec(privateKeyBytes));
    }

    private PrivateKey createPrivateKey(File privateKeyFile, KeySpec keySpec) {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM_PRIVATE_KEY);
            return keyFactory.generatePrivate(keySpec);
        } catch (NoSuchAlgorithmException e) {
            throw new TaskwarriorKeyStoreException(e, "Key factory could not be initialized for algorithm '%s'.", ALGORITHM_PRIVATE_KEY);
        } catch (InvalidKeySpecException e) {
            throw new TaskwarriorKeyStoreException(e, "Could not generate private key for '%s'.", privateKeyFile);
        }
    }
}
