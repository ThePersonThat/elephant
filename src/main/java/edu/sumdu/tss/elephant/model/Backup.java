package edu.sumdu.tss.elephant.model;

import lombok.Data;

import java.util.Date;

@Data
public class Backup {

    private Long id;

    private String database;
    private String point;
    private Date createdAt;
    private Date updatedAt;
    private String status;

    public enum BACKUP_STATE {PERFORMED, DONE, RESTORE}
}
