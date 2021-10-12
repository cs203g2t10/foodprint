// package foodprint.backend.config;

// import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
// import java.util.Collection;

// import org.springframework.security.core.GrantedAuthority;

// public class AuthToken extends UsernamePasswordAuthenticationToken {
//     private static final long serialVersionUID = 1L;

//     private String twoFaToken;

//     public AuthToken(Object principal, Object credentials, Collection<? extends GrantedAuthority> authorities) {
//         super(principal, credentials, authorities);
//     }

//     public AuthToken(Object principal, Object credentials) {
//         super(principal, credentials);
//     }

//     public String getTwoFaToken() {
//         return this.twoFaToken;
//     }

//     public void setTwoFaToken(String twoFaToken) {
//         this.twoFaToken = twoFaToken;
//     }
// }
