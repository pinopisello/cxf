
package bank.common;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the bank.common package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _AccountNotFoundException_QNAME = new QName("http://cxf.apache.org/schemas/cxf/idl/bank", "AccountNotFoundException");
    private final static QName _AccountAlreadyExistsException_QNAME = new QName("http://cxf.apache.org/schemas/cxf/idl/bank", "AccountAlreadyExistsException");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: bank.common
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link AccountNotFoundExceptionType }
     * 
     */
    public AccountNotFoundExceptionType createAccountNotFoundExceptionType() {
        return new AccountNotFoundExceptionType();
    }

    /**
     * Create an instance of {@link AccountAlreadyExistsExceptionType }
     * 
     */
    public AccountAlreadyExistsExceptionType createAccountAlreadyExistsExceptionType() {
        return new AccountAlreadyExistsExceptionType();
    }

    /**
     * Create an instance of {@link GetAccountResponse }
     * 
     */
    public GetAccountResponse createGetAccountResponse() {
        return new GetAccountResponse();
    }

    /**
     * Create an instance of {@link Account }
     * 
     */
    public Account createAccount() {
        return new Account();
    }

    /**
     * Create an instance of {@link GetAccount }
     * 
     */
    public GetAccount createGetAccount() {
        return new GetAccount();
    }

    /**
     * Create an instance of {@link CreateAccountResponse }
     * 
     */
    public CreateAccountResponse createCreateAccountResponse() {
        return new CreateAccountResponse();
    }

    /**
     * Create an instance of {@link CreateAccount }
     * 
     */
    public CreateAccount createCreateAccount() {
        return new CreateAccount();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AccountNotFoundExceptionType }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link AccountNotFoundExceptionType }{@code >}
     */
    @XmlElementDecl(namespace = "http://cxf.apache.org/schemas/cxf/idl/bank", name = "AccountNotFoundException")
    public JAXBElement<AccountNotFoundExceptionType> createAccountNotFoundException(AccountNotFoundExceptionType value) {
        return new JAXBElement<AccountNotFoundExceptionType>(_AccountNotFoundException_QNAME, AccountNotFoundExceptionType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AccountAlreadyExistsExceptionType }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link AccountAlreadyExistsExceptionType }{@code >}
     */
    @XmlElementDecl(namespace = "http://cxf.apache.org/schemas/cxf/idl/bank", name = "AccountAlreadyExistsException")
    public JAXBElement<AccountAlreadyExistsExceptionType> createAccountAlreadyExistsException(AccountAlreadyExistsExceptionType value) {
        return new JAXBElement<AccountAlreadyExistsExceptionType>(_AccountAlreadyExistsException_QNAME, AccountAlreadyExistsExceptionType.class, null, value);
    }

}
