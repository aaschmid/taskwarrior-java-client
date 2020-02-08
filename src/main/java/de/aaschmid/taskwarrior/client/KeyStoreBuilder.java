package de.aaschmid.taskwarrior.client;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.KeyStore;
import java.security.KeyStore.PasswordProtection;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.KeyStore.ProtectionParameter;
import java.security.KeyStoreException;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.bouncycastle.asn1.pkcs.RSAPrivateKey;
import org.bouncycastle.crypto.params.RSAPrivateCrtKeyParameters;
import org.bouncycastle.crypto.util.PrivateKeyFactory;
import org.bouncycastle.crypto.util.PrivateKeyInfoFactory;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;

import static java.util.Objects.requireNonNull;

class KeyStoreBuilder {

    private static final String TYPE_CERTIFICATE = "X.509";

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

    public PrivateKey createPrivateKeyFor(File privateKeyFile) {
        byte[] privKeyBytes = null;
        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(privateKeyFile))) {
            privKeyBytes = new byte[(int) privateKeyFile.length()];
            bis.read(privKeyBytes);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String privateKeyString = "----...";

        PemObject privateKeyObject = null;
        try {
            PemReader pemReader;
            pemReader = new PemReader(new InputStreamReader(new ByteArrayInputStream(privKeyBytes)));

            privateKeyObject = pemReader.readPemObject();
        } catch (Exception e) {
            e.printStackTrace();
        }

        RSAPrivateCrtKeyParameters privateKeyParameter = null;
        if (privateKeyObject.getType().endsWith("RSA PRIVATE KEY")) {
            //PKCS#1 key
            RSAPrivateKey rsa   = RSAPrivateKey.getInstance(privateKeyObject.getContent());
            privateKeyParameter = new RSAPrivateCrtKeyParameters(
                    rsa.getModulus(),
                    rsa.getPublicExponent(),
                    rsa.getPrivateExponent(),
                    rsa.getPrime1(),
                    rsa.getPrime2(),
                    rsa.getExponent1(),
                    rsa.getExponent2(),
                    rsa.getCoefficient()
            );
        } else if (privateKeyObject.getType().endsWith("PRIVATE KEY")) {
            //PKCS#8 key
            try {
                privateKeyParameter = (RSAPrivateCrtKeyParameters) PrivateKeyFactory.createKey(
                        privateKeyObject.getContent()
                );
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            throw new RuntimeException("Unsupported key type: " + privateKeyObject.getType());
        }

        try {
            return new JcaPEMKeyConverter()
                    .getPrivateKey(
                            PrivateKeyInfoFactory.createPrivateKeyInfo(
                                    privateKeyParameter
                            )
                    );
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
