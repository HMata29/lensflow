
package com.lensflow.api.auth.entity;

import jakarta.persistence.*;

@Entity
@Table(
    name = "user_modules",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_user_modules_user_module",
            columnNames = {"user_id", "module"}
        )
    }
)
public class UserModule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AppModule module;

    @Column(nullable = false)
    private boolean enabled = true;

    public UserModule() {
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public AppModule getModule() {
        return module;
    }

    public void setModule(AppModule module) {
        this.module = module;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
