package foodprint.backend.model;

import java.util.Calendar;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/*
Tokens can be of various types, to allow for registration, email confirmation
and password resetting purposes
*/
@Entity
@Table
@EnableTransactionManagement
public class Token {

    public static final int EMAIL_CONFIRMATION_TOKEN = 1;
    public static final int PASSWORD_RESET_REQUEST_TOKEN = 2;

    // Properties
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private Long id;

    @Column(name="type")
    private int type;

    @JoinColumn(name="token", unique=true)
    private String token;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name="requestor")
    private User requestor;

    @Column(name="created")
    private Date created;

    @Column(name="expiry")
    private Date expiry;

    @Column(name="used")
    private boolean used;

    // Constructors
    protected Token() {}

    public Token(int type, User requestor) {
        this.type = type;
        this.requestor = requestor;

        this.token = generateRandomToken();
        this.used = false;
        
        this.created = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(created);
        calendar.add(Calendar.HOUR_OF_DAY, 48);
        this.expiry = calendar.getTime();
    }

    // Custom methods
    public boolean isValid() {
        Date now = new Date();
        boolean notUsed = !used;
        boolean createdBeforeNow = now.after(created);
        boolean expiredAfterNow = now.before(expiry);
        return notUsed && createdBeforeNow && expiredAfterNow;
    }

    private String generateRandomToken() {
        return UUID.randomUUID().toString();
    }

    public String getTypeName() {
        switch (type) {
            case 1:
                return "EMAIL CONFIRMATION";
            case 2:
                return "FORGOT PASSWORD";
            default:
                return "OTHER";
        }

    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getType() {
        return this.type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getToken() {
        return this.token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public User getRequestor() {
        return this.requestor;
    }

    public void setRequestor(User requestor) {
        this.requestor = requestor;
    }

    public Date getExpiry() {
        return this.expiry;
    }

    public void setExpiry(Date expiry) {
        this.expiry = expiry;
    }

    public Date getCreated() {
        return this.created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public boolean isUsed() {
        return this.used;
    }

    public boolean getUsed() {
        return this.used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Token)) {
            return false;
        }
        Token tkn = (Token) o;
        return Objects.equals(id, tkn.id) && type == tkn.type && Objects.equals(getToken(), tkn.token) && Objects.equals(requestor, tkn.requestor) && Objects.equals(expiry, tkn.expiry);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, type, token, requestor, expiry);
    }


}