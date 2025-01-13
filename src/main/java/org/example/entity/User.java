package org.example.entity;

import lombok.Getter;
import lombok.Setter;
import org.example.entity.basic.BasicEntity;

import java.util.Date;

@Getter
@Setter
public class User extends BasicEntity {

    private String id;


    private String username;


    private String email;


    private String password;


    private String salt;

    private Status status;


    private Role role;


    private String avatarUrl;


    private String phoneNumber;


    private String address;


    private Date birthday;

    private String nickname;

    // Enum for Status
    public enum Status {
        ACTIVE("active"),
        INACTIVE("inactive"),
        DISABLED("disabled");

        private final String value;

        Status(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    // Enum for Role
    public enum Role {
        SUPERADMIN("superadmin"),
        ADMIN("admin"),
        USER("user");

        private final String value;

        Role(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }
}
