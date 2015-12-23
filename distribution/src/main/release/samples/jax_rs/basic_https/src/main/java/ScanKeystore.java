import java.io.ByteArrayInputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.KeyStore.Entry;
import java.security.KeyStore.PasswordProtection;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.KeyStore.TrustedCertificateEntry;
import java.security.PublicKey;
import  java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Enumeration;

public class ScanKeystore {
    //Client keystore:
    //static String[] STORE_FILE_TO_SCAN_PATH={ "/Users/glocon/Miei/local_git/nike_repo/CXF_forked/cxf/distribution/src/main/release/samples/jax_rs/basic_https/src/main/config/","clientKeystore.jks"};
    //static String STORE_PWD ="cspass";
    //static String PK_PWD ="ckpass";
    
    
    //Server keystore:
    static String[] STORE_FILE_TO_SCAN_PATH={ "/Users/glocon/Miei/local_git/nike_repo/CXF_forked/cxf/distribution/src/main/release/samples/jax_rs/basic_https/src/main/config/","serviceKeystore.jks"};
    static String STORE_PWD ="sspass";
    static String PK_PWD ="skpass";
    
    
    public static void main(String[] args) throws Exception {
        KeyStore store = KeyStore.getInstance("JKS");
        Path path = FileSystems.getDefault().getPath(STORE_FILE_TO_SCAN_PATH[0],STORE_FILE_TO_SCAN_PATH[1] );
        byte[] bytes = Files.readAllBytes(path);
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        store.load(bis, STORE_PWD.toCharArray());
        for (Enumeration<String> aliases = store.aliases(); aliases.hasMoreElements();) {
            String alias = aliases.nextElement();
            Entry entry =null;
            if(store.isCertificateEntry(alias))
                 entry = store.getEntry(alias,null);
             else
                 entry = store.getEntry(alias,new PasswordProtection(PK_PWD.toCharArray())  );
            if(entry instanceof TrustedCertificateEntry){
                System.out.println("====================  alias = "+alias+" ===================");
                System.out.println(entry);
            }
            if(entry instanceof PrivateKeyEntry){
                System.out.println("====================  alias = "+alias+" ===================");
                PrivateKeyEntry pk = (PrivateKeyEntry)entry;
                System.out.println(pk);
                Certificate[] pk_certificatekey = pk.getCertificateChain();
                for(Certificate currCert:pk_certificatekey){
                    X509Certificate currCertx509 = (X509Certificate)currCert;
                    System.out.println(currCertx509);
                    String type = currCertx509.getType();
                    PublicKey publicKey = currCertx509.getPublicKey();
                    System.out.println(publicKey);
                }
               
            }
            
            
        }
    }
}
