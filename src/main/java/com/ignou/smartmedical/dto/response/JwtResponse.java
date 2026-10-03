package com.ignou.smartmedical.dto.response;

import com.ignou.smartmedical.enums.Role;

public class JwtResponse {
    private String token;
    private String type = "Bearer";
    private Long id;
    private String usernameOrEmail;
    private Role role;
    private String name;

    public JwtResponse() {}

    public JwtResponse(String token, Long id, String usernameOrEmail, Role role, String name) {
        this.token = token;
        this.id = id;
        this.usernameOrEmail = usernameOrEmail;
        this.role = role;
        this.name = name;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsernameOrEmail() { return usernameOrEmail; }
    public void setUsernameOrEmail(String usernameOrEmail) { this.usernameOrEmail = usernameOrEmail; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}