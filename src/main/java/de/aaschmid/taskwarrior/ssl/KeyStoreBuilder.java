package de.aaschmid.taskwarrior.ssl;

import static java.util.Objects.requireNonNull;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
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
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class KeyStoreBuilder {

    public static final String KEYSTORE_PASSWORD = UUID.randomUUID().toString();

    private static final String TYPE_CERTIFICATE = "X.509";
    private static final String ALGORITHM_PRIVATE_KEY = "RSA";

    private ProtectionParameter keyStoreProtection = new PasswordProtection(KEYSTORE_PASSWORD.toCharArray());
    private File caCertFile;
    private File privateKeyCertFile;
    private File privateKeyFile;

    public KeyStoreBuilder withKeyStoreProtection(ProtectionParameter keyStoreProtection) {
        this.keyStoreProtection = requireNonNull(keyStoreProtection, "'keyStoreProtection' must not be null.");
        return this;
    }

    public KeyStoreBuilder withCaCertFile(File caCertFile) {
        requireNonNull(caCertFile, "'caCertFile' must not be null.");
        if (!caCertFile.exists()) {
            throw new IllegalArgumentException("Given CA certificate file does not exist, was '" + caCertFile + "'.");
        }
        this.caCertFile = caCertFile;
        return this;
    }

    public KeyStoreBuilder withPrivateKeyCertFile(File privateKeyCertFile) {
        requireNonNull(privateKeyCertFile, "'privateKeyCertFile' must not be null.");
        if (!privateKeyCertFile.exists()) {
            throw new IllegalArgumentException("Given private key certificate file does not exist, was '" + privateKeyCertFile + "'.");
        }
        this.privateKeyCertFile = privateKeyCertFile;
        return this;
    }

    /**
     * Provide the private key file to use. It must be in {@code *.DER} format. If you have a *.PME private key, you can create it using
     * {@code openssl}.
     * <p>
     * The required command looks like following:<br>
     * <code>$ openssl pkcs8 -topk8 -nocrypt -in key.pem -inform PEM -out key.der -outform DER</code>
     * </p>
     *
     * @param privateKeyFile private key file in {@code *.DER} format (must not be {@code null}
     * @return the {@link KeyStore} builder itself
     */
    public KeyStoreBuilder withPrivateKeyFile(File privateKeyFile) {
        requireNonNull(privateKeyFile, "'privateKeyFile' must not be null.");
        if (!privateKeyFile.exists()) {
            throw new IllegalArgumentException("Given private key file does not exist, was '" + privateKeyFile + "'.");
        }
        this.privateKeyFile = privateKeyFile;
        return this;
    }

    public KeyStore build() {
        KeyStore result;
        try {
            result = KeyStore.Builder.newInstance(KeyStore.getDefaultType(), null, keyStoreProtection).getKeyStore();
        } catch (KeyStoreException e) {
            throw new TaskwarriorKeyStoreException(e, "Could not build keystore: %s", e.getMessage());
        }

        AtomicInteger idx = new AtomicInteger(0);
        for (Certificate cert : createCertificatesFor(caCertFile)) {
            try {
                result.setCertificateEntry("ca_" + idx.getAndIncrement(), cert);
            } catch (KeyStoreException e) {
                throw new TaskwarriorKeyStoreException(e, "Could not add CA certificate '%s' to keystore: %s", caCertFile, e.getMessage());
            }
        }

        Certificate[] privateKeyCertsChain = createCertificatesFor(privateKeyCertFile).toArray(new Certificate[0]);
        PrivateKey privateKey = createPrivateKeyFor(privateKeyFile);
        try {
            result.setEntry("key", new PrivateKeyEntry(privateKey, privateKeyCertsChain), keyStoreProtection);
        } catch (KeyStoreException e) {
            throw new TaskwarriorKeyStoreException(e, "Could not private cert  '%s' and key  'Ts' to keystore: %s", privateKeyCertFile,
                    privateKeyFile, e.getMessage());
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
            throw new TaskwarriorKeyStoreException(e, "Could not read certificate '%s' via input stream: %s", certFile, e.getMessage());
        } catch (CertificateException e) {
            throw new TaskwarriorKeyStoreException(e, "Could not generate certificates for '%s': %s", certFile, e.getMessage());
        }
        return result;
    }

    private PrivateKey createPrivateKeyFor(File privateKeyFile) {
        byte[] privKeyBytes;
        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(privateKeyFile))) {
            privKeyBytes = new byte[(int) privateKeyFile.length()];
            bis.read(privKeyBytes);
        } catch (IOException e) {
            throw new TaskwarriorKeyStoreException(e, "Could not read private key '%s' via input stream: %s", privateKeyFile,
                    e.getMessage());
        }

        try {
            KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM_PRIVATE_KEY);
            return keyFactory.generatePrivate(new PKCS8EncodedKeySpec(privKeyBytes));
        } catch (NoSuchAlgorithmException e) {
            throw new TaskwarriorKeyStoreException(e, "Key factory could not be initialized for algorithm '%s': %s", ALGORITHM_PRIVATE_KEY,
                    e.getMessage());
        } catch (InvalidKeySpecException e) {
            throw new TaskwarriorKeyStoreException(e, "Could not generate private key for '%s': %s", privateKeyFile, e.getMessage());
        }
    }
}
