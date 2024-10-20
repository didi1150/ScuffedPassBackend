package me.didi.PWMBackend.model.table;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.hibernate.annotations.ColumnDefault;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
@Entity
@Builder
@Table(name = "users")
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class User implements UserDetails {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "user_id")
	private Long id;

	@ColumnDefault("false")
	@Column(name = "enabled", nullable = false)
	@Builder.Default
	private boolean enabled = false;

	@ColumnDefault("false")
	@Column(name = "locked", nullable = false)
	@Builder.Default
	private boolean locked = false;

	@Column(name = "user_email")
	private String email;

	@Column(name = "user_pw")
	private String password;

	@Column(name = "user_salt", length = 10000)
	private String salt;
	@Column(name = "public_key", length = 10000)
	private String publicKey;
	@Column(name = "private_key_master", length = 10000)
	private String privateKeyMaster;
	@Column(name = "private_key_recovery", length = 10000)
	private String privateKeyRecovery;
	@Column(name = "encryption_key", length = 10000)
	private String encryptionKey;
	@Column(name = "iv")
	private String iv;

	@Column(name = "first_login")
	@ColumnDefault("true")
	@Builder.Default
	private boolean isFirstLogin = true;

	@OneToMany(mappedBy = "user")
	private List<Token> tokens;

	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private Set<Password> passwords;

	@ManyToMany(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
	@JoinTable(name = "users_roles", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
	private Set<Role> roles;

	@Column(name = "created_at", nullable = false)
	@Temporal(TemporalType.TIMESTAMP)
	@Builder.Default
	private LocalDateTime timestamp = LocalDateTime.now();

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return roles.stream().map(r -> new SimpleGrantedAuthority(r.getName())).toList();
	}

	@Override
	public String getPassword() {
		return password;
	}

	@Override
	public String getUsername() {
		return email;
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	@Override
	public boolean isAccountNonLocked() {
		return !locked;
	}
}
