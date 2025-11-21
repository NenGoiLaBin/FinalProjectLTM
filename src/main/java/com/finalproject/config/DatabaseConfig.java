package com.finalproject.config;

import javax.servlet.ServletContext;

public class DatabaseConfig {
    private String driver;
    private String url;
    private String username;
    private String password;
    
    public DatabaseConfig(ServletContext context) {
        this.driver = context.getInitParameter("dbDriver");
        this.url = context.getInitParameter("dbUrl");
        this.username = context.getInitParameter("dbUsername");
        this.password = context.getInitParameter("dbPassword");
    }
    
    public String getDriver() {
        return driver;
    }
    
    public String getUrl() {
        return url;
    }
    
    public String getUsername() {
        return username;
    }
    
    public String getPassword() {
        return password;
    }
}

