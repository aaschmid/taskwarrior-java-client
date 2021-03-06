package de.aaschmid.taskwarrior.client;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
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
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.bouncycastle.asn1.pkcs.RSAPrivateKey;
import org.bouncycastle.crypto.params.RSAPrivateCrtKeyParameters;
import org.bouncycastle.crypto.util.PrivateKeyInfoFactory;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;

import static java.util.Objects.requireNonNull;

class KeyStoreBuilder {

    private static final String CERTIFICATE_TYPE = "X.509";
    private static final String KEY_ALGORITHM_RSA = "RSA";
    private static final String PEM_TYPE_PKCS1 = "RSA PRIVATE KEY";
    private static final String PEM_TYPE_PKCS8 = "PRIVATE KEY";

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

    /**
     * Provide the non-null private key file to use. Supported are PKCS#1 and PKCS#8 keys in {@code *.PEM} format as well as PKCS#8 keys in
     * {@code *.DER} format.
     */
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
            CertificateFactory cf = CertificateFactory.getInstance(CERTIFICATE_TYPE);
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
                PemReader pemReader = new PemReader(new InputStreamReader(new ByteArrayInputStream(bytes), StandardCharsets.UTF_8));
                PemObject privateKeyObject = pemReader.readPemObject();

                switch (privateKeyObject.getType()) {
                    case PEM_TYPE_PKCS1:
                        return createPrivateKeyForPkcs1(privateKeyObject.getContent());
                    case PEM_TYPE_PKCS8:
                        return createPrivateKeyForPkcs8(privateKeyObject.getContent());
                    default:
                        throw new TaskwarriorKeyStoreException("Unsupported key algorithm '%s'.", privateKeyObject.getType());
                }
            }
            return createPrivateKeyForPkcs8(bytes);
        } catch (IOException e) {
            throw new TaskwarriorKeyStoreException(e, "Could not read private key of '%s' via input stream.", privateKeyFile);
        }
    }

    private PrivateKey createPrivateKeyForPkcs1(byte[] privateKeyBytes) {
        RSAPrivateKey rsa = RSAPrivateKey.getInstance(privateKeyBytes);
        RSAPrivateCrtKeyParameters keyParameters = new RSAPrivateCrtKeyParameters(
                rsa.getModulus(),
                rsa.getPublicExponent(),
                rsa.getPrivateExponent(),
                rsa.getPrime1(),
                rsa.getPrime2(),
                rsa.getExponent1(),
                rsa.getExponent2(),
                rsa.getCoefficient());

        try {
            return new JcaPEMKeyConverter().getPrivateKey(PrivateKeyInfoFactory.createPrivateKeyInfo(keyParameters));
        } catch (IOException e) {
            throw new TaskwarriorKeyStoreException(e, "Failed to encode PKCS#1 private key of '%s'.", privateKeyFile);
        }
    }

    private PrivateKey createPrivateKeyForPkcs8(byte[] privateKeyBytes) {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM_RSA);
            return keyFactory.generatePrivate(new PKCS8EncodedKeySpec(privateKeyBytes));
        } catch (NoSuchAlgorithmException e) {
            throw new TaskwarriorKeyStoreException(e, "Key factory could not be initialized for algorithm '%s'.", KEY_ALGORITHM_RSA);
        } catch (InvalidKeySpecException e) {
            throw new TaskwarriorKeyStoreException(e, "Invalid key spec for %s private key in '%s'.", KEY_ALGORITHM_RSA, privateKeyFile);
        }
    }
}
