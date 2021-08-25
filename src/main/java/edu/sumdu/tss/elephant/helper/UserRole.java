package edu.sumdu.tss.elephant.helper;

import io.javalin.core.security.Role;
import lombok.Getter;
import org.apache.commons.io.FileUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public enum UserRole implements Role {
    ANYONE(0) {
        public long maxConnections() {
            return 0;
        }

        public long maxDB() {
            return 0;
        }

        public long maxStorage() {
            return 0;
        }

        public long maxBackupsPerDB() {
            return 0;
        }
    },
    UNCHEKED(1) {
        public long maxConnections() {
            return 0;
        }

        public long maxDB() {
            return 0;
        }

        public long maxStorage() {
            return 0;
        }

        public long maxBackupsPerDB() {
            return 0;
        }
    },

    BASIC_USER(2) {
        public long maxConnections() {
            return 5;
        }

        public long maxDB() {
            return 2;
        }

        public long maxStorage() {
            return 5 * FileUtils.ONE_MB;
        }

        public long maxBackupsPerDB() {
            return 2;
        }
    },
    PROMOTED_USER(3) {
        public long maxConnections() {
            return 5;
        }

        public long maxDB() {
            return 2;
        }

        public long maxStorage() {
            return 20 * FileUtils.ONE_MB;
        }

        public long maxBackupsPerDB() {
            return 10;
        }
    },
    ADMIN(4) {
        public long maxConnections() {
            return 5;
        }

        public long maxDB() {
            return 100;
        }

        public long maxStorage() {
            return 50 * FileUtils.ONE_MB;
        }

        public long maxBackupsPerDB() {
            return 10;
        }
    };


    public final static Set<Role> AUTHED = new HashSet<>(Arrays.asList(UserRole.UNCHEKED, UserRole.BASIC_USER, UserRole.PROMOTED_USER, UserRole.ADMIN));
    @Getter
    private final Long value;

    UserRole(int value) {
        this.value = Long.valueOf(value);
    }

    public static UserRole byValue(long value) {
        for (UserRole role : UserRole.values()) {
            if (role.value.longValue() == value) {
                return role;
            }
        }
        throw new RuntimeException("UserRole not found for" + value);
    }

    public abstract long maxConnections();

    public abstract long maxDB();

    public abstract long maxStorage();

    public abstract long maxBackupsPerDB();
}
